package com.evature.evasdk.evaapis.crossplatform.flow;

import com.evature.evasdk.evaapis.crossplatform.EvaLocation;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by iftah on 12/04/2016.
 */
public class NavigateElement extends FlowElement {

    public final String pagePath;

    public NavigateElement(JSONObject jFlowElement, List<String> parseErrors, EvaLocation[] locations) {
        super(jFlowElement, parseErrors, locations);

        this.pagePath = jFlowElement.optString("URL");
        //this.filter = jFlowElement.optJSONObject("Filter");

    }
}
