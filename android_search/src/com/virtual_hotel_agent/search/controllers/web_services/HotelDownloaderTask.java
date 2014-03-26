package com.virtual_hotel_agent.search.controllers.web_services;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.hotel.request.InformationRequest;
import com.ean.mobile.request.RequestProcessor;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;

public class HotelDownloaderTask extends DownloaderTask {
	private static final String TAG = HotelDownloaderTask.class.getSimpleName();
	private long mHotelId;

	public HotelDownloaderTask(DownloaderTaskListenerInterface listener, long hotelId) {
		super( R.string.HOTEL);
		Log.d(TAG, "CTOR");
		attach(listener);
		mHotelId = hotelId;
	}

	@Override
	protected Object doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");

		if (isCancelled()) {
			Log.w(TAG, "thread was canceled!");
			return null;
		}
		mProgress = DownloaderStatus.Started;
		
		publishProgress();

		try {
			final Hotel hotel = MyApplication.HOTEL_ID_MAP.get(mHotelId);
			HotelInformation hotelInformation = RequestProcessor.run(new InformationRequest(hotel));
			MyApplication.EXTENDED_INFOS.put(mHotelId, hotelInformation);
			mProgress = DownloaderStatus.Finished;
        } catch (EanWsError ewe) {
            Log.d(TAG, "Unexpected error occurred within the api", ewe);
            mProgress = DownloaderStatus.FinishedWithError;
        } catch (UrlRedirectionException ure) {
        	MyApplication.sendRedirectionToast();
        	mProgress = DownloaderStatus.FinishedWithError;
        }

        return null;
	}
        
//		JSONObject jHotel = XpediaProtocolStatic.getExpediaHotelInformation(mContext, hotelData.mSummary.mHotelId,
//				SettingsAPI.getCurrencyCode(mContext));

//		if (jHotel == null) {
//			mProgress = DownloaderStatus.FinishedWithError;
//			return null;
//		}
		
//		try {
//
//			JSONObject jHotelInfo = jHotel.getJSONObject("HotelInformationResponse");
//
//			hotelData.mDetails = new HotelDetails(jHotelInfo);
//
//			mProgress = DownloaderStatus.Finished;
//
//			
//			return jHotel;
//
//		} catch (JSONException e) {
//			MainActivity.LogError(TAG, "JSON exception getting hotel details", e);
//			return jHotel;
//		}
//	}

	public long getHotelId() {
		return mHotelId;
	}

}
