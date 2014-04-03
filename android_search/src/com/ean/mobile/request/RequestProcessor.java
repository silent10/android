/*
 * Copyright (c) 2013, Expedia Affiliate Network
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that redistributions of source code
 * retain the above copyright notice, these conditions, and the following
 * disclaimer. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies, 
 * either expressed or implied, of the Expedia Affiliate Network or Expedia Inc.
 */

package com.ean.mobile.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.Constants;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.BuildConfig;
import com.virtual_hotel_agent.search.VHAApplication;

/**
 * Responsible for the logic that executes requests through the EAN API.
 */
public final class RequestProcessor {

    /**
     * Constructor to prevent instantiation.
     */
    private RequestProcessor() {
        // empty code block
    }

    /**
     * Executes a request using the data provided in the Request object.
     * @param request contains all necessary data to execute a request and parse a response
     * @param <T> the response data
     * @return a response object populated by the JSON data retrieved from the API
     * @throws EanWsError thrown if any error messages are returned via the API call
     * @throws UrlRedirectionException thrown if the request is redirected (possibly due to a network issue)
     */
    public static <T> T run(final Request<T> request) throws EanWsError, UrlRedirectionException {
        try {
            final JSONObject jsonResponse = performApiRequest(request);
            return request.consume(jsonResponse);
        } catch (JSONException jsone) {
        	VHAApplication.logError(Constants.LOG_TAG, "Unable to process JSON", jsone);
            CommonParameters.customerSessionId = null;
        } catch (IOException ioe) {
        	VHAApplication.logError(Constants.LOG_TAG, "Could not read response from API", ioe);
        } catch (URISyntaxException use) {
        	VHAApplication.logError(Constants.LOG_TAG, "Improper URI syntax", use);
        }
        return null;
    }

    /**
     * Performs an api request at the specified path with the parameters listed.
     * @param request contains all necessary data to execute a request and parse a response
     * @return The String representation of the JSON returned from the request.
     * @throws IOException If there is a network error or some other connection issue.
     * @throws UrlRedirectionException If the network connection was unexpectedly redirected.
     * @throws URISyntaxException thrown if the URI cannot be built
     */
    private static String performApiRequestForString(final Request request)
            throws IOException, UrlRedirectionException, URISyntaxException {
        //Build the url
        final URLConnection connection;
        final long startTime = System.currentTimeMillis();
        final String endpoint;
        connection = request.getUri().toURL().openConnection();
        connection.setUseCaches(false); // signature makes caching impossible since url changes each time
        if (request.requiresSecure()) {
            // cause booking requests to use post.
            connection.setDoOutput(true);
            ((HttpURLConnection) connection).setRequestMethod("POST");
            ((HttpURLConnection) connection).setFixedLengthStreamingMode(0);
            
            endpoint = connection.getURL().getHost();
            Log.d(Constants.LOG_TAG, request.getName()+": secure endpoint (host only): " + endpoint);
        }
        else {
        	endpoint = connection.getURL().toString();
        	Log.d(Constants.LOG_TAG, request.getName()+": endpoint: " + endpoint);
        }
        // force application/json
        connection.setRequestProperty("Accept", "application/json, */*");
        connection.addRequestProperty("Accept-Encoding","gzip");

        final String jsonString;
        try {
        	InputStream inputStream = connection.getInputStream();
        	String contentEncoding = connection.getContentEncoding();
			if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
				inputStream = new GZIPInputStream(inputStream);
			}
        	
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            //before we go further, we must check to see if we were redirected.
            if (!request.getUri().getHost().equals(connection.getURL().getHost())
                    && !request.isTolerantOfUriRedirections()) {
                // then we were redirected and we can't tolerate it!!
                throw new UrlRedirectionException();
            }
            final StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            jsonString = jsonBuilder.toString();
        } finally {
            // Always close the connection.
            ((HttpURLConnection) connection).disconnect();
        }
        final long timeTaken = System.currentTimeMillis() - startTime;
        Log.d(Constants.LOG_TAG, request.getName()+ " took " + timeTaken + " milliseconds.");
        
        if (BuildConfig.DEBUG == false) {
			Tracker defaultTracker = GoogleAnalytics.getInstance(VHAApplication.getAppContext()).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createEvent("expedia_search", request.getName(), endpoint, (VHAApplication.selectedHotel == null) ? -1l: VHAApplication.selectedHotel.hotelId)
					    .set(Fields.CURRENCY_CODE, CommonParameters.currencyCode)
					    .build()
					   );
		}
        return jsonString;
    }

    /**
     * Performs an API request.
     * @param request contains all necessary data to execute a request and parse a response
     * @return The JSONObject that represents the content returned by the API
     * @throws IOException If there is a network issue, or the network stream cannot otherwise be read.
     * @throws JSONException If the response does not contain valid JSON
     * @throws EanWsError If the response contains an EanWsError element
     * @throws UrlRedirectionException If the network connection was unexpectedly redirected.
     * @throws URISyntaxException thrown if the URI cannot be built
     */
    private static JSONObject performApiRequest(final Request request)
            throws IOException, JSONException, EanWsError, UrlRedirectionException, URISyntaxException {
        final JSONObject response = new JSONObject(performApiRequestForString(request));
        if (response.has("EanWsError")) {
        	CommonParameters.customerSessionId = null;
        	throw EanWsError.fromJson(response.getJSONObject("EanWsError"));
        }
        return response;
    }

}
