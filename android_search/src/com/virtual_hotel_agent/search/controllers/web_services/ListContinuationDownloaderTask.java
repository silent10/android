package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.request.ListRequest;
import com.ean.mobile.request.RequestProcessor;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

public class ListContinuationDownloaderTask extends DownloaderTask {
	private static final String TAG = ListContinuationDownloaderTask.class.getSimpleName();
	private String mCurrencyCode;
	private HotelListFragment hotelFragment;

	public ListContinuationDownloaderTask(HotelListFragment listener, String currencyCode) {
		super(R.string.HOTELS);
		Log.i(TAG, "CTOR");
		attach(listener);
		hotelFragment = listener;
		mCurrencyCode = currencyCode;
		
	}

	@Override
	protected void onPostExecute(Object result) {

		if (hotelFragment == null)
			return;

		super.onPostExecute(result);
	}

	@Override
	protected Object doInBackground(Void... params) {
		Log.i(TAG, "Do in background");
//
//		JSONObject hotelListResponse = XpediaProtocolStatic.getExpediaNext(hotelFragment.getActivity(), mNextQuery, mCurrencyCode);
//		if (hotelListResponse == null)
//			VHAApplication.logError(TAG, "Response for next null");
//		mProgress = DownloaderStatus.MadeSomeProgress;
//		onProgressUpdate();
//		
//		if (false == addHotelData(hotelListResponse)) {
//			mProgress = DownloaderStatus.FinishedWithError;
//		} else {
//			mProgress = DownloaderStatus.Finished;
//		}

		try {
            final ListRequest request = new ListRequest(
                VHAApplication.cacheKey,
                VHAApplication.cacheLocation);

            VHAApplication.updateFoundHotels(RequestProcessor.run(request));
            mProgress = DownloaderStatus.Finished;
        } catch (EanWsError ewe) {
            Log.d(TAG, "An APILevel Exception occurred.", ewe);
            mProgress = DownloaderStatus.FinishedWithError;
        } catch (UrlRedirectionException ure) {
        	VHAApplication.sendRedirectionToast();
        	mProgress = DownloaderStatus.FinishedWithError;
        }
		
		//return hotelListResponse;
		return null;
	}

}
