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
import java.util.List;

/**
 * The holder for a list of hotels, used as the return value from the list call.
 */
public final class HotelList {

    /**
     * The key used to paginate through multiple parts of a larger list request.
     */
    public final String cacheKey;

    /**
     * The server where the cached request lies.
     */
    public final String cacheLocation;

    /**
     * The ID also used to paginate through cached requests.
     */
    public final String customerSessionId;

    /**
     * The total number of Hotel objects able to be retrieved by the request that started this.
     */
    public final int totalNumberOfResults;

    /**
     * Holds the hotel info objects from the api response.
     */
    public final List<Hotel> hotels;
    
    /**
     * true if more results are available for follow-up request
     */
    public final boolean moreResults;

    /**
     * The main constructor for this class. Maps to ArrayList(Collection c) and sets the other fields
     * of this class as well.
     * @param hotels The hotels with which to initially populate this list.
     * @param cacheKey The cache key to set.
     * @param cacheLocation The cache location to set.
     * @param customerSessionId The session to set.
     * @param totalNumberOfResults The total number of results that the request that created this
     *                             HotelList can return.
     * @param moreResults True if more results are available for follow-up request                    
     */
    public HotelList(final List<Hotel> hotels,
                     final String cacheKey, final String cacheLocation,
                     final String customerSessionId, final int totalNumberOfResults,
                     final boolean moreResults) {
        this.hotels = Collections.unmodifiableList(hotels);
        this.cacheKey = cacheKey;
        this.cacheLocation = cacheLocation;
        this.customerSessionId = customerSessionId;
        this.totalNumberOfResults = totalNumberOfResults;
        this.moreResults = moreResults;
    }

    /**
     * Similar to {@link java.util.Collections#emptyList()} but for HotelList.
     * @return An empty hotel info list whose fields are all null.
     */
    public static HotelList emptyList() {
        return new HotelList(Collections.<Hotel>emptyList(), null, null, null, 0, false);
    }
}
