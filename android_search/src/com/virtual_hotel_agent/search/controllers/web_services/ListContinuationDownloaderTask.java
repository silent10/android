package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

public class ListContinuationDownloaderTask extends DownloaderTask {
	private static final String TAG = ListContinuationDownloaderTask.class.getSimpleName();
	private String mNextQuery;
	private String mCurrencyCode;
	private HotelListFragment hotelFragment;

	public ListContinuationDownloaderTask(HotelListFragment listener, String nextQuery, String currencyCode) {
		super(R.string.HOTELS);
		Log.i(TAG, "CTOR");
		mNextQuery = nextQuery;
		attach(listener);
		hotelFragment = listener;
		mCurrencyCode = currencyCode;
		
	}

	@Override
	protected void onPostExecute(JSONObject result) {

		if (hotelFragment == null)
			return;

		super.onPostExecute(result);
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
		Log.i(TAG, "Do in background");

		JSONObject hotelListResponse = XpediaProtocolStatic.getExpediaNext(hotelFragment.getActivity(), mNextQuery, mCurrencyCode);
		if (hotelListResponse == null)
			MainActivity.LogError(TAG, "Response for next null");
		mProgress = DownloaderStatus.MadeSomeProgress;
		onProgressUpdate();
		
		if (false == addHotelData(hotelListResponse)) {
			mProgress = DownloaderStatus.FinishedWithError;
		} else {
			mProgress = DownloaderStatus.Finished;
		}

		return hotelListResponse;
	}

	boolean addHotelData(JSONObject hotelListResponseJSON) {

		Log.i(TAG, "Add Hotel Data");

		try {
			XpediaDatabase db = new XpediaDatabase(hotelListResponseJSON);
			if (db.unrecoverableError) {
				MyApplication.setDb(db);
				return false;
			}
			if (MyApplication.getDb() == null) {
				MyApplication.setDb(db);
			} else {
				MyApplication.getDb().addData(db);
			}
			return true;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}

}
