package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class PNRAttributes  implements Serializable {
	public boolean requested = false;
	
	public PNRAttributes(JSONObject pnrAttributes, List<String> parseErrors) {
		try {
			if (pnrAttributes.has("Requested")) {
				requested = pnrAttributes.getBoolean("Requested");
			}
		} catch (JSONException e) {
			DLog.e("PNR Attributes", "Error parsing JSON", e);
			parseErrors.add("Error during parsing PNR: "+e.getMessage());
		}
	}
}
