package com.evature.search.controllers.web_services;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.evaapis.EvaApiReply;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.expedia.EvaXpediaDatabase;
import com.evature.search.models.expedia.XpediaProtocol;
import com.google.inject.Inject;

public class HotelListDownloaderTask extends EvaDownloaderTask {

	private static final String TAG = HotelListDownloaderTask.class.getSimpleName();
	// String mSearchQuery;
	String mCurrencyCode;
	EvaApiReply apiReply;
	
	@Inject XpediaProtocol xpediaProtocol;

	@Override
	public int getId() {
		return R.string.HOTELS;
	}

	public void initialize(EvaDownloaderTaskInterface listener, EvaApiReply apiReply, String currencyCode) {
		Log.i(TAG, "CTOR");
		// mSearchQuery = searchQuery;
		this.apiReply = apiReply;
		attach(listener);
		mCurrencyCode = currencyCode;
	}

	String createHotelData(String hotelListResponse) {

		if (hotelListResponse == null) {
			return null;
		}
		JSONObject hotelListResponseJSON;
		try {
			hotelListResponseJSON = new JSONObject(hotelListResponse);
//			MyApplication.getDb().EvaDatabaseUpdateExpedia(hotelListResponseJSON);
			EvaXpediaDatabase db = new EvaXpediaDatabase(hotelListResponseJSON);
			MyApplication.setDb(db);
			
			
//			if (MyApplication.getDb().mHotelData == null) {
//				return false;
//			}
//			else {
//				 MyApplication.setDb(db);
//			}
			return hotelListResponseJSON.toString(2);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	protected String doInBackground(Void... params) {

		Log.i(TAG, "doInBackground: start");
		// String searchQuery = EvaProtocol.getEvatureResponse(mQueryString);
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_EXPEDIA_HOTEL_FETCH;
		publishProgress();
		Log.i(TAG, "doInBackground: Calling Expedia");
		String hotelListResponse = xpediaProtocol.getExpediaAnswer(apiReply, mCurrencyCode);
		if (hotelListResponse == null) {
			Log.d(TAG, "null hotelist response!");
		}
		else {
			Log.d(TAG, hotelListResponse);
		}
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
		
		return hotelListResponse;
	}
	
	@Override
	protected void onPostExecute(String result) {
		if (result == null) {
			Log.i(TAG, "doInBackground: Error in Expedia response");
			mProgress = DownloaderStatus.FinishedWithError;
		}
		else {
			result = createHotelData(result);
			
			Log.i(TAG, "doInBackground: All OK");
			mProgress = DownloaderStatus.Finished;
			MyApplication.getDb().setArrivalDate(apiReply.ean.get("arrivalDate"));
			// XpediaProtocolStatic.getParamFromEvatureResponse(mSearchQuery, "arrivalDate"));
			MyApplication.getDb().setDepartueDate(apiReply.ean.get("departureDate"));
			// XpediaProtocolStatic.getParamFromEvatureResponse(mSearchQuery, "departureDate"));
			MyApplication.getDb().setNumberOfAdults(1);
			Log.i(TAG, "doInBackground: end");
		}	
		super.onPostExecute(result);
	}
}
