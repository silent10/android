package com.evature.search;

import android.os.AsyncTask;

public class RoomsUpdaterTask extends AsyncTask<Void, Integer, Void> 
{

	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}
/*	private static final String TAG = "RoomsUpdaterTask";

	@Override
	protected void onPostExecute(Void result) {
		//mShowHotel.endProgressDialog();

		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
	//	mShowHotel.startProgressDialog("Updating room details");
		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	//private ShowHotel mShowHotel;


	HotelData mHotelData;

	
	public RoomsUpdaterTask(ShowHotel showHotel, HotelData hotelData) {

		mHotelData = hotelData;
		mShowHotel = showHotel;
		attach(showHotel);		
	}

	@Override
	protected Void doInBackground(Void... params) {


		String str= XpediaProtocol.getRoomInformationForHotel(mHotelData.mSummary.mHotelId,
				EvaSearchApplication.getDb().mArrivalDateParam,
				EvaSearchApplication.getDb().mDepartureDateParam,
				EvaSettingsAPI.getCurrencyCode(mShowHotel),
				EvaSearchApplication.getDb().mNumberOfAdultsParam);


		mHotelData.mSummary.updateRoomDetails(str);
		
		
		mShowHotel.mHandlerFinish.sendEmptyMessage(ShowHotel.ROOMS_UPDATED);
		
		Log.i("TAG",str);

		return null;
	}


	public void attach(ShowHotel showHotel) {
		mShowHotel = showHotel;
	}

	public void detach()
	{
		mShowHotel = null;
	}

	public Object getProgress() {
		return new Integer(0);
	}
*/
}

