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
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.RoomOccupancy;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;

/**
 * The class to use to get specific availability of rooms for a particular hotel, occupancy, and occupancy dates.
 */
public final class RoomAvailabilityRequest extends Request<List<HotelRoom>> {

    /**
     * Gets the room availability for the specified information.
     *
     * THIS SHOULD NOT BE RUN ON THE MAIN THREAD. It is a long-running network process and so might cause
     * force close dialogs.
     *
     * @param hotelId The hotel to search for availability in.
     * @param room The singular room occupancy to search for.
     * @param arrivalDate The date of arrival.
     * @param departureDate The date of departure (from the hotel).
     */
    public RoomAvailabilityRequest(final long hotelId, final RoomOccupancy room,
            final LocalDate arrivalDate, final LocalDate departureDate) {

        this(hotelId, Collections.singletonList(room), arrivalDate, departureDate);
    }
    /**
     * Gets the room availability for the specified information.
     *
     * THIS SHOULD NOT BE RUN ON THE MAIN THREAD. It is a long-running network process and so might cause
     * force close dialogs otherwise.
     *
     * @param hotelId The hotel to search for availability in.
     * @param rooms The list of room occupancies to search for.
     * @param arrivalDate The date of arrival.
     * @param departureDate The date of departure (from the hotel).
     */

    public RoomAvailabilityRequest(final long hotelId, final List<RoomOccupancy> rooms,
            final LocalDate arrivalDate, final LocalDate departureDate) {

        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("hotelId", Long.toString(hotelId)),
            new BasicNameValuePair("includeDetails", "true"),
            new BasicNameValuePair("includeRoomImages", "true")
        );

        final List<NameValuePair> roomPairs = new ArrayList<NameValuePair>(rooms.size());
        for (RoomOccupancy occupancy : rooms) {
            roomPairs.add(new BasicNameValuePair("room" + (roomPairs.size() + 1),
                occupancy.asAbbreviatedRequestString()));
        }

        final List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.addAll(getBasicUrlParameters(arrivalDate, departureDate));
        urlParameters.addAll(requestParameters);
        urlParameters.addAll(roomPairs);

        setUrlParameters(urlParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HotelRoom> consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject response = jsonObject.getJSONObject("HotelRoomAvailabilityResponse");

        if (response.has("EanWsError")) {
            throw EanWsError.fromJson(response.getJSONObject("EanWsError"));
        }

        CommonParameters.customerSessionId = response.optString("customerSessionId");

        final List<HotelRoom> hotelRooms;
        if (response.has("HotelRoomResponse")) {
            // we know that it has HotelRoomResponse, just don't know if it'll be
            // parsed as an object or as an array. If there's only one in the collection,
            // it'll be parsed as a singular object, otherwise it'll be an array.
            final LocalDate arrivalDate = LocalDate.parse(response.getString("arrivalDate"), DATE_TIME_FORMATTER);
            if (response.optJSONArray("HotelRoomResponse") != null) {
                final JSONArray hotelRoomResponse = response.optJSONArray("HotelRoomResponse");
                hotelRooms = HotelRoom.parseRoomRateDetails(hotelRoomResponse, arrivalDate);
            } else {
                final JSONObject hotelRoomResponse = response.optJSONObject("HotelRoomResponse");
                hotelRooms = HotelRoom.parseRoomRateDetails(hotelRoomResponse, arrivalDate);
            }
        } else {
            hotelRooms = Collections.emptyList();
        }

        return hotelRooms;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("http", "api.ean.com", "/ean-services/rs/hotel/v3/avail", getQueryString(), null);
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
		return "RoomAvailabilityRequest";
	}
}
