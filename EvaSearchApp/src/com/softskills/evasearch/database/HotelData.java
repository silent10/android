package com.softskills.evasearch.database;

import org.json.JSONObject;


public class HotelData {
	
	public HotelData(JSONObject jHotel) {
		mSummary = new HotelSummary(jHotel);
	}
	public HotelSummary mSummary=null;
	public HotelDetails mDetails=null;

}
