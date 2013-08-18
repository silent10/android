package com.evature.util;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.evaapis.EvaAPIs;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

@Singleton
public class ExternalIpAddressGetter {

	private static final String TAG = ExternalIpAddressGetter.class.getSimpleName();
	private Handler mHandler = new Handler();
	
	private Runnable mRunnable;
	
	private static String IpAddress;
	
	private static final long CHECK_IP_EVERY_MS = 5 * 60 * 1000; // repeat check every X minutes
	
	@Inject protected Injector injector;
	private long lastTimeChecked = -1;
	
	public ExternalIpAddressGetter() {
		mRunnable = new Runnable() {
			public void run() {
				executeGetIpAddr();
				mHandler.postDelayed(mRunnable, CHECK_IP_EVERY_MS); // and repeat every X minutes!
			}
		};
	}
	
	public void start() {
		
		mHandler.removeCallbacks(mRunnable); // We remove any existing callbacks to the handler before adding the
											// new handler, to make absolutely sure we don't get more callback
											// events than we want.
		mHandler.postDelayed(mRunnable, 1000); // 1 second delay
	}
	
	public void pause() {
		mHandler.removeCallbacks(mRunnable); // We remove any existing callbacks
	}


	public void executeGetIpAddr() {
		if (System.currentTimeMillis() - lastTimeChecked < CHECK_IP_EVERY_MS-100) {
			Log.d(TAG, "Not spamming IP check - too soon");
			return;
		}
		lastTimeChecked = System.currentTimeMillis();
		
		GetExternalIpAddress ipGetter = injector.getInstance(GetExternalIpAddress.class);
		ipGetter.execute(); // Get the external IP address (in the background)
	}

	public static String getExternalIpAddr() {
		return IpAddress;
	}
	public static void setExternalIpAddr(String ipaddr) {
		IpAddress = ipaddr;
	}



	// Uses AsyncTask to create a task away from the main UI thread. This task takes a URL string and uses it to create
	// an HttpUrlConnection. Once the connection has been established, the AsyncTask downloads the contents of the
	// webpage as an InputStream. Finally, the InputStream is converted into a string, which is saved to the Model.
	private static class GetExternalIpAddress extends AsyncTask<String, Integer, String> {
		@Inject DownloadUrl urlDownloader;
		
		@Override
		protected String doInBackground(String... ignore) {
			try {
				Log.d(TAG, "Requesting external IP address");
				String result = urlDownloader.get(EvaAPIs.API_ROOT + "/whatismyip");
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
				IpAddress = result;
			} else {
				Log.d(TAG, "My external IP resolver returned null!");
			}
		}
	}

}
