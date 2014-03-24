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

import java.util.Collections;

import java.util.HashSet;
import java.util.Set;

/**
 * This class wraps supplier types so that we can univerally reference them
 * as opposed to dealing with ids and code as the API returns them.
 *
 * Id's come from http://developer.ean.com/docs/read/request_itinerary
 * Letters come from http://developer.ean.com/docs/read/room_avail
 */
public enum  SupplierType {

    /**
     * Expedia supplier type.
     */
    EXPEDIA("E", 2, 9, 13),

    /**
     * Sabre supplier type.
     */
    SABRE("S", 3),

    /**
     * Venere supplier type.
     */
    VENERE("V", 14),

    /**
     * Worldspan supplier type.
     */
    WORLDSPAN("W", 10);

    /**
     * Single letter code for supplier type.
     */
    public final String code;

    /**
     * Set of ids used for supplier type.
     */
    private final Set<Integer> ids;

    /**
     * Constructor used to create one.
     * @param code Letter code for supplier.
     * @param ids Set of supplier ids.
     */
    private SupplierType(final String code, final Integer... ids) {
        final Set<Integer> idSet = new HashSet<Integer>(ids.length);
        Collections.addAll(idSet, ids);
        this.ids = Collections.unmodifiableSet(idSet);

        this.code = code;
    }

    /**
     * Looks for a matching supplier for a supplier id.
     * @param supplierId Supplier id passed from the api.
     * @return SupplierType if there was a match, otherwise null.
     */
    public static SupplierType getById(final int supplierId) {
        for (SupplierType supplierType : values()) {
            if (supplierType.ids.contains(supplierId)) {
                return supplierType;
            }
        }
        return null;
    }

    /**
     * Looks for a matching supplier for a supplier code.
     * @param supplierCode Supplier code passed from the api.
     * @return SupplierType if there was a match, otherwise null.
     */
    public static SupplierType getByCode(final String supplierCode) {
        for (SupplierType supplierType : values()) {
            if (supplierType.code.equals(supplierCode)) {
                return supplierType;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s [%s]", this.name(), this.code);
    }
}
