package com.evature.search.vayant;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class Journey {

	public BookingSolution mBookingSolutions[];

	public Journey(JSONArray solutions) {
		mBookingSolutions = new BookingSolution[solutions.length()];
		for (int index = 0; index < solutions.length(); index++) {
			try {
				mBookingSolutions[index] = new BookingSolution(solutions.getJSONObject(index));
			} catch (JSONException e) {
				Log.e("VAYANT", "Bad journeys array");
			}
		}

	}
}
