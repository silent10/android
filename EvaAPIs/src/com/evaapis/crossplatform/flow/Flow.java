package com.evaapis.crossplatform.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.EvaLocation;
import com.evature.util.DLog;

public class Flow  implements Serializable {

	private static final String TAG = "Flow";
	public FlowElement[]  Elements;
	
	public Flow(JSONArray jFlow, List<String> parseErrors, EvaLocation[] locations) {
		
		ArrayList<FlowElement>  elementsToAdd = new ArrayList<FlowElement>();
		HashSet<Integer> indexesToSkip = new HashSet<Integer>();
		try {
			for (int index=0; index < jFlow.length(); index++) {
				if (indexesToSkip.contains(Integer.valueOf(index))) {
					continue;
				}
				JSONObject jElement = jFlow.getJSONObject(index);
				String flowType = jElement.getString("Type");
				if (flowType.equals( FlowElement.TypeEnum.Question.name())) {
					elementsToAdd.add( new QuestionElement(jElement, parseErrors, locations));
				}
				else if (flowType.equals( FlowElement.TypeEnum.Flight.name())) {
					FlightFlowElement flightFlowElement = new FlightFlowElement(jElement, parseErrors, locations);
					elementsToAdd.add( flightFlowElement);
					indexesToSkip.add( Integer.valueOf(flightFlowElement.ActionIndex));
				}
				else if (flowType.equals( FlowElement.TypeEnum.Reply.name())) {
					elementsToAdd.add( new ReplyElement(jElement, parseErrors, locations));
				}
				else if (flowType.equals( FlowElement.TypeEnum.Statement.name())) {
					elementsToAdd.add( new StatementElement(jElement, parseErrors, locations));
				}
				else {
					elementsToAdd.add( new FlowElement(jElement, parseErrors, locations));
				}
			}
		} catch(JSONException e) {
			DLog.e(TAG, "Bad EVA reply! ", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());	
		}
		
		Elements = elementsToAdd.toArray(new FlowElement[elementsToAdd.size()]);
	}
}
