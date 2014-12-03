package com.evaapis.crossplatform.flow;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.EvaLocation;
import com.evaapis.crossplatform.flow.FlowElement.TypeEnum;
import com.evature.util.Log;

public class QuestionElement extends FlowElement {

	private final static String TAG = "QuestionElement";
	public enum QuestionType {
		Unknown,
		Open,
		Multiple_Choice,
		YesNo
	};
	public QuestionType questionType;
	public enum QuestionCategory {
		Unknown, 
		Location_Ambiguity, 
		Missing_Date,
		Missing_Duration,
		Missing_Location,
		Informative
	};
	public QuestionCategory questionCategory; 
	
	public String questionSubCategory;
	public String[] choices;
	
	
	public QuestionElement(JSONObject jFlowElement, List<String> parseErrors,
			EvaLocation[] locations) {
		
		super(jFlowElement, parseErrors, locations);
		
		try {
			if (jFlowElement.has("QuestionType")) {
				try {
					questionType = QuestionType.valueOf(jFlowElement.getString("QuestionType").replace(' ','_'));
				}
				catch(IllegalArgumentException e) {
					Log.w(TAG, "Unexpected QuestionType in Flow element", e);
					questionType = QuestionType.Unknown;
				}
			}
			
			if (jFlowElement.has("QuestionCategory")) {
				try {
					questionCategory = QuestionCategory.valueOf(jFlowElement.getString("QuestionCategory").replace(' ', '_'));
				}
				catch(IllegalArgumentException e) {
					Log.w(TAG, "Unexpected Question Category in Flow element", e);
					questionCategory = QuestionCategory.Unknown;
				}
			}
			
			if (jFlowElement.has("QuestionSubCategory")) {
				questionSubCategory = jFlowElement.getString("QuestionSubCategory");
			}
			
			if (jFlowElement.has("QuestionChoices")) {
				JSONArray jChoices = jFlowElement.getJSONArray("QuestionChoices");
				choices = new String[jChoices.length()];
				for (int index=0; index < jChoices.length(); index++) {
					choices[index] = jChoices.getString(index);
				}
			}
		}
		catch(JSONException e) {
			Log.e(TAG, "Bad EVA reply!", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());	
		}
		catch(Exception e) {
			Log.e(TAG, "Error parsing EVA reply", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());
			try {
				Log.i(TAG, jFlowElement.toString(4));
			} catch (JSONException e1) {
				Log.e(TAG, "JSON exception parsing question element", e);
			}
		}


	}

}
