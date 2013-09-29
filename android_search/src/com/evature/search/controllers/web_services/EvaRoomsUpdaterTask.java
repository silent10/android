package com.evature.search.controllers.web_services;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.MyApplication;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.fragments.CalendarFragment;

public class EvaRoomsUpdaterTask extends EvaDownloaderTask {
	private HotelData mHotelData;
	private CalendarFragment mCalenderFragment;

	public EvaRoomsUpdaterTask(CalendarFragment calendarFragment, int hotelIndex) {
		super(-1);
		mHotelData =MyApplication.getDb().mHotelData[hotelIndex];			
		attach(calendarFragment);	
		mCalenderFragment = calendarFragment;
	}
	
	
	@Override
	protected void onPostExecute(String result) {
		mProgress = DownloaderStatus.Finished;
		super.onPostExecute(result);
	}

	@Override
	protected String doInBackground(Void... params) {
		String str= XpediaProtocolStatic.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				MyApplication.getDb().mArrivalDateParam,
				MyApplication.getDb().mDepartureDateParam,
				EvaSettingsAPI.getCurrencyCode((mCalenderFragment).getActivity()),
				MyApplication.getDb().mNumberOfAdultsParam);

		mHotelData.mSummary.updateRoomDetails(str);

		return str;
	}

	

	

}
