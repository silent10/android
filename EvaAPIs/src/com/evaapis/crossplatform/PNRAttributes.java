package com.evaapis.crossplatform;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class PNRAttributes {
	public boolean requested = false;
	
	public PNRAttributes(JSONObject pnrAttributes, List<String> parseErrors) {
		try {
			if (pnrAttributes.has("Requested")) {
				requested = pnrAttributes.getBoolean("Requested");
			}
		} catch (JSONException e) {
			Log.e("PNR Attributes", "Error parsing JSON",e);
			parseErrors.add("Error during parsing PNR: "+e.getMessage());
		}
	}
}
