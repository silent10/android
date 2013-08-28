package com.evaapis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.util.Log;

import com.evature.util.DownloadUrl;
import com.evature.util.ExternalIpAddressGetter;
import com.google.inject.Inject;


    //Uses AsyncTask to create a task away from the main UI thread. This task takes a
	// URL string and uses it to create an HttpUrlConnection. Once the connection
	// has been established, the AsyncTask downloads the contents of the webpage as
	// an InputStream. Finally, the InputStream is converted into a string,
	// which is displayed in the UI by the AsyncTask's onPostExecute method.
	public class EvaCallerTask extends AsyncTask<Void, Integer, String> {

		private static final String TAG = "EvaCallerTask";
		
		@Inject DownloadUrl urlDownloader;

		EvaSearchReplyListener mSearchReplyListener;
		private String mSessionId = "1";
		private String mLanguage;
		//String mLanguage = "US";
		private Object mCookie; // 
		private String mInputText;
		int mResponseId;
		
		/****
		 * 
		 * @param searchReplyListener
		 * @param languageCode
		 * @param inputText - text to send to Eva,   ignored if null
		 * @param responseId - response to send to Eva, ignored if -1
		 * @param sessionId
		 * @param cookie   - will be returned to the listener on callback
		 */
		public void initialize(EvaSearchReplyListener searchReplyListener, 
								String sessionId,  
								String languageCode,
								String inputText, 
								int responseId, 
								Object cookie) {
			mSearchReplyListener = searchReplyListener;
			mSessionId = sessionId;
			mLanguage = languageCode;
			mResponseId = responseId;
			mInputText = inputText;
			mCookie = cookie;
		}

		@Override
		protected String doInBackground(Void... non) {
			String API_KEY = EvaAPIs.API_KEY;
			String SITE_CODE = EvaAPIs.SITE_CODE;
			String evatureUrl = EvaAPIs.API_ROOT + "/api/v1.0?";
			evatureUrl += ("site_code=" + SITE_CODE);
			evatureUrl += ("&api_key=" + API_KEY);
			//evatureUrl += ("&language=" + mLanguage);
			evatureUrl += ("&session_id=" + mSessionId);
			if (mLanguage != null && !"".equals(mLanguage)) {
				evatureUrl += "&language="+mLanguage;
			}
			evatureUrl += ("&locale="+EvaAPIs.locale);
			if (mResponseId != -1) {
				evatureUrl += ("&dialog_response="+mResponseId);
			}
			if (mInputText != null) {
				try {
					evatureUrl += ("&input_text=" + URLEncoder.encode(mInputText, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace(); 
				}
			}
			String externalIpAddress = ExternalIpAddressGetter.getExternalIpAddr();
			if (externalIpAddress != null && (externalIpAddress.length()>0) && (externalIpAddress.trim().length()>0)) {
				evatureUrl += ("&ip_addr=" + externalIpAddress);
			}
			double longitude = EvatureLocationUpdater.getLongitude();
			double latitude = EvatureLocationUpdater.getLatitude();
			if (latitude != -1 && longitude != -1) {
				evatureUrl += ("&longitude=" + longitude + "&latitude=" + latitude);
			}

			Log.i(TAG, "<< Sending Eva URL = " + evatureUrl);
			try {
				return urlDownloader.get(evatureUrl);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}


		@Override
		protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.
			EvaApiReply apiReply = new EvaApiReply(result);		
			
			mSearchReplyListener.onEvaReply(apiReply, mCookie);
			
		}

		
	}

