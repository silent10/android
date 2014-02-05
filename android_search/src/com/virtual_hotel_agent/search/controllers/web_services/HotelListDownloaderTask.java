package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

import roboguice.util.Ln;
import android.util.Log;

import com.evaapis.EvaApiReply;
import com.google.inject.Inject;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocol;

public class HotelListDownloaderTask extends DownloaderTask {

	private static final String TAG = HotelListDownloaderTask.class.getSimpleName();
	// String mSearchQuery;
	String mCurrencyCode;
	EvaApiReply apiReply;
	
	@Inject XpediaProtocol xpediaProtocol;

	public HotelListDownloaderTask() {
		super(R.string.HOTELS);
	}

	public void initialize(DownloaderTaskInterface listener, EvaApiReply apiReply, String currencyCode) {
		Log.i(TAG, "CTOR");
		// mSearchQuery = searchQuery;
		this.apiReply = apiReply;
		attach(listener);
		mCurrencyCode = currencyCode;
	}

	void createHotelData(JSONObject hotelListResponseJSON) {

		if (hotelListResponseJSON == null) {
			return;
		}
		try {
//			MyApplication.getDb().EvaDatabaseUpdateExpedia(hotelListResponseJSON);
			XpediaDatabase db = new XpediaDatabase(hotelListResponseJSON);
			MyApplication.setDb(db);
			
			
//			if (MyApplication.getDb().mHotelData == null) {
//				return false;
//			}
//			else {
//				 MyApplication.setDb(db);
//			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected JSONObject doInBackground(Void... params) {

		Log.i(TAG, "doInBackground: start");
		// String searchQuery = EvaProtocol.getEvatureResponse(mQueryString);
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_EXPEDIA_HOTEL_FETCH;
		publishProgress();
		Log.i(TAG, "doInBackground: Calling Expedia");
		JSONObject hotelListResponse = xpediaProtocol.getExpediaAnswer(apiReply, MyApplication.getExpediaRequestParams(), mCurrencyCode);
		if (hotelListResponse == null) {
			Ln.d("null hotelist response!");
		}
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
		
		return hotelListResponse;
	}
	
	@Override
	protected void onPostExecute(JSONObject result) {
		if (result == null) {
			Log.i(TAG, "doInBackground: Error in Expedia response");
			mProgress = DownloaderStatus.FinishedWithError;
		}
		else {
			createHotelData(result);
			
			Log.i(TAG, "doInBackground: All OK");
			mProgress = DownloaderStatus.Finished;
			MyApplication.getExpediaRequestParams().setArrivalDate(apiReply.ean.get("arrivalDate"));
			MyApplication.getExpediaRequestParams().setDepartueDate(apiReply.ean.get("departureDate"));
			//MyApplication.getDb().setNumberOfAdults(1);
			Log.i(TAG, "doInBackground: end");
		}	
		super.onPostExecute(result);
	}
}
