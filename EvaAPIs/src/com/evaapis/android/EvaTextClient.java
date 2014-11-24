package com.evaapis.android;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.support.v4.util.Pair;

import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.DownloadUrl;
import com.evature.util.Log;


    //Uses AsyncTask to create a task away from the main UI thread. This task takes a
	// URL string and uses it to create an HttpUrlConnection. Once the connection
	// has been established, the AsyncTask downloads the contents of the webpage as
	// an InputStream. Finally, the InputStream is converted into a string,
	// which is displayed in the UI by the AsyncTask's onPostExecute method.
	public class EvaTextClient extends AsyncTask<Void, Integer, EvaApiReply> {

		private static final String TAG = "EvaCallerTask";
		
		private final EvaComponent mEva;
		private final Object mCookie;  
		private final String mInputText;
		private final ArrayList<String> mNBest;
		private int mResponseId;
		private long startOfTextSearch;
		private boolean mEditLastUtterance;

		private String recordingKey;

		private boolean mFromSpeech;
		
		/****
		 * 
		 * @param inputText - text to send to Eva,   ignored if null
		 * @param responseId - response to send to Eva, ignored if -1
		 * @param cookie   - will be returned to the listener on callback
		 */
		public EvaTextClient( final EvaComponent eva,
								final String inputText, 
								final int responseId, 
								final Object cookie, 
								final boolean editLastUtterance) {
			mResponseId = responseId;
			mEva = eva;
			mInputText = inputText;
			mNBest = null;
			mCookie = cookie;
			mFromSpeech = false;
			mEditLastUtterance = editLastUtterance;
		}
		
		/****
		 * 
		 * @param nBestText - the N best results from voice recognition - passing all of them for Eva to choose the best one
		 * @param responseId - response to send to Eva, ignored if -1
		 * @param cookie   - will be returned to the listener on callback
		 */
		public EvaTextClient(final EvaComponent eva,
								final ArrayList<String> nBestText, 
								final Object cookie,
								final boolean editLastUtterance) {
			mResponseId = -1;
			mEva = eva;
			mNBest = nBestText;
			mFromSpeech = true; // hackish..
			mInputText = null;
			mCookie = cookie;
			mEditLastUtterance = editLastUtterance;
		}

		@Override
		protected EvaApiReply doInBackground(Void... non) {
			startOfTextSearch = System.nanoTime();
			String evatureUrl = mEva.mConfig.vproxyHost;
			
//			// TODO REMOVE!!!!!!!!!!!
//			if (BuildConfig.DEBUG)
//			evatureUrl = "http://10.0.0.52:8008"; 
			
			evatureUrl += "/"+mEva.mConfig.apiVersion+"?";
			evatureUrl += ("site_code=" + mEva.getSiteCode());
			evatureUrl += ("&api_key=" + mEva.getApiKey());
			//evatureUrl += ("&language=" + mLanguage);
			evatureUrl += ("&session_id=" + mEva.getSessionId());
			evatureUrl += ("&sdk_version="+EvaComponent.SDK_VERSION);
			evatureUrl += "&ffi_chains=true&ffi_statement=true";
			evatureUrl += "&from_speech="+(mFromSpeech ? "true" : "false");
			String language = mEva.getPreferedLanguage();
			if (language != null && !"".equals(language)) {
				evatureUrl += "&language="+language.replaceAll("-.*$", "");
			}
			if (mEva.getLocale() != null) {
				evatureUrl += ("&locale="+ mEva.getLocale());
			}
			else {
				Locale currentLocale = Locale.getDefault();
				evatureUrl += "&locale="+ currentLocale.getCountry(); 
			}
			
			
			if (mEva.getDeviceId() != null) {
				evatureUrl += "&uid="+mEva.getDeviceId();
			}
			if (mEva.getContext() != null) {
				evatureUrl += "&context="+mEva.getContext();
			}
			if (mEva.getScope() != null) {
				evatureUrl += "&scope="+mEva.getScope();
			}
			if (mResponseId != -1) {
				evatureUrl += ("&dialog_response="+mResponseId);
			}
			if (mEva.mConfig.appVersion != null) {
				evatureUrl += "&app_version="+ mEva.mConfig.appVersion;
			}
			try {
				evatureUrl += "&time_zone="+URLEncoder.encode((""+TimeZone.getDefault().getRawOffset()/3600000.0).replaceFirst("\\.0+$",  ""), "UTF-8");
				evatureUrl += "&android_ver="+URLEncoder.encode(String.valueOf(android.os.Build.VERSION.RELEASE), "UTF-8");
				evatureUrl += "&device="+URLEncoder.encode(android.os.Build.MODEL, "UTF-8");
				
				HashMap<String, String> extraParams = mEva.getExtraParams();
				for (String key : extraParams.keySet()) {
					String val = extraParams.get(key);
					if (val != null)
						evatureUrl += "&"+key+"="+URLEncoder.encode(val, "UTF-8");
				}
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "UnsupportedEncodingException", e);
			}
			
			if (mNBest != null) {
				for (String input: mNBest) {
					try {
						evatureUrl += ("&input_text=" + URLEncoder.encode(input, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						Log.e(TAG, "UnsupportedEncodingException", e);
					}
				}
			}
			if (mInputText != null) {
				try {
					evatureUrl += ("&input_text=" + URLEncoder.encode(mInputText, "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Log.e(TAG, "UnsupportedEncodingException", e); 
				}
			}
//			String externalIpAddress = ExternalIpAddressGetter.getExternalIpAddr();
//			if (externalIpAddress != null) {
//				evatureUrl += ("&ip_addr=" + externalIpAddress);
//			}
			double latitude = EvatureLocationUpdater.getLatitude();
			if (latitude != EvatureLocationUpdater.NO_LOCATION) {
				double longitude = EvatureLocationUpdater.getLongitude();
				evatureUrl += ("&longitude=" + longitude + "&latitude=" + latitude);
			}
			if (mEditLastUtterance) {
				evatureUrl += "&edit_last_utterance=true";
			}
			if (recordingKey != null) {
				evatureUrl += "&recording_key="+recordingKey;
			}
			
			int retries = 0;
			while (retries < 3) {
				Log.i(TAG, "<< Sending Eva URL = " + evatureUrl);
				try {
					String result = DownloadUrl.sget(evatureUrl);
					EvaApiReply apiReply = new EvaApiReply(result);
					return apiReply;
				} catch (IOException e) {
					Log.w(TAG, "IOException in request to Evature: "+e.getMessage());
					retries++;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
			// had 3 IOExceptions in a row - give up
			EvaApiReply errorReply = new EvaApiReply("{\"status\": false }");
			errorReply.errorMessage = EvaComponent.NETWORK_ERROR;
			return errorReply;

		}


		@Override
		protected void onPostExecute(EvaApiReply apiReply) { // onPostExecute displays the results of the AsyncTask.
			
			// coming from text search
			if (mEva.isDebug()) {
				JSONObject debugData = new JSONObject();
				try {
					if (apiReply.JSONReply != null) {
						debugData.put("Time in HTTP Execute", ((System.nanoTime() - startOfTextSearch)/1000000)+"ms");
						apiReply.JSONReply.put("debug", debugData);
					}
				} catch (JSONException e) {
					Log.e(TAG, "Failed setting debug data", e);
				}
			}
						
			mEva.onEvaReply(apiReply, mCookie);
		}

		public void setRecordingKey(String recordingKey) {
			this.recordingKey= recordingKey;
		}
		
	}

