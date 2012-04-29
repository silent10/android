package com.softskills.evasearch;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.softskills.evasearch.database.HotelData;
import com.softskills.evasearch.database.HotelDetails;

public class EvaHotelDownloaderTask extends EvaDownloaderTask 
{
	private static final String TAG = "EvaHotelDownloaderTask";
		
	@Override
	protected void onProgressUpdate(Integer... values) {

		super.onProgressUpdate(values);
	}

	
	int mHotelIndex;

	public EvaHotelDownloaderTask(Context listener, int hotelndex) {
		attach((EvaDownloaderTaskInterface)listener);		
		mHotelIndex = hotelndex;
	}

	@Override
	protected Void doInBackground(Void... params) {

		HotelData hotelData = EvaSearchApplication.getDb().mHotelData[mHotelIndex];
		
		publishProgress();
		
		String hotelInfo = XpediaProtocol.getExpediaHotelInformation(hotelData.mSummary.mHotelId,
				EvaSettingsAPI.getCurrencyCode((Context)mListener));

		JSONObject jHotel;
		try {
			if(hotelInfo!=null)
			{
				jHotel = new JSONObject(hotelInfo);

				JSONObject jHotelInfo = jHotel.getJSONObject("HotelInformationResponse");


				hotelData.mDetails = new HotelDetails(jHotelInfo);

				String str= XpediaProtocol.getRoomInformationForHotel(hotelData.mSummary.mHotelId,
						EvaSearchApplication.getDb().mArrivalDateParam,
						EvaSearchApplication.getDb().mDepartureDateParam,
						EvaSettingsAPI.getCurrencyCode((Context)mListener),
						EvaSearchApplication.getDb().mNumberOfAdultsParam);


				hotelData.mSummary.updateRoomDetails(str);
				
				mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH;
				Log.i(TAG,str);				
			}
			else
			{
				mProgress = EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR;
			}

			
		} catch (JSONException e) {
			Log.e(TAG,"JSON exception getting hotel details");
			e.printStackTrace();
		}

		return null;
	}

	public int getHotelIndex() {
		return mHotelIndex;		
	}


}

