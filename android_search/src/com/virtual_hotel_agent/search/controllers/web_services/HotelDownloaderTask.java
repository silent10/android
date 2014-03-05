package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelDetails;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;

public class HotelDownloaderTask extends DownloaderTask {
	private static final String TAG = HotelDownloaderTask.class.getSimpleName();

	public String lastResponse = null;
	

	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	int mHotelIndex;
	Context mContext;

	public HotelDownloaderTask(DownloaderTaskListenerInterface listener, Context context, int hotelndex) {
		super( R.string.HOTEL);
		Log.d(TAG, "CTOR");
		attach(listener);
		mContext = context;
		mHotelIndex = hotelndex;
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");

		XpediaDatabase db = MyApplication.getDb();
		HotelData hotelData = db.mHotelData[mHotelIndex];

		db.setHotelSelected(mHotelIndex);
		
		publishProgress();

		JSONObject jHotel = XpediaProtocolStatic.getExpediaHotelInformation(mContext, hotelData.mSummary.mHotelId,
				SettingsAPI.getCurrencyCode(mContext));

		if (jHotel == null) {
			mProgress = DownloaderStatus.FinishedWithError;
			return null;
		}
		
		try {

			JSONObject jHotelInfo = jHotel.getJSONObject("HotelInformationResponse");

			hotelData.mDetails = new HotelDetails(jHotelInfo);

			mProgress = DownloaderStatus.Finished;

			
			return jHotel;

		} catch (JSONException e) {
			MainActivity.LogError(TAG, "JSON exception getting hotel details", e);
			return jHotel;
		}
	}

	public int getHotelIndex() {
		return mHotelIndex;
	}

}
