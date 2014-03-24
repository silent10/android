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
import java.util.HashMap;
import java.util.Map;

/**
 * The various statuses a HotelConfirmation can hold once booked.
 * Indicates the status of the reservation in the supplier system at the time of booking.
 * Anticipate appropriate customer messaging for all non-confirmed values.
 */
public enum ConfirmationStatus {

    /**
     * Encountered when an unknown confirmation status is encountered. Should never happen.
     */
    UNKNOWN(""),
    /**
     * The normal state of a confirmation. When everything has gone right, a confirmation will say confirmed.
     */
    CONFIRMED("CF"),
    /**
     * A cancelled confirmation.
     */
    CANCELLED("CX"),
    /**
     * Unconfirmed. Usually due to the property being sold out. Agent will follow up.
     * Most cases result in customer being advised to select other property when agent cannot obtain a reservation.
     */
    UNCONFIRMED("UC"),
    /**
     * Pending Supplier. Agent will follow up with customer when confirmation number is available.
     */
    PENDING_SUPPLIER("PS"),
    /**
     * Error. Agent attention needed. Agent will follow up.
     */
    ERROR("ER"),
    /**
     * Deleted Itinerary (Usually a test or failed booking).
     */
    DELETED("DT");

    private static final Map<String, ConfirmationStatus> STATUSES;

    static {
        final Map<String, ConfirmationStatus> statuses = new HashMap<String, ConfirmationStatus>();
        statuses.put(CONFIRMED.code, CONFIRMED);
        statuses.put(CANCELLED.code, CANCELLED);
        statuses.put(UNCONFIRMED.code, UNCONFIRMED);
        statuses.put(PENDING_SUPPLIER.code, PENDING_SUPPLIER);
        statuses.put(ERROR.code, ERROR);
        statuses.put(DELETED.code, DELETED);
        STATUSES = Collections.unmodifiableMap(statuses);
    }

    final String code;

    /**
     * The sole constructor, sets the string code that this status applies to.
     * @param code The code for this status.
     */
    ConfirmationStatus(final String code) {
        this.code = code;
    }

    /**
     * Gets a ConfirmationStatus object from a string. Assumes no whitespace and fully uppercase.
     * @param code The code returned from the API
     * @return The ConfirmationStatus object represented by the string.
     */
    public static ConfirmationStatus fromString(final String code) {
        if (code == null || code.length() != 2) {
            return UNKNOWN;
        } else {
            return STATUSES.get(code);
        }
    }
}
