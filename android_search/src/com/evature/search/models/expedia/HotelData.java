package com.evature.search.models.expedia;

import org.json.JSONObject;

import roboguice.util.Ln;

public class HotelData {

	public HotelData(JSONObject jHotel) {
		mSummary = new HotelSummary(jHotel);
//		Ln.d("Hotel %s   price= %s - %s",mSummary.mName, mSummary.mLowRate, mSummary.mHighRate);
	}
	
	// summary is filled by both hotel-list and hotel-info
	public HotelSummary mSummary = null;

	// http://developer.ean.com/docs/hotel-info/
	public HotelDetails mDetails = null;

}
