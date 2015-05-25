package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RequestAttributes  implements Serializable {
	private static final String TAG = "RequestAttributes";

	public List<String> transportType = new ArrayList<String>();

	public enum SortEnum {
		unknown,
		reviews, location, price, price_per_person, distance, rating, guest_rating, 
		stars, time, total_time, duration, arrival_time, departure_time, outbound_arrival_time, 
		outbound_departure_time, inbound_arrival_time, inbound_departure_time, airline, operator, 
		cruiseline, cruiseship, name, popularity, recommendations
	}

	public enum SortOrderEnum {
		unknown,
		ascending, descending, reverse
	}

	public SortEnum sortBy = null;
	public SortOrderEnum sortOrder = null;

	public RequestAttributes(JSONObject requestAttributes,
			List<String> parseErrors) {
		if (requestAttributes.has("Transport Type")) {
			JSONArray jTransportType;
			try {
				jTransportType = requestAttributes
						.getJSONArray("Transport Type");
				for (int i = 0; i < jTransportType.length(); i++) {
					transportType.add(jTransportType.getString(i));
				}
			} catch (JSONException e) {
				DLog.e(TAG, "Problem parsing JSON", e);
				parseErrors
						.add("Error parsing Request Attr: " + e.getMessage());
			}
		}
		if (requestAttributes.has("Sort")) {
			try {
				JSONObject jSort = requestAttributes.getJSONObject("Sort");
				if (jSort.has("By")) {
					try {
						sortBy = SortEnum.valueOf(jSort.getString("By").replace(
								' ', '_'));
					}
					catch(IllegalArgumentException e) {
						DLog.w(TAG, "Unexpected SortBy", e);
						sortBy = SortEnum.unknown;
					}
				}
				if (jSort.has("Order")) {
					try {
						sortOrder = SortOrderEnum.valueOf(jSort.getString("Order").replace(
								' ', '_'));
					}
					catch(IllegalArgumentException e) {
						DLog.w(TAG, "Unexpected SortOrder", e);
						sortOrder = SortOrderEnum.unknown;
					}
				}
			} catch (JSONException e) {
				DLog.e(TAG, "Problem parsing JSON", e);
				parseErrors
						.add("Error parsing Request Attr: " + e.getMessage());
			}
		}
		
	}
}
