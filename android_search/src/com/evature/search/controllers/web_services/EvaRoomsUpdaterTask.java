package com.evature.search.controllers.web_services;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.MyApplication;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.fragments.CalendarFragment;

public class EvaRoomsUpdaterTask extends EvaDownloaderTask {
	private HotelData mHotelData;
	private Context mContext;

	public EvaRoomsUpdaterTask(EvaDownloaderTaskInterface calendarFragment, Context context, int hotelIndex) {
		super(-1);
		mHotelData =MyApplication.getDb().mHotelData[hotelIndex];			
		attach(calendarFragment);	
		mContext= context;
	}
	
	
	@Override
	protected void onPostExecute(String result) {
		mProgress = DownloaderStatus.Finished;
		super.onPostExecute(result);
	}

	@Override
	protected String doInBackground(Void... params) {
		String str= XpediaProtocolStatic.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				MyApplication.getDb(),
				EvaSettingsAPI.getCurrencyCode(mContext));

		mHotelData.mSummary.updateRoomDetails(str);

		return str;
	}

	

	

}
