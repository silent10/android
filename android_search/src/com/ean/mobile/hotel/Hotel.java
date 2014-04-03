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
import java.math.RoundingMode;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.text.Html;

import com.ean.mobile.LatLongAddress;
import com.evaapis.android.EvatureLocationUpdater;
import com.virtual_hotel_agent.search.VHAApplication;

/**
 * The holder for information about a particular hotel.
 */
public final class Hotel {

    /**
     * The name of this hotel.
     */
    public final String name;

    /**
     * The short description of this hotel.
     */
    public final String shortDescription;

    /**
     * The location description of this hotel.
     */
    public final String locationDescription;

    /**
     * The star rating of this hotel.
     */
    public final BigDecimal starRating;

    /**
     * The main HotelImageTuple for this hotel.
     */
    public final HotelImageTuple mainHotelImageTuple;

    /**
     * The currency code used for the price of this hotel.
     */
    public final String currencyCode;

    /**
     * The ean id of this hotel.
     */
    public final long hotelId;

    /**
     * The street address of this hotel.
     */
    public final LatLongAddress address;

    /**
     * The type of supplier for the hotel.
     */
    public final SupplierType supplierType;

    /**
     * The high price of the hotel, in the currency specified by {currencyCode}.
     */
    public final BigDecimal highPrice;

    /**
     * The low price found for the hotel, in the currency specified by {currencyCode}.
     */
    public final BigDecimal lowPrice;
    
    public final int amenityMask;
    
    private double distance = -2;

    /**
     * The constructor that constructs the hotel info from a JSONObject.
     * @param hotelSummary The object holding the hotel's info.
     * @throws JSONException If there is a problem with the JSON objects
     * @throws MalformedURLException If the thumbnail url is not correctly formatted.
     */
    public Hotel(final JSONObject hotelSummary) throws JSONException, MalformedURLException {
        this.name = Html.fromHtml(hotelSummary.optString("name")).toString();
        this.hotelId = hotelSummary.optLong("hotelId");
        this.address = new LatLongAddress(hotelSummary);
        this.shortDescription =  Html.fromHtml(hotelSummary.optString("shortDescription")).toString();
        this.locationDescription = Html.fromHtml(hotelSummary.optString("locationDescription")).toString();
        this.starRating = parseStarRating(hotelSummary.optString("hotelRating"));
        final String thumbnailString = hotelSummary.optString("thumbNailUrl").replace("_t.jpg", "_n.jpg");
        this.mainHotelImageTuple = new HotelImageTuple(thumbnailString, null, null);
        this.highPrice = new BigDecimal(hotelSummary.getDouble("highRate")).setScale(2, RoundingMode.HALF_EVEN);
        this.lowPrice = new BigDecimal(hotelSummary.getDouble("lowRate")).setScale(2, RoundingMode.HALF_EVEN);
        this.currencyCode = hotelSummary.optString("rateCurrencyCode");
        this.supplierType = SupplierType.getByCode(hotelSummary.optString("supplierType"));
        this.amenityMask = hotelSummary.optInt("amenityMask", 0);
    }

    /**
     * Gets the star rating (a BigDecimal) from the string representation of the star rating.
     * @param starRating The String representation of a star rating.
     * @return The BigDecimal representation of a star rating.
     */
    public static BigDecimal parseStarRating(final String starRating) {
        return starRating == null || "".equals(starRating) ? BigDecimal.ZERO : new BigDecimal(starRating).setScale(1);
    }

    /**
     * {@inheritDoc}
     *
     * Simply returns the name field of this object.
     */
    @Override
    public String toString() {
        return this.name;
    }

	public double getDistanceFromMe() {
		if (distance == -2) {
			double hotelLatitude = address.latitude.doubleValue();
			double hotelLongitude = address.longitude.doubleValue();
			double myLongitude, myLatitude;
			try {
				myLongitude = EvatureLocationUpdater.getLongitude();
				if (myLongitude != EvatureLocationUpdater.NO_LOCATION) {
					myLatitude = EvatureLocationUpdater.getLatitude();
					float[] results = new float[3];
					Location.distanceBetween(myLatitude, myLongitude,
							hotelLatitude, hotelLongitude, results);
					if (results != null && results.length > 0)
						distance = results[0] / 1000;
				}
	
			} catch (Exception e2) {
				VHAApplication.logError("Hotel", "Error calculating distance", e2);
				distance = -1;
			}
		}
		return distance;
	}
}
