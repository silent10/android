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

package com.ean.mobile.hotel.request;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ean.mobile.Constants;
import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelImageTuple;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.request.CommonParameters;
import com.ean.mobile.request.Request;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;

/**
 * Uses getHotelInformation to get the rest of the hotel's information, including images
 * and the hotel's full description.
 */
public final class InformationRequest extends Request<HotelInformation> {

	private Hotel hotel;
    /**
     * Gets the rest of the information about a hotel not included in previous calls.
     * @param hotelId The hotelId for which to gather more information.
     */
    public InformationRequest(final Hotel hotel) {
    	this.hotel = hotel;
        final List<NameValuePair> requestParameters = Arrays.<NameValuePair>asList(
            new BasicNameValuePair("hotelId", Long.toString(hotel.hotelId)),
            new BasicNameValuePair("options", "HOTEL_SUMMARY,HOTEL_DETAILS,PROPERTY_AMENITIES,HOTEL_IMAGES")
        );

        final List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.addAll(getBasicUrlParameters());
        urlParameters.addAll(requestParameters);
        setUrlParameters(urlParameters);
    }

    
    private void optionalAppend(StringBuilder sb, JSONObject jObj, String element, String title) {
		String value = jObj.optString(element, "").trim();
		if (value.equals("") == false) {
			sb.append("&lt;p&gt; &lt;b&gt;")
			  .append(title)
			  .append("&lt;/b&gt; &lt;br /&gt;")
			  .append(value)
			  .append("&lt;/p&gt;");
		}
	}
	
	private String getHotelDescription(JSONObject jHotelDetails) {
		StringBuilder descriptionStr = new StringBuilder();
		
		descriptionStr.append( jHotelDetails.optString("propertyDescription", ""));
		
		optionalAppend(descriptionStr, jHotelDetails, "drivingDirections", "Driving Directions" );
		optionalAppend(descriptionStr, jHotelDetails, "hotelPolicy", "Hotel Policy" );
		optionalAppend(descriptionStr, jHotelDetails, "checkInInstructions", "Check In Instructions" );
		optionalAppend(descriptionStr, jHotelDetails, "roomInformation", "Room Information" );
		optionalAppend(descriptionStr, jHotelDetails, "propertyInformation", "Property Information" );
		
		if (hotel.address != null && hotel.address.lines.isEmpty() == false) {
			descriptionStr.append("&lt;p&gt; &lt;b&gt;Address &lt;/b&gt; &lt;br /&gt;");
			for (String line : hotel.address.lines) {
				descriptionStr.append(line).append("&lt;br /&gt;");
			}
			descriptionStr.append(hotel.address.city);
			if (hotel.address.stateProvinceCode.equals("") == false) {
				descriptionStr.append(", ").append(hotel.address.stateProvinceCode);
			}
			descriptionStr.append("&lt;br /&gt;");
			descriptionStr.append(hotel.address.postalCode).append(", ");
			descriptionStr.append(hotel.address.countryCode).append("&lt;br /&gt;");
			
		}
		
		optionalAppend(descriptionStr, jHotelDetails, "areaInformation", "Area Information" );
		
		
		return descriptionStr.toString();
	}
    
    /**
     * {@inheritDoc}
     */
    @Override
    public HotelInformation consume(final JSONObject jsonObject) throws JSONException, EanWsError {
        if (jsonObject == null) {
            return null;
        }

        final JSONObject infoResp = jsonObject.getJSONObject("HotelInformationResponse");
        final JSONObject details = infoResp.getJSONObject("HotelDetails");
        final JSONArray images = infoResp.getJSONObject("HotelImages").getJSONArray("HotelImage");

        final String longDescription = getHotelDescription(details);

        final List<HotelImageTuple> imageTuples = new ArrayList<HotelImageTuple>();

        JSONObject image;
        for (int i = 0; i < images.length(); i++) {
            image = images.getJSONObject(i);
            try {
                imageTuples.add(
                    new HotelImageTuple(new URL(image.optString("thumbnailUrl")),
                        new URL(image.optString("url")), image.optString("caption")));
            } catch (MalformedURLException me) {
                VHAApplication.logError(Constants.LOG_TAG, "Unable to process JSON: "+
                				image.optString("url","<null>")+" "+image.optString("thumbnailUrl","<null>"), me);
            }
        }

        CommonParameters.customerSessionId = infoResp.optString("customerSessionId");

        Log.d(Constants.LOG_TAG, "Found " + imageTuples.size() + " images");
        return new HotelInformation(infoResp.optLong("@hotelId"), longDescription, imageTuples);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI getUri() throws URISyntaxException {
        return new URI("http", "api.ean.com", "/ean-services/rs/hotel/v3/info", getQueryString(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requiresSecure() {
        return false;
    }


	@Override
	public String getName() {
		return "InformationRequest";
	}
}
