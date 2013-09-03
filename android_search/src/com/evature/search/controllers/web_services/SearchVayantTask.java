package com.evature.search.controllers.web_services;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.evaapis.EvaApiReply;
import com.evaapis.EvaLocation;
import com.evaapis.flow.FlowElement;
import com.evature.search.MyApplication;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.vayant.VayantJourneys;

public class SearchVayantTask extends EvaDownloaderTask {

	private static final String TAG = "SearchVayantTask";
	MainActivity mMainActivity;
	private EvaApiReply mApiReply;
	private FlowElement mFlowElement;

	public SearchVayantTask(MainActivity mainActivity, EvaApiReply apiReply, FlowElement flowElement) {
		mMainActivity = mainActivity;
		mApiReply = apiReply;
		mFlowElement = flowElement;
	}

	@Override
	protected String doInBackground(Void... unusedParams) {
		String airport_code0 = null;
		String airport_code1 = null;
		if (mApiReply.locations.length >= 2) {
			EvaLocation loc1, loc2;
			if (mFlowElement != null && mFlowElement.RelatedLocations.length > 1) {
				loc1 = mFlowElement.RelatedLocations[0];
				loc2 = mFlowElement.RelatedLocations[mFlowElement.RelatedLocations.length-1];
			}
			else {
				loc1 = mApiReply.locations[0];
				loc2 = mApiReply.locations[1];
			}
			airport_code0 = loc1.airportCode();
			airport_code1 = loc2.airportCode();
			Log.i(TAG, "airport_codes = " + airport_code1);
			
			if (airport_code0 != null && airport_code1 != null) {
				// Encode the request to Vayant:
				JSONObject obj = new JSONObject();
	
				try { // Encoding examples: http://code.google.com/p/json-simple/wiki/EncodingExamples
					obj.put("User", "iftah@evature.com"); // "tal@evature.com");
					obj.put("Pass", "91bab377e2d27afff60160c0508621d1d924b5f7");//"cfccc293b6d6398404e95100693cefd8457c6a0d");
					obj.put("Origin", airport_code0);
					obj.put("Destination", airport_code1);
					obj.put("Environment", "fast_search_1_0");
					
					String dateStr;
					if (loc1.Departure != null && loc1.Departure.Date != null) {
						dateStr = loc1.Departure.Date;
					}
					else {
						// using today
						dateStr = String.format(Locale.US, "%1$tY-%1$tm-%1$te", Calendar.getInstance().getTime());
					}
					obj.put("DepartureFrom", dateStr); //"2012-06-28");
					obj.put("DepartureTo", dateStr); //"2012-06-28");
					obj.put("Response", "json");
					obj.put("MaxSolutions", 100);
				} catch (JSONException e) {
					// This should not happen!
					e.printStackTrace();
				}
				String json_dump = obj.toString();
				try {
					return callApi(json_dump);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return "";
	}

	// Call the Vayant API, sending the JSON request and receiving the JSON reply.
	// Tips on posting a JSON object: http://mycenes.wordpress.com/tag/json/
	private String callApi(String data) throws IOException {
		final String vayant_url = "http://fs-json.demo.vayant.com:7081";
		URL url = new URL(vayant_url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000 /* milliseconds */);
		conn.setConnectTimeout(15000 /* milliseconds */);
		conn.setRequestMethod("POST");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("Accept", "application/json");
		conn.addRequestProperty("Accept-Encoding","gzip");
		conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
		conn.getOutputStream().write(data.getBytes());
		conn.getOutputStream().flush();
		// Starts the query
		conn.connect();
		
		int response = conn.getResponseCode();
		Log.d(TAG, "The response is: " + response);
		
		// for some reason the GZIPINputStream is needed (HttpURLConnection should handle the gzip) - I guess Vayant doesn't return the right header but does encode the content
		// Read from web: http://stackoverflow.com/a/1381784/78234
		Reader r = new InputStreamReader(new GZIPInputStream(conn.getInputStream()), "UTF-8");
		StringBuilder buf = new StringBuilder();
		char[] sBuffer = new char[512];
		int readBytes = 0;
		while ((readBytes = r.read(sBuffer)) != -1) {
			buf.append(sBuffer, 0, readBytes);
		}
		String str = buf.toString();
		Log.d(TAG, "Response read "+str.length()+" chars");
		return str;
	}

	@Override
	protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.
		if (result != null) {
			JSONObject vayantReply;
			VayantJourneys journeys = null;
			try {
				vayantReply = new JSONObject(result);
				result = vayantReply.toString(2);
				journeys = new VayantJourneys(vayantReply.getJSONArray("Journeys"));
				MyApplication.getJourneyDb().mJourneys = journeys;
				Log.d(TAG, "JSON parsed");
			} catch (JSONException e) {
				Log.e("VAYANT", "Bad reply");
			}
			
			Log.d(TAG, "Got Vayant Response!");
			if (mMainActivity != null) {
				mMainActivity.setVayantReply(result);
			}
			mProgress = DownloaderStatus.Finished;
		} else {
			Log.e(TAG, "Error getting Vayant Response!");
			mProgress = DownloaderStatus.FinishedWithError;
		}
		super.onPostExecute(result);
		
	}
}
