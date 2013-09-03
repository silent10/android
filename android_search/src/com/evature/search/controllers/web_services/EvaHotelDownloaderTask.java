package com.evature.search.controllers.web_services;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.expedia.EvaXpediaDatabase;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.HotelDetails;
import com.evature.search.models.expedia.XpediaProtocolStatic;

public class EvaHotelDownloaderTask extends EvaDownloaderTask {
	private static final String TAG = EvaHotelDownloaderTask.class.getSimpleName();

	public String lastResponse = null;
	
	@Override
	public int getId() {
		return R.string.HOTEL;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	int mHotelIndex;
	Context mContext;

	public EvaHotelDownloaderTask(EvaDownloaderTaskInterface listener, int hotelndex) {
		Log.d(TAG, "CTOR");
		attach(listener);
		mContext = (Context) listener;
		mHotelIndex = hotelndex;
	}

	@Override
	protected String doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");

		EvaXpediaDatabase db = MyApplication.getDb();
		HotelData hotelData = db.mHotelData[mHotelIndex];

		publishProgress();

		String hotelInfo = XpediaProtocolStatic.getExpediaHotelInformation(hotelData.mSummary.mHotelId,
				EvaSettingsAPI.getCurrencyCode(mContext));

		if (hotelInfo == null) {
			mProgress = DownloaderStatus.FinishedWithError;
			return null;
		}
		
		JSONObject jHotel = null;
		try {
			jHotel = new JSONObject(hotelInfo);

			JSONObject jHotelInfo = jHotel.getJSONObject("HotelInformationResponse");

			hotelData.mDetails = new HotelDetails(jHotelInfo);

//			if (db.mArrivalDateParam != null && db.mDepartureDateParam != null) {
//				String str = XpediaProtocolStatic.getRoomInformationForHotel(hotelData.mSummary.mHotelId,
//						db.mArrivalDateParam, db.mDepartureDateParam,
//						EvaSettingsAPI.getCurrencyCode(mContext), db.mNumberOfAdultsParam);
			
//				Log.d(TAG, str);
//				hotelData.mSummary.updateRoomDetails(str);
//			}
			mProgress = DownloaderStatus.Finished;

		} catch (JSONException e) {
			Log.e(TAG, "JSON exception getting hotel details");
			e.printStackTrace();
		}

		try {
			lastResponse = jHotel.toString(2);
			
			return lastResponse;
		} catch (JSONException e) {
			e.printStackTrace();
			return hotelInfo;
		}
	}

	public int getHotelIndex() {
		return mHotelIndex;
	}

}
