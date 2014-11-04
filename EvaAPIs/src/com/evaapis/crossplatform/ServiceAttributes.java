package com.evaapis.crossplatform;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class ServiceAttributes  implements Serializable {
	private static final String TAG = "ServiceAttributes";

	public static final String CALL_SUPPORT = "Call Support";
	
	public boolean callSupportRequested;

	
	public ServiceAttributes(JSONObject jService, List<String> parseErrors) {
		try {
			if (jService.has(CALL_SUPPORT)) {
				callSupportRequested = jService.getJSONObject(CALL_SUPPORT).getBoolean("Requested");
			}
		}
		catch (JSONException e) {
			Log.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing service attributes: "+e.getMessage());
		}
	}

}
