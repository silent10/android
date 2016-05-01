package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;
import com.evature.evasdk.util.StringUtils;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class EvaTime  implements Serializable {

    private static final String TAG = "EvaTime";
    public String date; // Represent a specific date and time if given.
	public String time; // Example: "fly to ny 3/4/2010 at 10am" results: "date": "2010-04-03", "time": "10:00:00".
	public String delta; // May represent:
	// A range starting from Date/Time. Example: "next week" results: "date": "2010-10-25", "delta": "days=+6"
	// A duration without an anchor date. Example: "hotel for a week" results: "delta": "days=+7"
	//
	public String maxDelta;
	public String minDelta;

    public enum TimeRestriction {
        Unknown,
        NoEarlier, NoLater, NoMore, NoLess, Latest, Earliest
    }
	public TimeRestriction restriction;
	// A restriction on the date/time requirement. Values can be: "no_earlier", "no_later", "no_more", "no_less",
	// "latest", "earliest"
	//
	// Example: "depart NY no later than 10am" results: "restriction": "no_later", "time": "10:00:00"

	// A boolean flag representing that a particular time has been calculated from other times, and not directly derived
	// from the input text. In most cases if an arrival time to a location is specified, the departure time from the
	// previous location is calculated.
	public Boolean calculated;

	public EvaTime(JSONObject evaTime, List<String> parseErrors) {
		try {
			if (evaTime.has("Date")) {
				date = evaTime.getString("Date");
			}
			if (evaTime.has("Time")) {
				time = evaTime.getString("Time");
			}
			if (evaTime.has("Restriction")) {
                try {
                    restriction = TimeRestriction.valueOf(StringUtils.toCamelCase(evaTime.getString("Restriction")));
                }
                catch(IllegalArgumentException e) {
                    parseErrors.add("Unexpected Restriction " + evaTime.optString("Restriction"));
                    DLog.w(TAG, "Unexpected Restriction", e);
                    restriction = TimeRestriction.Unknown;
                }
			}
			if (evaTime.has("Delta")) {
				delta = evaTime.getString("Delta");
			}
			maxDelta = evaTime.optString("MaxDelta", null);
			minDelta = evaTime.optString("MinDelta", null);
			if (evaTime.has("Calculated")) {
				calculated = evaTime.getBoolean("Calculated");
			}

		} catch (JSONException e) {
			DLog.e("EvaTime", "Error parsing JSON", e);
			parseErrors.add("Error during parsing time: "+e.getMessage());
		}
	}
	
	public Integer daysDelta() {
		return daysDelta(delta);
	}
	
	public static Integer daysDelta(String delta) {
		Integer result = null;
		if (delta != null) {
			if (delta.startsWith("days=+")) {
				result = Integer.parseInt(delta.substring(6));
			}
		}
		return result;
	}
}
