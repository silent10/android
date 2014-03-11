package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;

import android.location.Location;

import com.evaapis.android.EvatureLocationUpdater;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;


public class HotelData {
	private static final String TAG = "HotelData";

	private boolean selected;
	private double distance = -2; 
	
	// summary is filled by both hotel-list and hotel-info
	public HotelSummary mSummary = null;

	// http://developer.ean.com/docs/hotel-info/
	public HotelDetails mDetails = null;

	
	public HotelData(JSONObject jHotel) {
		mSummary = new HotelSummary(jHotel);
//		Ln.d("Hotel %s   price= %s - %s",mSummary.mName, mSummary.mLowRate, mSummary.mHighRate);
		selected = false;
	}

	
	public void setSelected(boolean val) {
		selected = val;
	}
	
	public boolean isSelected() {
		return selected;
	}

	public double getDistanceFromMe() {
		if (distance == -2) {
			double hotelLatitude = mSummary.mLatitude;
			double hotelLongitude = mSummary.mLongitude;
			double myLongitude, myLatitude;
			try {
				myLongitude = EvatureLocationUpdater.getLongitude();
				if (myLongitude != EvatureLocationUpdater.NO_LOCATION) {
					myLatitude = EvatureLocationUpdater.getLatitude();
					float[] results = new float[3];
					Location.distanceBetween(myLatitude, myLongitude,
							hotelLatitude, hotelLongitude, results);
					if (results != null && results.length > 0)
						distance = results[0] / 1000;
				}
	
			} catch (Exception e2) {
				MainActivity.LogError(TAG, "Error calculating distance", e2);
				distance = -1;
			}
		}
		return distance;
	}

}
