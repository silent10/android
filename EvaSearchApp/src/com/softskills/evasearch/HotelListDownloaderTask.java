package com.softskills.evasearch;

import org.json.JSONException;
import org.json.JSONObject;

import com.softskills.evasearch.database.EvaDatabase;

import android.util.Log;

public class HotelListDownloaderTask extends EvaDownloaderTask {

	private static final String TAG = "HotelListDownloaderTask";
	String mQueryString;
	String mCurrencyCode;

	public HotelListDownloaderTask(EvaDownloaderTaskInterface listener, String queryString, String currencyCode) 
	{		
		Log.i(TAG,"CTOR");
		mQueryString = queryString;
		attach(listener);
		mCurrencyCode = currencyCode;
	}
	
	
boolean createHotelData(String hotelListResponse) {
		
		JSONObject hotelListResponseJSON;
		try {
			hotelListResponseJSON = new JSONObject(hotelListResponse);
			EvaDatabase db = new EvaDatabase(hotelListResponseJSON);
		
			if(db.mHotelData!=null)
			{
				EvaSearchApplication.setEvaDb(db);
			}
			else
			{
				return false;
			}
			return true;
		} catch (JSONException e) {			
			e.printStackTrace();
			return false;
		}
		catch (NullPointerException e) {			
			e.printStackTrace();
			return false;
		}
		
	}

	
	@Override
	protected Void doInBackground(Void... params) {
		
		Log.i(TAG,"doInBk:start");
		String searchQuery = EvaProtocol.getEvatureResponse(mQueryString);
    	mProgress = EvaDownloaderTaskInterface.PROGRESS_EXPEDIA_HOTEL_FETCH;
    	publishProgress();
    	Log.i(TAG,"doInBk:Calling Expedia");
    	String hotelListResponse = XpediaProtocol.getExpediaAnswer(searchQuery, mCurrencyCode);
    	mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
    	if((hotelListResponse==null)||(false==createHotelData(hotelListResponse)))
    	{
    		Log.i(TAG,"doInBk:Error in Expedia response");
    		mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR;    		
    	}
    	else
    	{
    		Log.i(TAG,"doInBk:All OK");
    		mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH;
    		EvaSearchApplication.getDb().setArrivalDate(
    				XpediaProtocol.getParamFromEvatureResponse(searchQuery, "arrivalDate"));
    		
    		EvaSearchApplication.getDb().setDepartueDate(
    				XpediaProtocol.getParamFromEvatureResponse(searchQuery, "departureDate"));
    		EvaSearchApplication.getDb().setNumberOfAdults(1);
    	}
    	Log.i(TAG,"doInBk:end");
		return null;
	}
}
