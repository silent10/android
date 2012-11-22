package com.evature.search.expedia;

import org.json.JSONObject;

public class HotelData {

	public HotelData(JSONObject jHotel) {
		mSummary = new HotelSummary(jHotel);
	}

	public HotelSummary mSummary = null;
	public HotelDetails mDetails = null;

}
