package com.evature.search;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.evature.search.expedia.EvaDatabase;
import com.evature.search.expedia.HotelData;
import com.evature.search.expedia.HotelDetails;
import com.evature.search.expedia.XpediaProtocol;

public class EvaHotelDownloaderTask extends EvaDownloaderTask {
	private static final String TAG = EvaHotelDownloaderTask.class.getSimpleName();

	@Override
	protected int getId() {
		return R.string.HOTEL;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	int mHotelIndex;

	public EvaHotelDownloaderTask(Context listener, int hotelndex) {
		Log.d(TAG, "CTOR");
		attach((EvaDownloaderTaskInterface) listener);
		mHotelIndex = hotelndex;
	}

	@Override
	protected Void doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");

		EvaDatabase db = MyApplication.getDb();
		HotelData hotelData = db.mHotelData[mHotelIndex];

		publishProgress();

		String hotelInfo = XpediaProtocol.getExpediaHotelInformation(hotelData.mSummary.mHotelId,
				EvaSettingsAPI.getCurrencyCode((Context) mListener));

		JSONObject jHotel;
		try {
			if (hotelInfo != null) {
				jHotel = new JSONObject(hotelInfo);

				JSONObject jHotelInfo = jHotel.getJSONObject("HotelInformationResponse");

				hotelData.mDetails = new HotelDetails(jHotelInfo);

				if (db.mArrivalDateParam != null && db.mDepartureDateParam != null) {
					String str = XpediaProtocol.getRoomInformationForHotel(hotelData.mSummary.mHotelId,
							db.mArrivalDateParam, db.mDepartureDateParam,
							EvaSettingsAPI.getCurrencyCode((Context) mListener), db.mNumberOfAdultsParam);
					Log.d(TAG, str);
					hotelData.mSummary.updateRoomDetails(str);
				}
				mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH;

			} else {
				mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR;
			}

		} catch (JSONException e) {
			Log.e(TAG, "JSON exception getting hotel details");
			e.printStackTrace();
		}

		return null;
	}

	public int getHotelIndex() {
		return mHotelIndex;
	}

}
