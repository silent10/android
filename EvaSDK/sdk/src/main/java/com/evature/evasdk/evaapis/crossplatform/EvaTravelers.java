package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class EvaTravelers  implements Serializable{
	private final static String TAG = "EvaTravellers";
	public Integer adult = null;
	public Integer child = null;
	public Integer infant = null;
	public Integer elderly = null;
	
	public EvaTravelers(JSONObject jTravelers, List<String> parseErrors) {
		try {
			if (jTravelers.has("Adult")) {
				adult = jTravelers.getInt("Adult");
			}
			if (jTravelers.has("Child")) {
				child = jTravelers.getInt("Child");
			}
			if (jTravelers.has("Infant")) {
				infant = jTravelers.getInt("Infant");
			}
			if (jTravelers.has("Elderly")) {
				elderly = jTravelers.getInt("Elderly");
			}
		} catch (JSONException e) {
			parseErrors.add("Error during parsing Travelers: "+e.getMessage());
			DLog.e(TAG, "Travelers Parse error ", e);
		}
	}
	
	public Integer allAdults() {
		int result = 0;
		if (adult != null) {
			result = adult.intValue();
		}
		if (elderly != null) {
			result += elderly.intValue();
		}
		return result;
	}
	
	public Integer allChildren() {
		int result = 0;
		if (child != null) {
			result = child.intValue();
		}
		if (infant != null) {
			result += infant.intValue();
		}
		return result;
	}
}
