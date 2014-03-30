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

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This holds information about each of the images to be displayed in the hotel full information. This includes
 * inner views, exterior views and so forth.
 */
public final class HotelImageTuple {
    /**
     * The protocol with which to fetch images.
     */
    private static final String IMAGE_PROTOCOL = "http";

    /**
     * The host from which to fetch images.
     */
    private static final String IMAGE_HOST = "images.travelnow.com";

    /**
     * The URL from whence to retrieve the thumbnail.
     */
    public final URL thumbnailUrl;

    /**
     * The URL from whence to retrieve the main image.
     */
    public final URL mainUrl;

    /**
     * The caption for the image.
     */
    public final String caption;

    /**
     * Constructs the object with final values.
     * @param thumbnailUrl The URL of the thumbnail image.
     * @param mainUrl The URL of the main image.
     * @param caption The caption for the image.
     */
    public HotelImageTuple(final URL thumbnailUrl, final URL mainUrl, final String caption) {
        this.thumbnailUrl = thumbnailUrl;
        this.mainUrl = mainUrl;
        this.caption = caption;
    }

    /**
     * Constructs the object with final values.
     * @param partialThumbnailUrl The partial URL of the thumbnail image.
     *                            Will be converted to use the default image host and protocol for the actual urls.
     * @param partialMainUrl The partial URL of the main image.
     *                       Will be converted to use the default image host and protocol for the actual urls.
     * @param caption The caption for the image.
     * @throws MalformedURLException If either partial url cannot be turned into a full url. Not affected by
     * nulls. In the case of nulls, the particular URL will be null.
     */
    public HotelImageTuple(final String partialThumbnailUrl, final String partialMainUrl, final String caption)
            throws MalformedURLException {
        this.thumbnailUrl = getFullImageUrl(partialThumbnailUrl);
        this.mainUrl = getFullImageUrl(partialMainUrl);
        this.caption = caption;
    }


    /**
     * Gets the full image url, based simply on the partial url which does not include the protocol or host.
     * @param partial The partial url, excluding the host and previous
     * @return The full url to the image.
     * @throws java.net.MalformedURLException If the default IMAGE_PROTOCOL and IMAGE_HOST, combined with partial do not
     *  create a valid URL.
     */
    private static URL getFullImageUrl(final String partial) throws MalformedURLException {
        return partial == null ? null : new URL(IMAGE_PROTOCOL, IMAGE_HOST, partial);
    }
}
