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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelList;
import com.ean.mobile.hotel.RoomOccupancy;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;
import com.virtual_hotel_agent.search.VHAApplication;

/**
 * The most useful method gets the List of hotels based on the search parameters, particularly the destination passed.
 */
public final class ListRequest extends Request<HotelList> {

    private static final String NUMBER_OF_RESULTS = "20";

    /**
     * Uses the EAN API to search for hotels in the given destination using http requests.
     *
     * @param destination The destination to search for hotel availability.
     * @param occupancy The stated occupancy to search for.
     * @param arrivalDate The arrival date of the request.
     * @param departureDate The departure date of the request.
     */
    public ListRequest(final String destination, final RoomOccupancy occupancy,
            final LocalDate arrivalDate, final LocalDate departureDate) {

        this(destination, Collections.singletonList(occupancy), arrivalDate, departureDate);
    }
    /**
     * Uses the EAN API to search for hotels in the given destination using http requests.
     *
     * @param destination The destination to search for hotel availability.
     * @param occupancies The stated occupancy of each room to search for.
     * @param arrivalDate The arrival date of the request.
     * @param departureDate The departure date of the request.
     */
    public ListRequest(final String destination, final List<RoomOccupancy> occupancies,
            final LocalDate arrivalDate, final LocalDate departureDate) {

        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("destinationString", destination),
            new BasicNameValuePair("numberOfResults", NUMBER_OF_RESULTS),
            new BasicNameValuePair("supplierType", "E")  // Temporary until Hotel collect handling is finished
        );

        final List<NameValuePair> roomParameters = new ArrayList<NameValuePair>(occupancies.size());

        int roomNumber = 1;
        for (RoomOccupancy occupancy : occupancies) {
            roomParameters.add(new BasicNameValuePair("room" + roomNumber, occupancy.asAbbreviatedRequestString()));
            roomNumber++;
        }

        final List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.addAll(getBasicUrlParameters(arrivalDate, departureDate));
        urlParameters.addAll(requestParameters);
        urlParameters.addAll(roomParameters);

        setUrlParameters(urlParameters);
    }
    
    public ListRequest(List<NameValuePair> urlParameters) {
    	setUrlParameters(urlParameters);
    }

    /**
     * Loads more results into a HotelList so pagination can be supported.
     * @param cacheKey Cache key from previous request
     * @param cacheLocation Cache location from previous request
     */
    public ListRequest(final String cacheKey, final String cacheLocation) {

        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("cacheKey", cacheKey),
            new BasicNameValuePair("cacheLocation", cacheLocation)
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
    public HotelList consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject response = jsonObject.getJSONObject("HotelListResponse");

        if (response.has("EanWsError")) {
            throw EanWsError.fromJson(response.getJSONObject("EanWsError"));
        }

        final String newCacheKey = response.optString("cacheKey");
        final String newCacheLocation = response.optString("cacheLocation");
        final String outgoingCustomerSessionId = response.optString("customerSessionId");
        final boolean hasMoreResults = response.optBoolean("moreResultsAvailable");
        final int totalNumberOfResults = response.optJSONObject("HotelList").optInt("@activePropertyCount");

        final JSONArray newHotelJson = response.getJSONObject("HotelList").getJSONArray("HotelSummary");
        final List<Hotel> newHotels = new ArrayList<Hotel>(newHotelJson.length());
        for (int i = 0; i < newHotelJson.length(); i++) {
            try {
                newHotels.add(new Hotel(newHotelJson.getJSONObject(i)));
            } catch (MalformedURLException me) {
                VHAApplication.logError("Unable to process JSON", me.getMessage());
            }
        }

        CommonParameters.customerSessionId = outgoingCustomerSessionId;

        return new HotelList(newHotels,
            newCacheKey, newCacheLocation, outgoingCustomerSessionId, totalNumberOfResults, hasMoreResults);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("http", "api.ean.com", "/ean-services/rs/hotel/v3/list", getQueryString(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresSecure() {
        return false;
    }
	@Override
	public String getName() {
		return "ListRequest";
	}

}
