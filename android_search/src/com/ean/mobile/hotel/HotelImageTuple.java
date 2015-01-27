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
import java.util.regex.Pattern;

import com.evature.util.DLog;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.search.VHAApplication;

import android.graphics.Bitmap;
import android.print.PrintAttributes.Resolution;

/**
 * This holds information about each of the images to be displayed in the hotel full information. This includes
 * inner views, exterior views and so forth.
 */
public final class HotelImageTuple {
    
	private static final String TAG = "HotelImageTuple";
    
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
    public URL thumbnailUrl;

    /**
     * The URL from whence to retrieve the main image.
     */
    public URL mainUrl;

    /**
     * The caption for the image.
     */
    public final String caption;
    
    public final int category;

    public static final String[] CATEGORY_TITLES = {
    	"Unknown",   "Exterior",   "Lobby",   "Guest Room", "Recreation", "Property Choice",
    	"Property Choice","Collage", "Restaurant", "Interior", "Outdoor Pool", "Indoor Pool", "Private Pool",
    	"Children's Pool", "Sauna", "Hot Tub", "Marina", "Beach", "Poolside Bar", "Lounge/Bar", "Tennis Court",
    	"Volleyball Court", "Game Room", "Golf Course", "Fitness Facility", "Day Spa", "Casino",
    	"Beauty Salon", "BBQ", "Basketball Court", "Patio/Balcony", "Courtyard", "View", "Terrace",
    	"Room", "Studio", "Suite", "Efficiency", "Master Bedroom", "Living Area", "Kitchen", "Dining Area",
    	"Breakfast Area", "Bathroom", "Meeting Facility", "Ballroom", "Business Center", "Coffee Shop",
    	"Gift Shop" };
    
    // thumbnail images urls ending with "_t.jpg"   -->  can replace to "_y.jpg" for high res photo
    private static final String VERY_LOW_RES_THUMB = "t"; // 70x70
    private static final String LOW_RES_THUMB = "n"; // 90x90
    private static final String WIDE_LOW_RES_THUMB = "e"; // 160x90
    private static final String MID_LOW_RES_THUMB = "g"; // 140x140
    private static final String MID_RES_THUMB = "d"; // 180x180

    // these sizes below are not exact - the change from image to image
    private static final String WIDE_MID_RES_THUMB = "s"; // 200x181
    private static final String WIDE_RES_THUMB = "l"; // 255x144
    private static final String IMAGE = "b"; // 350x317
    private static final String FULL_RES_IMAGE = "y"; // 500x454

	
    // Some images do not have a full res photo,
    // for example http://images.travelnow.com/hotels/7000000/6240000/6235900/6235889/6235889_2_b.jpg
    
    private static Pattern resolutionMark = Pattern.compile("_([a-z]).jpg$"); 
    
    
    private static String replaceResolution(String photoUrl, String newRes) {
    	if (photoUrl == null)
    		return null;
    	return resolutionMark.matcher(photoUrl).replaceAll("_"+newRes+".jpg");
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
    public HotelImageTuple(final String partialThumbnailUrl, final String partialMainUrl, final String caption, final int category)
            throws MalformedURLException {
        this.thumbnailUrl = getFullImageUrl(replaceResolution(partialThumbnailUrl, WIDE_RES_THUMB));
        this.mainUrl = getFullImageUrl(replaceResolution(partialMainUrl, FULL_RES_IMAGE));
        this.caption = caption;
        this.category = category;
    }

    public String getCategoryTitle() {
    	if (category > 0 && category < CATEGORY_TITLES.length) {
    		return CATEGORY_TITLES[category];
    	}
    	return "";
    }
  /*  
    public Bitmap loadMainImage() {
    	Bitmap img = VHAApplication.HOTEL_PHOTOS.get(mainUrl.toString());
    	if (img != null) {
    		return img;
    	}
    }
*/
    /**
     * Gets the full image url, based simply on the partial url which does not include the protocol or host.
     * @param partial The partial url, excluding the host and previous
     * @return The full url to the image.
     * @throws java.net.MalformedURLException If the default IMAGE_PROTOCOL and IMAGE_HOST, combined with partial do not
     *  create a valid URL.
     */
    private static URL getFullImageUrl(final String partial) {
    	if (partial == null) {
    		return null;
    	}
    	try {
    		if (partial.startsWith("http")) {
				return new URL(partial);
    		}
			return new URL(IMAGE_PROTOCOL, IMAGE_HOST, partial);
    	} catch (MalformedURLException e) {
			DLog.e(TAG, "Malformed URL "+partial, e);
			return null;
		}
    }

	public boolean downgradeThumbnailResolution() {
		final String[] resolutions = {
				VERY_LOW_RES_THUMB, LOW_RES_THUMB, MID_LOW_RES_THUMB , 
				MID_RES_THUMB,
				WIDE_LOW_RES_THUMB,
				WIDE_MID_RES_THUMB,
				WIDE_RES_THUMB
		};
		    		
		String current = resolutionMark.matcher(thumbnailUrl.toString()).group(1);
		for (int i=1; i<resolutions.length; i++) {
			if (current.equals(resolutions[i])) {
				try {
					thumbnailUrl = new URL(replaceResolution(thumbnailUrl.toString(), resolutions[i-1]));
				} catch (MalformedURLException e) {
					DLog.e(TAG, "Failed to downgrade resolution of thumbnail url "+thumbnailUrl);
					return false;
				}
				return true;
			}
		}
		return false;
	}

	public boolean downgradeImgResolution() {
		final String[] resolutions = {
				IMAGE, FULL_RES_IMAGE
		};
		    		
		String current = resolutionMark.matcher(mainUrl.toString()).group(1);
		for (int i=1; i<resolutions.length; i++) {
			if (current.equals(resolutions[i])) {
				try {
					mainUrl = new URL(replaceResolution(mainUrl.toString(), resolutions[i-1]));
				} catch (MalformedURLException e) {
					DLog.e(TAG, "Failed to downgrade resolution of main url "+mainUrl);
					return false;
				}
				return true;
			}
		}
		return false;
	}
}
