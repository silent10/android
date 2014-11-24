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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.evature.util.ExternalIpAddressGetter;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.SettingsAPI;

/**
 * The base class for all of the API requests that are implemented. Provides some easy-to use methods for performing
 * requests.
 * @param <T> a response object used to store converted JSON data
 */
public abstract class Request<T> {

    /**
     * A formatter to use for all date/string conversions.
     */
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("MM/dd/yyyy");

    private List<NameValuePair> urlParameters;

    /**
     * Parses the specified JSON and returns the appropriate object containing the response data.
     * @param jsonObject a raw JSON response
     * @return an object representing the converted JSON data
     * @throws JSONException thrown if the provided JSON data cannot be parsed
     * @throws EanWsError thrown if an error message is included in the JSON data
     */
    public abstract T consume(final JSONObject jsonObject) throws JSONException, EanWsError;

    /**
     * Builds and returns a valid URI used to contact the EAN API.
     * @return a valid URI
     * @throws URISyntaxException thrown if the URI cannot be built
     */
    public abstract URI getUri() throws URISyntaxException;

    /**
     * Signifies if the particular implementation is tolerant of Uri Redirections. If the implementation
     * is tolerant of this case, RequestProcessor.run will not throw UriRedirectionExceptions and instead
     * return the data anyway. The implementation of consume should then be tolerant of strange inputs and
     * not throw deserialization exceptions, even in cases where completely unexpected data is returned.
     * @return Whether or not this implementation is tolerant of uri redirection. Default is false.
     */
    public boolean isTolerantOfUriRedirections() {
        return false;
    }

    /**
     * Return true if the implemented request should use a secure connection.
     * @return true if secure, false if not
     */
    public abstract boolean requiresSecure();
    
    
	public abstract String getName();


    /**
     * Gets the url parameters that will need to be present for every request.
     * @param arrivalDate The arrival date for this request.
     * @param departureDate The departure date for this request.
     * @return The above parameters plus the cid, apikey, minor rev, and customer user agent url parameters.
     */
    public static List<NameValuePair> getBasicUrlParameters(
            final LocalDate arrivalDate, final LocalDate departureDate) {
    	
    	CommonParameters.customerIpAddress = ExternalIpAddressGetter.getExternalIpAddr();
    	CommonParameters.currencyCode = SettingsAPI.getCurrencyCode(VHAApplication.getAppContext());
    	
        final List<NameValuePair> params = CommonParameters.asNameValuePairs();
        if (arrivalDate != null) {
            params.add(new BasicNameValuePair("arrivalDate", DATE_TIME_FORMATTER.print(arrivalDate)));
        }
        if (departureDate != null) {
            params.add(new BasicNameValuePair("departureDate", DATE_TIME_FORMATTER.print(departureDate)));
        }
        return params;
    }

    /**
     * Creates the query portion of a URI based on a list of NameValuePairs.
     * @return The requested string.
     */
    protected String getQueryString() {
        final List<NameValuePair> params = getUrlParameters();
        String queryString = null;
        if (params != null && !params.isEmpty()) {
            final StringBuilder sb = new StringBuilder(params.size() * 10);
            for (NameValuePair param : params) {
                if (param == null) {
                    continue;
                }
                sb.append(param.getName());
                sb.append("=");
                sb.append(param.getValue() == null ? "" : param.getValue());
                sb.append("&");
            }
            String potentialQueryString = sb.toString();
            if (potentialQueryString.length() == 0) {
                potentialQueryString = null;
            } else if (potentialQueryString.endsWith("&")) {
                potentialQueryString = potentialQueryString.substring(0, potentialQueryString.length() - 1);
            }
            // URLEncoder.encode cannot be used since it encodes things in a way the api does not expect, particularly
            // dates.
            queryString = potentialQueryString;
        }
        return queryString;
    }

    /**
     * Gets the url parameters that will need to be present for every request.
     * @return the cid, apikey, locale, currency code, minor rev, and customer user agent url parameters.
     */
    public static List<NameValuePair> getBasicUrlParameters() {
        return getBasicUrlParameters(null, null);
    }

    /**
     * Getter method for urlParameters.
     * @return urlParameters
     */
    public List<NameValuePair> getUrlParameters() {
        return urlParameters;
    }

    /**
     * Setter method for urlParameters.
     * @param urlParameters the new value for urlParameters
     */
    public void setUrlParameters(final List<NameValuePair> urlParameters) {
        this.urlParameters = urlParameters;
    }
}
