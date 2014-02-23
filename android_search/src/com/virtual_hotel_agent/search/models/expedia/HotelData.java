package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;


public class HotelData {
	private boolean selected;
	
	public HotelData(JSONObject jHotel) {
		mSummary = new HotelSummary(jHotel);
//		Ln.d("Hotel %s   price= %s - %s",mSummary.mName, mSummary.mLowRate, mSummary.mHighRate);
		selected = false;
	}
	
	// summary is filled by both hotel-list and hotel-info
	public HotelSummary mSummary = null;

	// http://developer.ean.com/docs/hotel-info/
	public HotelDetails mDetails = null;

	public void setSelected(boolean val) {
		selected = val;
	}
	
	public boolean isSelected() {
		return selected;
	}

}
