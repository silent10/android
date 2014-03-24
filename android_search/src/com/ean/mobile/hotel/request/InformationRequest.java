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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.Html;
import android.util.Log;

import com.ean.mobile.Constants;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.HotelImageTuple;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;

/**
 * Uses getHotelInformation to get the rest of the hotel's information, including images
 * and the hotel's full description.
 */
public final class InformationRequest extends Request<HotelInformation> {

    /**
     * Gets the rest of the information about a hotel not included in previous calls.
     * @param hotelId The hotelId for which to gather more information.
     */
    public InformationRequest(final long hotelId) {

        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("hotelId", Long.toString(hotelId)),
            new BasicNameValuePair("options", "HOTEL_DETAILS,HOTEL_IMAGES")
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
    public HotelInformation consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject infoResp = jsonObject.getJSONObject("HotelInformationResponse");
        final JSONObject details = infoResp.getJSONObject("HotelDetails");
        final JSONArray images = infoResp.getJSONObject("HotelImages").getJSONArray("HotelImage");

        final String longDescription = Html.fromHtml(details.optString("propertyDescription")).toString();

        final List<HotelImageTuple> imageTuples = new ArrayList<HotelImageTuple>();

        JSONObject image;
        for (int i = 0; i < images.length(); i++) {
            image = images.getJSONObject(i);
            try {
                imageTuples.add(
                    new HotelImageTuple(new URL(image.optString("thumbnailUrl")),
                        new URL(image.optString("url")), image.optString("caption")));
            } catch (MalformedURLException me) {
                Log.e(Constants.LOG_TAG, "Unable to process JSON", me);
            }
        }

        CommonParameters.customerSessionId = infoResp.optString("customerSessionId");

        Log.d(Constants.LOG_TAG, "Found " + imageTuples.size() + " images");
        return new HotelInformation(infoResp.optLong("@hotelId"), longDescription, imageTuples);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("http", "api.ean.com", "/ean-services/rs/hotel/v3/info", getQueryString(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresSecure() {
        return false;
    }
}
