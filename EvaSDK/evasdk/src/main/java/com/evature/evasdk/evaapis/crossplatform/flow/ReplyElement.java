package com.evature.evasdk.evaapis.crossplatform.flow;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class ReplyElement extends FlowElement   implements Serializable {
    private static final String TAG = "ReplyElement";
    public String attributeType;
    public Object attributeValue;

    public enum ReplyAttribute {
        Unknown,
        CallSupport,
        ReservationID,
        Cancellation,
        Baggage,
        MultiSegment
    };

    public ReplyAttribute attributeKey;

	public ReplyElement(JSONObject jFlowElement, List<String> parseErrors, EvaLocation[] locations, JSONObject jApiReply) {
		super(jFlowElement, parseErrors, locations);
		
		try {
			String jAttributeKey = jFlowElement.getString("AttributeKey");
			attributeType = jFlowElement.getString("AttributeType");
            if (jApiReply.has(attributeType)) {
                attributeValue = jApiReply.getJSONObject(attributeType).opt(jAttributeKey);
            }
            try {
                attributeKey = ReplyAttribute.valueOf(jAttributeKey.replace(" ","").replace("-",""));
            }
            catch(IllegalArgumentException e) {
                DLog.w(TAG, "Unexpected ReplyAttribute in Flow element", e);
                attributeKey = ReplyAttribute.Unknown;
            }
		}
		catch(JSONException e) {
			parseErrors.add("Exception during parsing Reply element: "+e.getMessage());	
		}
	}
}
