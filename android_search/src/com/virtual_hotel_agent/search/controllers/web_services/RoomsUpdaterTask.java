package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

import android.content.Context;

import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;

public class RoomsUpdaterTask extends DownloaderTask {
	private HotelData mHotelData;
	private Context mContext;

	public RoomsUpdaterTask(Context context, int hotelIndex) {
		super(-1);
		mHotelData =MyApplication.getDb().mHotelData[hotelIndex];			
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
				MyApplication.getExpediaRequestParams(),
				SettingsAPI.getCurrencyCode(mContext));

		mHotelData.mSummary.updateRoomDetails(result);

		return result;
	}

	

	

}
