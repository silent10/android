package com.evature.evasdk.evaapis.crossplatform.flow;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class ReplyElement extends FlowElement {
	public String AttributeKey;
	public String AttributeType;

	public ReplyElement(JSONObject jFlowElement, List<String> parseErrors, EvaLocation[] locations) {
		super(jFlowElement, parseErrors, locations);
		
		try {
			AttributeKey = jFlowElement.getString("AttributeKey");
			AttributeType = jFlowElement.getString("AttributeType");
		}
		catch(JSONException e) {
			parseErrors.add("Exception during parsing Reply element: "+e.getMessage());	
		}
	}
}
