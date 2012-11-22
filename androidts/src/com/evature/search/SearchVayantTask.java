package com.evature.search;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.evaapis.EvaApiReply;
import com.evaapis.RequestAttributes;
import com.evature.search.vayant.VayantJourneys;

public class SearchVayantTask extends AsyncTask<String, Integer, String> {

	private static final String TAG = "SearchVayantTask";
	MainActivity mMainActivity;
	private EvaApiReply mApiReply;

	SearchVayantTask(MainActivity mainActivity, EvaApiReply apiReply) {
		mMainActivity = mainActivity;
		mApiReply = apiReply;
	}

	@Override
	protected String doInBackground(String... unusedParams) {
		String airport_code0 = "";
		String airport_code1 = "";
		if (mApiReply.locations.length >= 2) {
			Log.i(TAG, "Eva returned 2 locations!");
			boolean airplane = false;
			if (mApiReply.locations[0].requestAttributes != null) {
				RequestAttributes request_attributes = mApiReply.locations[0].requestAttributes;
				if (request_attributes.transportType.size() > 0) {
					airplane = request_attributes.transportType.get(0).equals("Airplane");
					Log.d(TAG, "airplane = " + String.valueOf(airplane));
				}
			}
			if (airplane || mApiReply.flightAttributes != null) {
				Log.i(TAG, "Eva said transport type is an airplane or flight attribute!");
			}
			if (mApiReply.locations[0].allAirportCode != null)
				airport_code0 = mApiReply.locations[0].allAirportCode;
			else
				airport_code0 = mApiReply.locations[0].airports.get(0);
			Log.i(TAG, "airport_code0 = " + airport_code0);

			if (mApiReply.locations[1].allAirportCode != null)
				airport_code1 = mApiReply.locations[1].allAirportCode;
			else {
				if (mApiReply.locations[1].airports != null && mApiReply.locations[1].airports.size() > 0) {
					airport_code1 = mApiReply.locations[1].airports.get(0);
				}
			}
			Log.i(TAG, "airport_code1 = " + airport_code1);
		}
		if (!airport_code0.equals("") && !airport_code1.equals("")) {
			// Encode the request to Vayant:
			JSONObject obj = new JSONObject();

			try { // Encoding examples: http://code.google.com/p/json-simple/wiki/EncodingExamples
				obj.put("User", "tal@evature.com");
				obj.put("Pass", "cfccc293b6d6398404e95100693cefd8457c6a0d");
				JSONArray origins = new JSONArray();
				origins.put(airport_code0);
				obj.put("Origin", origins);
				obj.put("Destination", airport_code1);
				obj.put("Environment", "fast_search_1_0");
				obj.put("DepartureFrom", "2012-06-28");
			} catch (JSONException e) {
				// This should not happen!
				e.printStackTrace();
			}
			String json_dump = obj.toString();
			try {
				return callApi(json_dump);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
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
		conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
		conn.getOutputStream().write(data.getBytes());
		conn.getOutputStream().flush();
		// Starts the query
		conn.connect();
		int response = conn.getResponseCode();
		Log.d(TAG, "The response is: " + response);
		// Read from web: http://stackoverflow.com/a/1381784/78234
		Reader r = new InputStreamReader(conn.getInputStream(), "UTF-8");
		StringBuilder buf = new StringBuilder();
		while (true) {
			int ch = r.read();
			if (ch < 0)
				break;
			buf.append((char) ch);
		}
		String str = buf.toString();
		JSONObject vayantReply;
		VayantJourneys journeys = null;
		try {
			vayantReply = new JSONObject(str);
			journeys = new VayantJourneys(vayantReply.getJSONArray("Journeys"));
			MyApplication.getDb().mJourneys = journeys;
		} catch (JSONException e) {
			Log.e("VAYANT", "Bad reply");
		}

		return journeys.toString();
	}

	@Override
	protected void onPostExecute(String result) { // onPostExecute displays the results of the AsyncTask.

		if (result != null) {
			Log.d(TAG, "Got Vayant Response!");
			if (mMainActivity != null) {
				mMainActivity.setVayantReply();
			}
		} else {
			Log.w(TAG, "Did NOT get Vayant Response!");
			Log.w(TAG, "did get this: " + result);
		}

	}
}
