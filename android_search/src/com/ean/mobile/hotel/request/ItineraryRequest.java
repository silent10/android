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

package com.ean.mobile.hotel.request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Itinerary;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;

/**
 * Used to retrieve and parse itinerary information from the EAN API.
 */
public class ItineraryRequest extends Request<Itinerary> {

    /**
     * Uses the EAN API to search for hotels in the given destination using http requests.
     *
     * @param itineraryId the ID of the itinerary to retrieve.
     * @param emailAddress the e-mail address associated with the itinerary.
     */
    public ItineraryRequest(final long itineraryId, final String emailAddress) {
        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("itineraryId", String.valueOf(itineraryId)),
            new BasicNameValuePair("email", emailAddress)
        );

        final List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.addAll(getBasicUrlParameters());
        urlParameters.addAll(requestParameters);
        setUrlParameters(urlParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Itinerary consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject response = jsonObject.getJSONObject("HotelItineraryResponse");
        if (response.has("EanWsError")) {
            throw EanWsError.fromJson(response.getJSONObject("EanWsError"));
        }

        CommonParameters.customerSessionId = response.optString("customerSessionId");

        return new Itinerary(response.getJSONObject("Itinerary"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("http", "api.ean.com", "/ean-services/rs/hotel/v3/itin", getQueryString(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresSecure() {
        return false;
    }

}
