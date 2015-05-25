package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Sabre  implements Serializable {
	private static final String TAG = "Sabre";
	public String[] cryptic;
	public String[] warnings;

	public Sabre(JSONObject jSabre, List<String> parseErrors) {
		try {
			JSONArray jCryptic = jSabre.getJSONArray("cryptic");
			cryptic = new String[jCryptic.length()];
			for (int index = 0; index < jCryptic.length(); index++) {
				cryptic[index] = jCryptic.getString(index);
			}
			JSONArray jWarnings = jSabre.getJSONArray("warnings");
			warnings = new String[jWarnings.length()];
			for (int index = 0; index < jWarnings.length(); index++) {
				warnings[index] = jWarnings.getString(index);
			}
		} catch (JSONException e) {
			DLog.e(TAG, "Problem parsing JSON", e);
			parseErrors.add("Error parsing Sabre: "+e.getMessage());
		}
	}
}
