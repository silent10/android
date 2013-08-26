package com.evature.search.controllers.web_services;

import android.support.v4.app.Fragment;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.MyApplication;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.fragments.CalendarFragment;

public class EvaRoomsUpdaterTask extends EvaDownloaderTask {
	
	@Override
	protected void onPostExecute(String result) {
		mListener.endProgressDialog(mProgress, result);
		super.onPostExecute(result);
	}



	private HotelData mHotelData;
	
	
		@Override
	protected String doInBackground(Void... params) {
		String str= XpediaProtocolStatic.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				MyApplication.getDb().mArrivalDateParam,
				MyApplication.getDb().mDepartureDateParam,
				EvaSettingsAPI.getCurrencyCode(((Fragment)mListener).getActivity()),
				MyApplication.getDb().mNumberOfAdultsParam);

		mHotelData.mSummary.updateRoomDetails(str);

		return str;
	}

	

	public EvaRoomsUpdaterTask(CalendarFragment calendarFragment, int hotelIndex) {
		mHotelData =MyApplication.getDb().mHotelData[hotelIndex];			
		attach(calendarFragment);					
	}
	
	

}
