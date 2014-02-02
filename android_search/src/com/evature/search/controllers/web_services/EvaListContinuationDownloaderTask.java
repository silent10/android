package com.evature.search.controllers.web_services;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.Toast;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.expedia.EvaXpediaDatabase;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.fragments.HotelsFragment;

public class EvaListContinuationDownloaderTask extends EvaDownloaderTask {
	private static final String TAG = EvaListContinuationDownloaderTask.class.getSimpleName();
	private String mNextQuery;
	private String mCurrencyCode;
	private HotelsFragment hotelFragment;

	public EvaListContinuationDownloaderTask(HotelsFragment listener, String nextQuery, String currencyCode) {
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

		JSONObject hotelListResponse = XpediaProtocolStatic.getExpediaNext(mNextQuery, mCurrencyCode);
		if (hotelListResponse == null)
			Log.e(TAG, "Response for next null");
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
//			MyApplication.getDb().EvaDatabaseUpdateExpedia(hotelListResponseJSON);
			EvaXpediaDatabase db = new EvaXpediaDatabase(hotelListResponseJSON);
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
