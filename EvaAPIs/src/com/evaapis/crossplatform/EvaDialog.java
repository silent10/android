package com.evaapis.crossplatform;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.DLog;

public class EvaDialog  implements Serializable {

	private static final String TAG = "EvaDialog";

	public static class DialogElement {
		public String Content;
		public String Type;
		public String RelatedLocation;
		public String SubType;
		public String[] Choices;

		public DialogElement(JSONObject jElement, List<String> parseErrors) {
			try {
				if (jElement.has("Content")) {
					Content = jElement.getString("Content");
				}
				if (jElement.has("Type")) {
					Type = jElement.getString("Type");
				}
				if (jElement.has("RelatedLocation")) {
					RelatedLocation = jElement.getString("RelatedLocation");
				}
				if (jElement.has("SubType")) {
					SubType = jElement.getString("SubType");
				}
				if (jElement.has("Choices")) {
					JSONArray jChoices = jElement.getJSONArray("Choices");
					Choices = new String[jChoices.length()];
					for (int index=0; index < jChoices.length(); index++) {
						Choices[index] = jChoices.getString(index);
					}
				}
			}
			catch(JSONException e) {
				DLog.e(TAG, "Parse JSON", e);
				parseErrors.add("Error during parsing eva chat: "+e.getMessage());
			}
		}
		
	}
	
	public DialogElement[]  elements;
	public String sayIt;
	

	public EvaDialog(JSONObject jDialog, List<String> parseErrors) {
		try {
			if (jDialog.has("SayIt")) {
				sayIt = jDialog.getString("SayIt");
			}
			if (jDialog.has("Elements")) {
				JSONArray jElements = jDialog.getJSONArray("Elements");
				elements = new DialogElement[jElements.length()];
				for (int index = 0; index < jElements.length(); index++) {
					elements[index] = new DialogElement(jElements.getJSONObject(index), parseErrors);
				}
			}
			
		} catch (JSONException e) {
			DLog.e(TAG, "Parse JSON", e);
			parseErrors.add("Error during parsing eva chat: "+e.getMessage());
		}
	}
}
