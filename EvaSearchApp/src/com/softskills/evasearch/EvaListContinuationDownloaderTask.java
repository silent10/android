package com.softskills.evasearch;

import org.json.JSONException;
import org.json.JSONObject;

import com.softskills.evasearch.database.EvaDatabase;

import android.util.Log;

public class EvaListContinuationDownloaderTask extends HotelListDownloaderTask {

	@Override
	protected void onPostExecute(Void result) {
		
		if(mListener==null) return;
		
		((HotelListFragment)mListener).finishPaging();
		super.onPostExecute(result);
	}

	private static final String TAG = "EvaListContinuationDownloaderTask";

	public EvaListContinuationDownloaderTask(
			EvaDownloaderTaskInterface listener, String queryString,
			String currencyCode) {
		super(listener, queryString, currencyCode);		
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.i(TAG,"Do in background : Query string:"+mQueryString);
		
    	String hotelListResponse = XpediaProtocol.getExpediaNext(mQueryString, mCurrencyCode);
    	if (hotelListResponse == null)
    		Log.e(TAG,"Response for next null");
    	else
    		Log.i(TAG,"Next: "+hotelListResponse);
    	mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
    	if(false==addHotelData(hotelListResponse))
    	{
    		mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR;    		
    	}
    	else
    	{
    		mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH;
    	}
    	
    	publishProgress((Integer [] )null);
    	
		return null;
	}
	
	boolean addHotelData(String hotelListResponse) {
		
		Log.i(TAG,"Add Hotel Data");
		
		JSONObject hotelListResponseJSON;
		try {
			hotelListResponseJSON = new JSONObject(hotelListResponse);
			EvaDatabase db = new EvaDatabase(hotelListResponseJSON);
		
			if(db.mHotelData!=null)
			{
				EvaSearchApplication.getDb().addData(db);
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
	
}
