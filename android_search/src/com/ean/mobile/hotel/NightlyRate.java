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
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Holds data regarding a nightly rate for a room.
 */
public final class NightlyRate {

    /**
     * The type of promotion returned by the api for this nightly rate, either standard or mobile.
     */
    public enum PromoType {
        /**
         * Standard promotion. This is for all promos besides mobile-specific promos.
         */
        STANDARD,

        /**
         * Mobile specific promotions.
         */
        MOBILE;

        /**
         * Gets a promotype from a capital case (or otherwise) string.
         * @param value The promo type to get.
         * @return The promo type represented by the string, or null if there are no matching promo types.
         */
        public static PromoType fromString(final String value) {
            if (value == null) {
                return null;
            }
            try {
                return PromoType.valueOf(value.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
    }

    /**
     * Whether or not this rate is a promotional rate.
     */
    public final boolean promo;

    /**
     * The type of promo that this nightly rate has, either standard or mobile.
     */
    public final PromoType promoType;

    /**
     * The actual rate for this nightly rate.
     */
    public final BigDecimal rate;

    /**
     * The base rate (pre-promo) for this nightly rate.
     */
    public final BigDecimal baseRate;

    /**
     * The main constructor, building objects from a JSONObject.
     * @param nightlyRateJson The JSON representing this nightly rate.
     */
    public NightlyRate(final JSONObject nightlyRateJson) {
        this.promo = nightlyRateJson.optBoolean("@promo");
        this.rate = new BigDecimal(nightlyRateJson.optString("@rate"));
        this.baseRate = new BigDecimal(nightlyRateJson.optString("@baseRate"));
        if (this.promo) {
            this.promoType = PromoType.fromString(nightlyRateJson.optString("promoType"));
        } else {
            this.promoType = null;
        }
    }

    /**
     * Constructs a list of nightly rates from a JSONArray of nightly rates.
     * @param nightlyRatesJson The JSON representing the array of nightly rates
     * @return The NightlyRate objects parsed from the array.
     */
    public static List<NightlyRate> parseNightlyRates(final JSONArray nightlyRatesJson) {
        final List<NightlyRate> nightlyRates = new ArrayList<NightlyRate>(nightlyRatesJson.length());
        for (int j = 0; j < nightlyRatesJson.length(); j++) {
            nightlyRates.add(new NightlyRate(nightlyRatesJson.optJSONObject(j)));
        }
        return nightlyRates;
    }

    /**
     * Constructs a singleton list of NightlyRates from the JSONObject of nightly rate.
     * @param nightlyRatesJson The JSON representing the nightly rate
     * @return The NightlyRate object parsed from the object.
     */
    public static List<NightlyRate> parseNightlyRates(final JSONObject nightlyRatesJson) {
        return Collections.singletonList(new NightlyRate(nightlyRatesJson.optJSONObject("NightlyRate")));
    }
}

