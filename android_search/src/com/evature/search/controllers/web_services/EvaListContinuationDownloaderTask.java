package com.evature.search.controllers.web_services;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.R.string;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.fragments.HotelsFragment;

public class EvaListContinuationDownloaderTask extends EvaDownloaderTask {
	private static final String TAG = EvaListContinuationDownloaderTask.class.getSimpleName();
	private String mNextQuery;
	private String mCurrencyCode;

	public EvaListContinuationDownloaderTask(EvaDownloaderTaskInterface listener, String nextQuery, String currencyCode) {
		Log.i(TAG, "CTOR");
		mNextQuery = nextQuery;
		attach(listener);
		mCurrencyCode = currencyCode;
		
	}

	@Override
	protected int getId() {
		return R.string.HOTELS;
	}

	@Override
	protected void onPostExecute(Void result) {

		if (mListener == null)
			return;

		((HotelsFragment) mListener).finishPaging();
		super.onPostExecute(result);
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.i(TAG, "Do in background");

		String hotelListResponse = XpediaProtocolStatic.getExpediaNext(mNextQuery, mCurrencyCode);
		if (hotelListResponse == null)
			Log.e(TAG, "Response for next null");
		else
			Log.i(TAG, "Next: " + hotelListResponse);
		mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
		if (false == addHotelData(hotelListResponse)) {
			mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR;
		} else {
			mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH;
		}

		publishProgress((Integer[]) null);

		return null;
	}

	boolean addHotelData(String hotelListResponse) {

		Log.i(TAG, "Add Hotel Data");

		JSONObject hotelListResponseJSON;
		try {
			hotelListResponseJSON = new JSONObject(hotelListResponse);
			MyApplication.getDb().EvaDatabaseUpdateExpedia(hotelListResponseJSON);
			// EvaDatabase db = new EvaDatabase(hotelListResponseJSON);

			if (MyApplication.getDb() == null) {
				return false;
			} else {
				// MyApplication.getDb().addData(db);
			}
			return true;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
	}

}
