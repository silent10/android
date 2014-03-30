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

package com.ean.mobile;

import java.math.BigDecimal;

import org.json.JSONObject;

/**
 * An address implementation that has latitude and longitude included.
 */
public final class LatLongAddress extends Address {

    /**
     * The latitude of the address.
     */
    public final BigDecimal latitude;

    /**
     * The longitude for this address.
     */
    public final BigDecimal longitude;

    /**
     * Code for accuracy of the provided coordinates. May be null.
     */
    public final String coordinateAccuracyCode;

    /**
     * Creates an address object which includes latitude and longitude parts.
     * @param object A JSONObject which has all of the necessary fields for this type of object.
     */
    public LatLongAddress(final JSONObject object) {
        super(object);
        //Defaults to 0,0 lat,long to prevent null pointer exceptions.
        this.latitude = new BigDecimal(object.optString("latitude", "0"));
        this.longitude = new BigDecimal(object.optString("longitude", "0"));
        this.coordinateAccuracyCode = object.optString("coordinateAccuracyCode");
    }
}
