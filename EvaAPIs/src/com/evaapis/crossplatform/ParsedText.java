package com.evaapis.crossplatform;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class ParsedText {

	static public class TimesMarkup {
		public String text;
		public String type;
		public int position;
		// value, related location
	}
	
	public ArrayList<TimesMarkup> times;
	
	public ParsedText(JSONObject jsonObject, List<String> parseErrors) {
		if (jsonObject.has("Times")) {
			times = new ArrayList<ParsedText.TimesMarkup>();
			JSONArray jTimes;
			try {
				jTimes = jsonObject.getJSONArray("Times");
				for (int index = 0; index < jTimes.length(); index++) {
					JSONObject jTime = jTimes.getJSONObject(index);
					TimesMarkup time = new TimesMarkup();
					time.text = jTime.optString("Text");
					time.position = jTime.optInt("Position", -1);
					time.type = jTime.optString("Type");
					times.add(time);
				}
			} catch (JSONException e) {
				Log.e("ParsedText", "Error parsing JSON",e);
				parseErrors.add("Failed to parse Times in ParsedText");
			}

		}
	}

}
