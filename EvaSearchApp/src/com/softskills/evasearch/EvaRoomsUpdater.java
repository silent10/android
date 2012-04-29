package com.softskills.evasearch;

import android.support.v4.app.Fragment;

import com.softskills.evasearch.database.HotelData;

public class EvaRoomsUpdater extends EvaDownloaderTask {
	
	@Override
	protected void onPostExecute(Void result) {
		mListener.endProgressDialog();
		super.onPostExecute(result);
	}



	private HotelData mHotelData;
	
	
		@Override
	protected Void doInBackground(Void... params) {
		String str= XpediaProtocol.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				EvaSearchApplication.getDb().mArrivalDateParam,
				EvaSearchApplication.getDb().mDepartureDateParam,
				EvaSettingsAPI.getCurrencyCode(((Fragment)mListener).getActivity()),
				EvaSearchApplication.getDb().mNumberOfAdultsParam);

		mHotelData.mSummary.updateRoomDetails(str);

		return super.doInBackground(params);
	}

	

	public EvaRoomsUpdater(CalendarFragment calendarFragment, int hotelIndex) {
		mHotelData = EvaSearchApplication.getDb().mHotelData[hotelIndex];			
		attach(calendarFragment);					
	}
	
	

}
