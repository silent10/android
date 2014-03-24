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
 * The rest of the information about a hotel, that is loaded using the InformationRequest.
 */
public final class HotelInformation {

    /**
     * The id of the hotel this extended information is associated with.
     */
    public final long hotelId;

    /**
     * The long description of this hotel.
     */
    public final String longDescription;

    /**
     * The list of hotel images for this hotel.
     */
    public final List<HotelImageTuple> images;

    /**
     * The sole constructor, enables the class to be immutable.
     * @param hotelId The id of the hotel that this information is for.
     * @param longDescription The long description of this hotel. Often contains embedded html.
     * @param images The list of images for this hotel.
     */
    public HotelInformation(final long hotelId, final String longDescription, final List<HotelImageTuple> images) {
        this.hotelId = hotelId;
        this.longDescription = longDescription;
        this.images = Collections.unmodifiableList(images);
    }

}
