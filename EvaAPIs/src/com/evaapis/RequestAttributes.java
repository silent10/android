package com.evaapis;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class RequestAttributes {
	private static final String TAG = "RequestAttributes";

	public List<String> transportType = new ArrayList<String>();

	public RequestAttributes(JSONObject requestAttributes) {
		if (requestAttributes.has("Transport Type")) {
			JSONArray jTransportType;
			try {
				jTransportType = requestAttributes.getJSONArray("Transport Type");
				for (int i = 0; i < jTransportType.length(); i++) {
					transportType.add(jTransportType.getString(i));
				}
			} catch (JSONException e) {
				Log.e(TAG, "Problem parsing JSON");
			}
		}
	}
}
