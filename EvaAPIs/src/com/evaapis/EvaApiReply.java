package com.evaapis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.util.Log;

public class EvaApiReply {
	private final String TAG = "EvaApiReply";
	public String sayIt = null;
	public EvaChat chat = null;
	public EvaLocation[] locations;
	public EvaLocation[] alt_locations;
	public Map<String, String> ean = null;
	public FlightAttributes flightAttributes = null;

	public EvaApiReply(String fullReply) {
		Log.d(TAG, "CTOR");
		try {
			JSONObject jFullReply = new JSONObject(fullReply);
			boolean status = jFullReply.getBoolean("status");
			if (status) {
				JSONObject jApiReply = jFullReply.getJSONObject("api_reply");
				Log.d(TAG, "api_reply: " + jApiReply.toString(2));
				if (jApiReply.has("Chat"))
					chat = new EvaChat(jApiReply.getJSONObject("Chat"));
				if (jApiReply.has("Say It"))
					sayIt = jApiReply.getString("Say It");
				if (jApiReply.has("Locations")) {
					JSONArray jLocations = jApiReply.getJSONArray("Locations");
					locations = new EvaLocation[jLocations.length()];
					for (int index = 0; index < jLocations.length(); index++) {
						locations[index] = new EvaLocation(jLocations.getJSONObject(index));
					}
				}
				if (jApiReply.has("Alt Locations")) {
					JSONArray jLocations = jApiReply.getJSONArray("Alt Locations");
					alt_locations = new EvaLocation[jLocations.length()];
					for (int index = 0; index < jLocations.length(); index++) {
						alt_locations[index] = new EvaLocation(jLocations.getJSONObject(index));
					}
				}
				if (jApiReply.has("ean")) {
					JSONObject jEan = jApiReply.getJSONObject("ean");
					@SuppressWarnings("unchecked")
					Iterator<String> nameItr = jEan.keys();
					ean = new HashMap<String, String>();
					while (nameItr.hasNext()) {
						String key = nameItr.next().toString();
						String value = jEan.getString(key);
						ean.put(key, value);
					}
				}
				if (jApiReply.has("Flight Attributes")) {
					flightAttributes = new FlightAttributes(jApiReply.getJSONObject("Flight Attributes"));
				}

			}
		} catch (JSONException e) {
			Log.e(TAG, "Bad EVA reply!");
		}
	}

	public boolean isHotelSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return false; // It is a train search!
			}
			if (locations[1].actions == null) {
				return true;
			} else {
				List<String> actions = Arrays.asList(locations[1].actions);
				if (actions.contains("Get Accommodation"))
					return true;
			}
		}
		return false;
	}

	public boolean isFlightSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return false; // It is a train search!
			}
			if (locations[1].actions == null) {
				return true;
			} else {
				List<String> actions = Arrays.asList(locations[1].actions);
				if (actions.contains("Get There"))
					return true;
			}
		}
		return false;
	}

	public boolean isTrainSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return true; // It is a train search!
			}
		}
		return false;
	}

}
