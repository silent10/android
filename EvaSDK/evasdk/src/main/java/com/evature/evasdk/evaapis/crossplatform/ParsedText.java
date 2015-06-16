package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ParsedText implements Serializable {

	static public class TimesMarkup {
		public String text;
		public String type; // eg. departure, arrival
		public int position;
		// value, related location
	}
	
	static public class LocationMarkup {
		public String text;
		public int position;
	}
	
	public ArrayList<TimesMarkup> times;
	public ArrayList<LocationMarkup> locations;
	
	public ParsedText(JSONObject jsonObject, List<String> parseErrors) {
		if (jsonObject.has("Times")) {
			try {
				times = new ArrayList<TimesMarkup>();
				JSONArray jTimes = jsonObject.getJSONArray("Times");
				for (int index = 0; index < jTimes.length(); index++) {
					JSONObject jTime = jTimes.getJSONObject(index);
					TimesMarkup time = new TimesMarkup();
					time.text = jTime.optString("Text");
					time.position = jTime.optInt("Position", -1);
					time.type = jTime.optString("Type");
					times.add(time);
				}
			} catch (JSONException e) {
				DLog.e("ParsedText", "Error parsing JSON", e);
				parseErrors.add("Failed to parse Times in ParsedText");
			}
		}
		
		if (jsonObject.has("Locations")) {
			try {
				locations = new ArrayList<LocationMarkup>();
				JSONArray jLocations = jsonObject.getJSONArray("Locations");
				for (int index = 0; index < jLocations.length(); index++) {
					JSONObject jLocation = jLocations.getJSONObject(index);
					LocationMarkup location = new LocationMarkup();
					location.text = jLocation.optString("Text");
					location.position = jLocation.optInt("Position", -1);
					locations.add(location);
				}
			} catch (JSONException e) {
				DLog.e("ParsedText", "Error parsing JSON",e);
				parseErrors.add("Failed to parse Locations in ParsedText");
			}

		}
	}

}
