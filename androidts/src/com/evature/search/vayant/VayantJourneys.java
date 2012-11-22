package com.evature.search.vayant;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class VayantJourneys {

	private static final String TAG = "VayantJourneys";
	public Journey mJourneys[];

	public VayantJourneys(JSONArray journeys) {
		mJourneys = new Journey[journeys.length()];
		for (int index = 0; index < journeys.length(); index++) {
			try {
				mJourneys[index] = new Journey(journeys.getJSONArray(index));
			} catch (JSONException e) {
				Log.e(TAG, "Bad journeys array");
			}
		}

		Log.d(TAG, "# of journeys recieved = " + String.valueOf(journeys.length()));
	}
}
