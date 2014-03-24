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

package com.ean.mobile.hotel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ean.mobile.Name;

/**
 * A container for a room for booking.
 */
public final class ReservationRoom {

    private static final int NUMBER_OF_PAIRS_PER_ROOM = 9;

    /**
     * The name of the individual who will check in this room.
     */
    public final Name checkInName;

    /**
     * The bedtypeId for this room occupancy. Found in the Room object of a list request.
     */
    public final String bedTypeId;

    /**
     * The smoking preference for this room. Found in the Room object of a list request.
     */
    public final String smokingPreference;

    /**
     * {@link HotelRoom#roomTypeCode}.
     */
    public final String roomTypeCode;

    /**
     * {@link HotelRoom#rateCode}.
     */
    public final String rateCode;

    /**
     * {@link HotelRoom#rate}.
     */
    public final Rate rate;

    /**
     * The stated occupancy for this room.
     */
    public final RoomOccupancy occupancy;

    /**
     * The primary constructor setting the final variables in this class.
     * @param checkInName see {@link ReservationRoom#checkInName}.
     * @param bedTypeId see {@link ReservationRoom#bedTypeId}.
     * @param smokingPreference see {@link ReservationRoom#smokingPreference}.
     * @param roomTypeCode see {@link HotelRoom#roomTypeCode}.
     * @param rateCode see {@link HotelRoom#rateCode}.
     * @param rate see {@link HotelRoom#rate}.
     * @param occupancy Stated occupancy for the room.
     */
    public ReservationRoom(final Name checkInName, final String bedTypeId, final String smokingPreference,
            final String roomTypeCode, final String rateCode, final Rate rate, final RoomOccupancy occupancy) {
        this.checkInName = checkInName;
        this.bedTypeId = bedTypeId;
        this.smokingPreference = smokingPreference;
        this.rateCode = rateCode;
        this.roomTypeCode = roomTypeCode;
        this.rate = rate;
        this.occupancy = occupancy;
    }

    /**
     * The primary constructor setting the final variables in this class.
     * @param checkInName see {@link ReservationRoom#checkInName}.
     * @param bedTypeId see {@link ReservationRoom#bedTypeId}.
     * @param smokingPreference see {@link ReservationRoom#smokingPreference}.
     * @param roomTypeCode see {@link HotelRoom#roomTypeCode}.
     * @param rateCode see {@link HotelRoom#rateCode}.
     * @param rate see {@link HotelRoom#rate}.
     * @param numberOfAdults The number of adults in this occupancy.
     * @param childAges The list of children's ages for this room.
     */
    public ReservationRoom(final Name checkInName, final String bedTypeId, final String smokingPreference,
            final String roomTypeCode, final String rateCode, final Rate rate, final int numberOfAdults,
            final List<Integer> childAges) {
        this(checkInName, bedTypeId, smokingPreference, roomTypeCode, rateCode, rate,
            new RoomOccupancy(numberOfAdults, childAges));
    }

    /**
     * The abbreviated constructor taking a room as part of the constructor rather than the individual bits.
     * @param checkInName see {@link ReservationRoom#checkInName}.
     * @param room The HotelRoom object holding the bedTypeId, smokingPreference, roomTypeCode,
     *             and rateCode for this occupancy.
     * @param selectedBedTypeId The particular bedTypeId selected for the room. Should be one of those available
     *                          in "room". See {@link HotelRoom#bedTypes}.
     * @param occupancy The stated occupancy of the room.
     */
    public ReservationRoom(final Name checkInName, final HotelRoom room, final String selectedBedTypeId,
            final RoomOccupancy occupancy) {
        this(checkInName, selectedBedTypeId, room.smokingPreference, room.roomTypeCode, room.rateCode, room.rate,
            occupancy);
    }

    /**
     * The constructor used when constructing from a reservation object.
     * @param object A JSONObject holding at least the fields used by this object to construct itself.
     */
    public ReservationRoom(final JSONObject object) {
        this.bedTypeId = object.optString("bedTypeId");
        this.smokingPreference = object.optString("smokingPreference");
        this.rate = Rate.parseFromRateInformations(object).get(0);
        this.checkInName = new Name(object);
        this.occupancy = new RoomOccupancy(object);
        this.roomTypeCode = object.optString("roomTypeCode");
        this.rateCode = object.optString("rateCode");
    }

    /**
     * Gets the list of NameValuePairs for a list of room occupancies so that they can be easily added to a request.
     * @param rooms The rooms to make into NameValuePairs.
     * @return The NameValuePairs requested, in the natural order of the list, applying room[position] as the
     * name for each NameValuePair
     */
    public static List<NameValuePair> asNameValuePairs(final List<ReservationRoom> rooms) {
        final List<NameValuePair> pairs = new ArrayList<NameValuePair>(rooms.size() * NUMBER_OF_PAIRS_PER_ROOM);
        String rateKey = null;
        String thisRateKey;
        boolean singularRateKey = true;
        for (ReservationRoom room : rooms) {
            thisRateKey = room.rate.getRateKeyForOccupancy(room.occupancy);
            if (rateKey == null) {
                rateKey = thisRateKey;
            } else if (!thisRateKey.equals(rateKey)) {
                singularRateKey = false;
                break;
            }
        }

        int roomNumber = 1;
        for (ReservationRoom room : rooms) {
            final String roomId = "room" + roomNumber;

            pairs.add(new BasicNameValuePair(roomId, room.occupancy.asAbbreviatedRequestString()));
            pairs.add(new BasicNameValuePair(roomId + "FirstName", room.checkInName.first));
            pairs.add(new BasicNameValuePair(roomId + "LastName", room.checkInName.last));

            pairs.add(new BasicNameValuePair(roomId + "BedTypeId", room.bedTypeId));
            pairs.add(new BasicNameValuePair(roomId + "SmokingPreference", room.smokingPreference));

            if (rooms.size() == 1) {
                // then this is a singular room and the codes must be put in at the root.
                pairs.add(new BasicNameValuePair("roomTypeCode", room.roomTypeCode));
                pairs.add(new BasicNameValuePair("rateCode", room.rateCode));
                pairs.add(new BasicNameValuePair("chargeableRate", room.rate.chargeable.getTotal().toString()));
                pairs.add(new BasicNameValuePair("rateKey", room.rate.getRateKeyForOccupancy(room.occupancy)));
            } else {
                if (singularRateKey && roomNumber == 1) {
                    // In this case, we have multiple identical rooms and we must modify the keys accordingly.

                    // since all rooms are identical (have the same rate key), then all rooms have the same
                    // chargeable rate, therefore we can simply multiply the chargeable rate by the number
                    // of rooms for the total chargeable rate.
                    final BigDecimal chargeable
                        = room.rate.chargeable.getTotal().multiply(new BigDecimal(rooms.size()));
                    pairs.add(new BasicNameValuePair("roomTypeCode", room.roomTypeCode));
                    pairs.add(new BasicNameValuePair("rateCode", room.rateCode));
                    pairs.add(new BasicNameValuePair("chargeableRate", chargeable.toString()));
                    pairs.add(new BasicNameValuePair("rateKey", room.rate.getRateKeyForOccupancy(room.occupancy)));
                } else if (!singularRateKey) {
                    // if we're not a singular rate key, then we are a multiple room TYPE booking and require some
                    // special handling.
                    pairs.add(new BasicNameValuePair(roomId + "RoomTypeCode", room.roomTypeCode));
                    pairs.add(new BasicNameValuePair(roomId + "RateCode", room.rateCode));
                    pairs.add(new BasicNameValuePair(roomId + "ChargeableRate",
                        room.rate.chargeable.getTotal().toString()));
                    pairs.add(new BasicNameValuePair(roomId + "RateKey",
                        room.rate.getRateKeyForOccupancy(room.occupancy)));
                }
            }
            roomNumber++;

        }

        return Collections.unmodifiableList(pairs);
    }

    /**
     * Loads a ReservationRoom from a JSONObject which has a Room parameter which has the appropriate other fields
     * inside.
     * @param roomGroup A JSONObject which usually has a name of RoomGroup and has an array of Room fields.
     * @return The deserialized list.
     */
    public static List<ReservationRoom> fromJson(final JSONObject roomGroup) {
        final List<ReservationRoom> rooms;
        if (roomGroup.optJSONArray("Room") != null) {
            final JSONArray roomsJSON = roomGroup.optJSONArray("Room");
            rooms = new ArrayList<ReservationRoom>(roomsJSON.length());
            for (int i = 0; i < roomsJSON.length(); i++) {
                rooms.add(new ReservationRoom(roomsJSON.optJSONObject(i)));
            }
        } else {
            rooms = Collections.singletonList(new ReservationRoom(roomGroup.optJSONObject("Room")));
        }
        return rooms;
    }
}