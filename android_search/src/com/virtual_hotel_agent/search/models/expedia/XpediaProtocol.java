package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;

import com.evaapis.crossplatform.EvaApiReply;
import com.google.inject.Singleton;


/***
 * Wrapper for the static methods - allowing refactoring to be gradual and not all-or-nothing
 * This non-static way of calling the protocol allows mocking the class and verifiying the caller logic without triggering expedia protocol.
 */
@Singleton
public class XpediaProtocol {

	public Bitmap download_Image(String path) {
		return XpediaProtocolStatic.download_Image(path);
	}

	public JSONObject getExpediaHotelInformation(Context context, int hotelId, String currencyCode) {
		return XpediaProtocolStatic.getExpediaHotelInformation(context, hotelId, currencyCode);
	}

	public JSONObject getExpediaAnswer(Context context, EvaApiReply apiReply, ExpediaRequestParameters db,String currencyCode) {
		return XpediaProtocolStatic.getExpediaAnswer(context, apiReply, db, currencyCode);
	}

	public JSONObject getRoomInformationForHotel(Context context, int hotelId, ExpediaRequestParameters db,
			String currencyCode) {
		return XpediaProtocolStatic.getRoomInformationForHotel(context, hotelId, db, currencyCode);
	}

	public JSONObject getExpediaNext(Context context, String mQueryString, String currencyCode) {
		return XpediaProtocolStatic.getExpediaNext(context, mQueryString, currencyCode);
	}


}
