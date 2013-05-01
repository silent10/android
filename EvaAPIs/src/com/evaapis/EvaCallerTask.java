package com.evaapis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.evature.util.DownloadUrl;


    //Uses AsyncTask to create a task away from the main UI thread. This task takes a
	// URL string and uses it to create an HttpUrlConnection. Once the connection
	// has been established, the AsyncTask downloads the contents of the webpage as
	// an InputStream. Finally, the InputStream is converted into a string,
	// which is displayed in the UI by the AsyncTask's onPostExecute method.
	public class EvaCallerTask extends AsyncTask<String, Integer, String> {

		private static final String TAG = "EvaCallerTask";
		private final Context mContext;

		EvaSearchReplyListener mSearchReplyListener;
		
		EvaCallerTask(Context context,EvaSearchReplyListener esrl) {
			mContext = context;
			mSearchReplyListener = esrl;
		}

		@Override
		protected String doInBackground(String... urls) {
			// params comes from the execute() call: params[0] is the url.
			String API_KEY = EvaAPIs.API_KEY;
			String SITE_CODE = EvaAPIs.SITE_CODE;
			String evatureUrl = "http://freeapi.evature.com/api/v1.0?from_speech";
			evatureUrl += ("&site_code=" + SITE_CODE);
			evatureUrl += ("&api_key=" + API_KEY);
			evatureUrl += ("&language=" + urls[1].substring(0, 2)); // Add the language code!
			try {
				evatureUrl += ("&input_text=" + URLEncoder.encode(urls[0], "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace(); // TODO Auto-generated catch block
			}
			String externalIpAddress = EvaBaseActivity.getmExternalIpAddress();
			if (externalIpAddress != null && (externalIpAddress.length()>0) && (externalIpAddress.trim().length()>0)) {
				evatureUrl += ("&ip_addr=" + externalIpAddress);
			}
			EvatureLocationUpdater location;
			try {
				location = EvatureLocationUpdater.getInstance();
				double longitude = location.getLongitude();
				double latitude = location.getLatitude();
				if (latitude != 0 && longitude != 0) {
					evatureUrl += ("&longitude=" + longitude + "&latitude=" + latitude);
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			Log.d(TAG, "Eva URL = " + evatureUrl);
			try {
				return DownloadUrl.get(evatureUrl);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}


		@Override
		protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.
			Log.d(TAG, "Got EVA Response!");
			EvaApiReply apiReply = new EvaApiReply(result);		
			
			mSearchReplyListener.onEvaReply(apiReply);
			
		}
	}

