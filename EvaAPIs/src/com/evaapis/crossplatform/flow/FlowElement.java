package com.evaapis.crossplatform.flow;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.EvaLocation;
import com.evature.util.DLog;

public class FlowElement implements Serializable {

	private final static String TAG = "FlowElement"; 
	
	public enum TypeEnum {
		Flight,
		Hotel,
		Car,
		Cruise, 
		Train,
		Explore, 

		Question,
		Answer, 
		Statement,
		Service,
		Reply,
		
		Other
	}
	public TypeEnum Type;
	public EvaLocation[] RelatedLocations;
	String SayIt; 
	
	
	public FlowElement(JSONObject jFlowElement, List<String> parseErrors, EvaLocation[] locations) {
		
		try {
			if (jFlowElement.has("RelatedLocations")) {
				JSONArray jLocations = jFlowElement.getJSONArray("RelatedLocations");
				RelatedLocations = new EvaLocation[jLocations.length()];
				for (int index = 0; index < jLocations.length(); index++) {
					RelatedLocations[index] = locations[jLocations.getInt(index)];
				}
			}
		}
		catch(JSONException e) {
			DLog.e(TAG, "Bad EVA reply!  exception processing RelatedLocations in flow Element", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());	
		}
		
		try {
			Type = TypeEnum.valueOf(jFlowElement.getString("Type"));
		}
		catch(IllegalArgumentException e) {
			DLog.w(TAG, "Unexpected Flow Type in Flow element", e);
			Type = TypeEnum.Other;
		}
		catch(JSONException e) {
			DLog.e(TAG, "Bad EVA reply! no Type in Flow element", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());	
		}
		
		try {
			SayIt = jFlowElement.getString("SayIt");
		}
		catch(JSONException e) {
			DLog.e(TAG, "Bad EVA reply! no SayIt in Flow element", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());	
		}
	}
	
	public String getSayIt() {
		return SayIt;
	}
}
