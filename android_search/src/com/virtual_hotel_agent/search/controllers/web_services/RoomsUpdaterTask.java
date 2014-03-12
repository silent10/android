package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

import android.content.Context;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;

public class RoomsUpdaterTask extends DownloaderTask {
	private static final String TAG = "RoomUpdaterTask";
	private HotelData mHotelData;
	private Context mContext;

	public RoomsUpdaterTask(Context context, int hotelIndex) {
		super(-1);
		XpediaDatabase db = MyApplication.getDb();
		if (db != null && db.mHotelData != null && db.mHotelData.length > hotelIndex)
			mHotelData = db.mHotelData[hotelIndex];
		else {
			MainActivity.LogError(TAG, "Attempting to update rooms without hotelData");
		}
		mContext= context;
	}
	
	
	@Override
	protected void onPostExecute(JSONObject result) {
		mProgress = DownloaderStatus.Finished;
		super.onPostExecute(result);
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
		JSONObject result= XpediaProtocolStatic.getRoomInformationForHotel(mContext, mHotelData.mSummary.mHotelId,
				MyApplication.getExpediaAppState(),
				SettingsAPI.getCurrencyCode(mContext));

		mHotelData.mSummary.updateRoomDetails(result);

		return result;
	}

}
