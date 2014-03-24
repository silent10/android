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

import org.json.JSONObject;

/**
 * An address specifically suited for customer addresses, with some functionality specifying
 * the use of the address for the customer.
 */
public final class CustomerAddress extends Address {

    /**
     * The type of address that this address is for, Billing or Shipping.
     */
    public enum AddressType {
        /**
         * Should only happen if there is a new type of address.
         */
        UNKNOWN(Integer.MIN_VALUE),

        /**
         * What will be shown for a billing address.
         */
        BILLING(1),

        /**
         * If the address is a shipping address.
         */
        SHIPPING(2);

        /**
         * The id returned from the api that this object will be for.
         */
        public final int typeId;

        /**
         * The sole constructor for this enum.
         * @param typeId The id returned from the api.
         */
        AddressType(final int typeId) {
            this.typeId = typeId;
        }

        /**
         * Returns the appropriate address type from a particular integer.
         * @param typeId The id to find an address type.
         * @return The appropriate address type.
         */
        public static AddressType fromInt(final int typeId) {
            if (typeId > 2 || typeId < 1) {
                return UNKNOWN;
            }
            if (typeId == 1) {
                return BILLING;
            } else {
                return SHIPPING;
            }
        }
    }

    /**
     * Whether or not this address is a primary address associated with a customer.
     */
    public final boolean isPrimary;

    /**
     * Whether this is a billing or shipping address.
     */
    public final AddressType type;

    /**
     * The JSON-based constructor for this address. In addition to the normal address fields,
     * the json object must also have fields "isPrimary" and "type" as boolean and integer,
     * respectively.
     * @param object The JSONObject holding the appropriate fields.
     */
    public CustomerAddress(final JSONObject object) {
        super(object);
        this.isPrimary = object.optBoolean("isPrimary");
        this.type = AddressType.fromInt(object.optInt("type"));
    }
}