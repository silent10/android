package com.evature.evasdk.evaapis.crossplatform;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class EvaWarning  implements Serializable {
	
	public String type;
	public String text;
	public int position = -1;
	
	public EvaWarning(JSONArray jWarning) throws JSONException {
		type = jWarning.getString(0);
		text = jWarning.getString(1);
		if ("Parse Warning".equals(type)) {
			JSONObject data = jWarning.getJSONObject(2);
			position = data.optInt("position");
			text = data.getString("text");
		}
	}
}
