package com.evature.search.models.expedia;

import android.graphics.Bitmap;

import com.evaapis.EvaApiReply;
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

	public String getExpediaHotelInformation(int hotelId, String currencyCode) {
		return XpediaProtocolStatic.getExpediaHotelInformation(hotelId, currencyCode);
	}

	public String getExpediaAnswer(EvaApiReply apiReply, EvaXpediaDatabase db,String currencyCode) {
		return XpediaProtocolStatic.getExpediaAnswer(apiReply, db, currencyCode);
	}

	public String getRoomInformationForHotel(int hotelId, EvaXpediaDatabase db,
			String currencyCode) {
		return XpediaProtocolStatic.getRoomInformationForHotel(hotelId, db, currencyCode);
	}

	public String getExpediaNext(String mQueryString, String currencyCode) {
		return XpediaProtocolStatic.getExpediaNext(mQueryString, currencyCode);
	}


}
