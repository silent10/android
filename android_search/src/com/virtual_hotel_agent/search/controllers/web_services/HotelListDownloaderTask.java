package com.virtual_hotel_agent.search.controllers.web_services;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.exception.UrlRedirectionException;
import com.ean.mobile.hotel.HotelList;
import com.ean.mobile.hotel.request.ListRequest;
import com.ean.mobile.request.Request;
import com.ean.mobile.request.RequestProcessor;
import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;

public class HotelListDownloaderTask extends DownloaderTask {

	private static final String TAG = HotelListDownloaderTask.class.getSimpleName();
	// String mSearchQuery;
//	String mCurrencyCode;
	EvaApiReply apiReply;

	/**
     * The formatter used for paring DateTime objects from returned api date fields.
     */
    private static final DateTimeFormatter API_DATE_PARSER = DateTimeFormat.forPattern("MM/dd/YYYY");

	public HotelListDownloaderTask() {
		super(R.string.HOTELS);
	}

	public void initialize(DownloaderTaskListenerInterface listener, EvaApiReply apiReply/*, String currencyCode*/) {
		Log.i(TAG, "CTOR");
		// mSearchQuery = searchQuery;
		this.apiReply = apiReply;
		attach(listener);
//		mCurrencyCode = currencyCode;
	}

//	void createHotelData(JSONObject hotelListResponseJSON) {
//
//		if (hotelListResponseJSON == null) {
//			return;
//		}
//		try {
////			MyApplication.getDb().EvaDatabaseUpdateExpedia(hotelListResponseJSON);
//			XpediaDatabase db = new XpediaDatabase(hotelListResponseJSON);
//			MyApplication.setDb(db);
//			
//			
////			if (MyApplication.getDb().mHotelData == null) {
////				return false;
////			}
////			else {
////				 MyApplication.setDb(db);
////			}
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		}
//
//	}

	@Override
	protected JSONObject doInBackground(Void... params) {

		Log.i(TAG, "doInBackground: start");
		// String searchQuery = EvaProtocol.getEvatureResponse(mQueryString);
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_EXPEDIA_HOTEL_FETCH;
		publishProgress();
		Log.i(TAG, "doInBackground: Calling Expedia");
//		JSONObject hotelListResponse = xpediaProtocol.getExpediaAnswer(context, apiReply, MyApplication.getExpediaAppState(), mCurrencyCode);
//		if (hotelListResponse == null) {
//			Log.w(TAG, "null hotelist response!");
//		}
		//mProgress = EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA;
		
		try {
			List<NameValuePair> requestParams = new ArrayList<NameValuePair>();
			for (String key : apiReply.ean.keySet()) {
				String value = apiReply.ean.get(key);
				requestParams.add(new BasicNameValuePair(key, value));
			}
			requestParams.addAll(Request.getBasicUrlParameters());
            final ListRequest request = new ListRequest(requestParams);

            HotelList listResponse = RequestProcessor.run(request);
			MyApplication.updateFoundHotels(listResponse, true);

              
			MyApplication.departureDate = API_DATE_PARSER.parseLocalDate(apiReply.ean.get("departureDate"));

            mProgress = DownloaderStatus.Finished;
        } catch (EanWsError ewe) {
            // If this exception occurs, it's likely an input error and should be recoverable.
            Log.d(TAG, "An APILevel Exception occurred.", ewe);
            mProgress = DownloaderStatus.FinishedWithError;
        } catch (UrlRedirectionException ure) {
            MyApplication.sendRedirectionToast();
            mProgress = DownloaderStatus.FinishedWithError;
        }
		
		if (isCancelled()) {
			return null;
		}
		return null;
	}
	
   
    
}
