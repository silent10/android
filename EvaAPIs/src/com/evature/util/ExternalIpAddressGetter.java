package com.evature.util;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.evaapis.EvaAPIs;
import com.evaapis.EvaBaseActivity;

public class ExternalIpAddressGetter {

	private static final String TAG = ExternalIpAddressGetter.class.getSimpleName();
	private Handler mHandler = new Handler();

	public ExternalIpAddressGetter() {
		mHandler.removeCallbacks(mRunnable); // We remove any existing callbacks to the handler before adding the
												// new handler, to make absolutely sure we don't get more callback
												// events than we want.
		mHandler.postDelayed(mRunnable, 1000); // 1 second delay
	}

	private Runnable mRunnable = new Runnable() {
		public void run() {
			new GetExternalIpAddress().execute(); // Get the external IP address (in the background)
			mHandler.postDelayed(mRunnable, 3 * 60 * 1000); // and repeat every 3 minutes!
		}
	};

	// Uses AsyncTask to create a task away from the main UI thread. This task takes a URL string and uses it to create
	// an HttpUrlConnection. Once the connection has been established, the AsyncTask downloads the contents of the
	// webpage as an InputStream. Finally, the InputStream is converted into a string, which is saved to the Model.
	private class GetExternalIpAddress extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... ignore) {
			try {
				Log.d(TAG, "Requesting external IP address");
				String result = DownloadUrl.get(EvaAPIs.API_ROOT + "/whatismyip");
				if (result == null) {
					return null;
				}
				JSONObject jResult = new JSONObject(result);
				return jResult.getString("ip_address");
			} catch (IOException caughtException) {
				// http://code.google.com/p/acra/wiki/AdvancedUsage#Sending_reports_for_caught_exceptions_or_for_unexpected_applicat
				// ErrorReporter.getInstance().handleException(caughtException);
				// This can sometimes fail - no need to log...
				return null;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.w(TAG, "attempt to get ip_address failed on JSON parse");
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.
			if (result != null) {
				Log.d(TAG, "External IP address = " + result);
				EvaBaseActivity.setExternalIpAddress(result);
			} else {
				Log.d(TAG, "My external IP resolver returned null!");
			}
		}
	}

}
