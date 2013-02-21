package com.evature.search;

import android.support.v4.app.Fragment;

import com.evature.search.expedia.HotelData;
import com.evature.search.expedia.XpediaProtocol;

public class EvaRoomsUpdater extends EvaDownloaderTask {
	
	@Override
	protected void onPostExecute(Void result) {
		mListener.endProgressDialog(mProgress);
		super.onPostExecute(result);
	}



	private HotelData mHotelData;
	
	
		@Override
	protected Void doInBackground(Void... params) {
		String str= XpediaProtocol.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				MyApplication.getDb().mArrivalDateParam,
				MyApplication.getDb().mDepartureDateParam,
				EvaSettingsAPI.getCurrencyCode(((Fragment)mListener).getActivity()),
				MyApplication.getDb().mNumberOfAdultsParam);

		mHotelData.mSummary.updateRoomDetails(str);

		return super.doInBackground(params);
	}

	

	public EvaRoomsUpdater(CalendarFragment calendarFragment, int hotelIndex) {
		mHotelData =MyApplication.getDb().mHotelData[hotelIndex];			
		attach(calendarFragment);					
	}
	
	

}
