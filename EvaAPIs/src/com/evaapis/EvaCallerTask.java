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
	public class EvaCallerTask extends AsyncTask<String, Integer, String> {

		private static final String TAG = "EvaCallerTask";
		
		@Inject DownloadUrl urlDownloader;

		EvaSearchReplyListener mSearchReplyListener;
		String mSessionId = "1";
		String mLanguage = "US";
		Object mCookie; // to be returned to the listener on callback
		
		public void initialize(EvaSearchReplyListener searchReplyListener, String sessionId, String languageCode, Object cookie) {
			mSearchReplyListener = searchReplyListener;
			mSessionId = sessionId;
			mLanguage = languageCode.substring(0, 2);
			mCookie = cookie;
		}

		@Override
		protected String doInBackground(String... inputText) {
			String API_KEY = EvaAPIs.API_KEY;
			String SITE_CODE = EvaAPIs.SITE_CODE;
			String evatureUrl = EvaAPIs.API_ROOT + "/api/v1.0?from_speech"; // TODO: from_speech not always true
			evatureUrl += ("&site_code=" + SITE_CODE);
			evatureUrl += ("&api_key=" + API_KEY);
			evatureUrl += ("&language=" + mLanguage);
			evatureUrl += ("&session_id=" + mSessionId);
			try {
				evatureUrl += ("&input_text=" + URLEncoder.encode(inputText[0], "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); 
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

			Log.d(TAG, "Eva URL = " + evatureUrl);
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

