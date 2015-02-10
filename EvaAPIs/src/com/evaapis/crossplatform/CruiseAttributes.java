package com.evaapis.crossplatform;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.DLog;

public class CruiseAttributes {
	private static final String TAG = "CruiseAttributes";
	
	public static class Cruiseline {
		public String name;
		public String key;
	}
	
	public static class Cruiseship {
		public String name;
		public String key;
	}

	
	public Cruiseline cruiselines[];
	
	public Cruiseship cruiseships[];

	public CruiseAttributes(JSONObject cruiseAttributes, List<String> parseErrors) {

		try {
			if (cruiseAttributes.has("Cruiseline")) {
				JSONArray jCruiseLines = cruiseAttributes.getJSONArray("Cruiseline");
				cruiselines = new Cruiseline[jCruiseLines.length()];
				for (int i = 0; i < jCruiseLines.length(); i++) {
					JSONObject jCruiseline = jCruiseLines.getJSONObject(i);
					Cruiseline cruiseline = new Cruiseline();
					cruiseline.name = jCruiseline.getString("Name");
					if (jCruiseline.has("Keys")) {
						JSONObject jKeys = jCruiseline.getJSONObject("Keys");
						Iterator<String> keys = jKeys.keys();
						if (keys.hasNext()) {
							String key = keys.next();
							cruiseline.key = jKeys.getString(key);
						}
					}
					cruiselines[i] = cruiseline;
				}
			}
		
			if (cruiseAttributes.has("Cruiseship")) {
				JSONArray jCruiseships = cruiseAttributes.getJSONArray("Cruiseship");
				cruiseships = new Cruiseship[jCruiseships.length()];
				for (int i = 0; i < jCruiseships.length(); i++) {
					JSONObject jCruiseship = jCruiseships.getJSONObject(i);
					Cruiseship cruiseship = new Cruiseship();
					cruiseship.name = jCruiseship.getString("Name");
					if (jCruiseship.has("Keys")) {
						JSONObject jKeys = jCruiseship.getJSONObject("Keys");
						Iterator<String> keys = jKeys.keys();
						if (keys.hasNext()) {
							String key = keys.next();
							cruiseship.key = jKeys.getString(key);
						}
					}
					cruiseships[i] = cruiseship;
				}
			}
		} catch (JSONException e) {
			DLog.e(TAG, "Problem parsing JSON", e);
			parseErrors
					.add("Error parsing Cruise Attr: " + e.getMessage());
		}
	}

}
