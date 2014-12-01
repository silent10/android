package com.virtual_hotel_agent.search.controllers.web_services;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.hotel.request.InformationRequest;
import com.ean.mobile.request.RequestProcessor;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;

public class HotelDownloaderTask extends DownloaderTask {
	private static final String TAG = HotelDownloaderTask.class.getSimpleName();
	private HotelItemClicked hotelItemSelectedEvent;
	public EanWsError eanWsError;

	public HotelDownloaderTask(DownloaderTaskListenerInterface listener, HotelItemClicked event) {
		super( R.string.HOTEL);
		Log.d(TAG, "CTOR");
		attach(listener);
		hotelItemSelectedEvent = event;
	}

	@Override
	protected Object doInBackground(Void... params) {
		Log.d(TAG, "doInBackground()");

		if (isCancelled()) {
			Log.w(TAG, "thread was canceled!");
			mProgress = DownloaderStatus.FinishedWithError;
			return null;
		}
		mProgress = DownloaderStatus.Started;
		
		publishProgress();

		try {
			eanWsError = null;
			final Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(hotelItemSelectedEvent.hotelId);
			HotelInformation hotelInformation = RequestProcessor.run(new InformationRequest(hotel));
			VHAApplication.EXTENDED_INFOS.put(hotelItemSelectedEvent.hotelId, hotelInformation);
			if (hotelInformation != null && hotelInformation.images != null && hotelInformation.images.size() > 0) {
				hotel.mainHotelImageTuple.mainUrl = hotelInformation.images.get(0).mainUrl;
			}
			mProgress = DownloaderStatus.Finished;
        } catch (EanWsError ewe) {
            Log.d(TAG, "Unexpected error occurred within the api", ewe);
            eanWsError = ewe;
            mProgress = DownloaderStatus.FinishedWithError;
        } catch (UrlRedirectionException ure) {
        	VHAApplication.sendRedirectionToast();
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
//			VHAApplication.logError(TAG, "JSON exception getting hotel details", e);
//			return jHotel;
//		}
//	}

	public HotelItemClicked getHotelEvent() {
		return hotelItemSelectedEvent;
	}

}
