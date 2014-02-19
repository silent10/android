package com.evaapis.crossplatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.flow.Flow;
import com.evature.util.Log;

public class EvaApiReply {
	private final String TAG = "EvaApiReply";
	public String sayIt = null;
	public String sessionId = null;
	public String processedText = null;
	public String originalInputText = null;
	public EvaChat chat = null;
	public EvaDialog dialog = null;
	public EvaLocation[] locations;
	public EvaLocation[] alt_locations;
	public Map<String, String> ean = null;
	public Sabre sabre = null;
	public RequestAttributes requestAttributes = null;
	public Map<String, Boolean> geoAttributes = null;
	public FlightAttributes flightAttributes = null;
	public HotelAttributes hotelAttributes = null;
	public EvaTravelers travelers = null;
	public EvaMoney money = null;
	public PNRAttributes pnrAttributes = null;
	public Flow flow = null;
	
	public String errorMessage = null; // error code returned from Eva service
	public List<EvaWarning> evaWarnings = new ArrayList<EvaWarning>();
	public List<String>  parseErrors = new ArrayList<String>();  // errors identified during the parsing
	
	public JSONObject JSONReply;

	public EvaApiReply(String fullReply) {
		parseErrors = new ArrayList<String>();
		evaWarnings = new ArrayList<EvaWarning>();
		try {
			this.JSONReply = new JSONObject(fullReply);
			JSONObject jFullReply = JSONReply;
			Log.d(TAG, "eva_reply: " + jFullReply.toString(2));
			boolean status = jFullReply.getBoolean("status");
			if (!status) {
				errorMessage = jFullReply.optString("message", "Error");
			}
			else {
				if (jFullReply.has("session_id")) {
					sessionId = jFullReply.getString("session_id");
				}
				JSONObject jApiReply = jFullReply.getJSONObject("api_reply");
				if (jApiReply.has("ProcessedText")) {
					processedText = jApiReply.getString("ProcessedText");
				}
				if (jApiReply.has("original_input_text")) {
					originalInputText = jApiReply.getString("original_input_text");
				}
				if (jApiReply.has("Warnings")) {
					JSONArray jWarn = jApiReply.getJSONArray("Warnings");
					for (int i=0; i<jWarn.length(); i++) {
						try {
							EvaWarning  warning = new EvaWarning(jWarn.getJSONArray(i));
							evaWarnings.add(warning);
						}
						catch(JSONException e) {
							// warnings may contain some non-array that we can ignore
						}
					}
				}
				if (jApiReply.has("Chat")) {
					chat = new EvaChat(jApiReply.getJSONObject("Chat"), parseErrors);
				}
				if (jApiReply.has("Dialog")) {
					dialog  = new EvaDialog(jApiReply.getJSONObject("Dialog"), parseErrors);
				}
				if (jApiReply.has("SayIt")) {
					sayIt = jApiReply.getString("SayIt");
				}
				if (jApiReply.has("Locations")) {
					JSONArray jLocations = jApiReply.getJSONArray("Locations");
					locations = new EvaLocation[jLocations.length()];
					for (int index = 0; index < jLocations.length(); index++) {
						locations[index] = new EvaLocation(jLocations.getJSONObject(index), parseErrors);
					}
				}
				if (jApiReply.has("Alt Locations")) {
					JSONArray jLocations = jApiReply.getJSONArray("Alt Locations");
					alt_locations = new EvaLocation[jLocations.length()];
					for (int index = 0; index < jLocations.length(); index++) {
						alt_locations[index] = new EvaLocation(jLocations.getJSONObject(index), parseErrors);
					}
				}
				if (jApiReply.has("ean")) {
					JSONObject jEan = jApiReply.getJSONObject("ean");
					@SuppressWarnings("unchecked")
					Iterator<String> nameItr = jEan.keys();
					ean = new HashMap<String, String>();
					while (nameItr.hasNext()) {
						String key = nameItr.next().toString();
						String value = jEan.getString(key);
						ean.put(key, value);
					}
				}
				
				if (jApiReply.has("sabre")) {
					JSONObject jSabre = jApiReply.getJSONObject("sabre");
					sabre = new Sabre(jSabre, parseErrors);
				}
				
				if (jApiReply.has("Geo Attributes")) {
					JSONObject jGeo = jApiReply.getJSONObject("Geo Attributes");
					@SuppressWarnings("unchecked")
					Iterator<String> nameItr = jGeo.keys();
					geoAttributes = new HashMap<String, Boolean>();
					while (nameItr.hasNext()) {
						String key = nameItr.next().toString();
						Boolean value = jGeo.getBoolean(key);
						geoAttributes.put(key, value);
					}
				}
				if (jApiReply.has("Travelers")) {
					travelers = new EvaTravelers(jApiReply.getJSONObject("Travelers"), parseErrors);
				}
				if (jApiReply.has("Money")) {
					money = new EvaMoney(jApiReply.getJSONObject("Money"), parseErrors);
				}
				if (jApiReply.has("Flight Attributes")) {
					flightAttributes = new FlightAttributes(jApiReply.getJSONObject("Flight Attributes"), parseErrors);
				}
				if (jApiReply.has("Hotel Attributes")) {
					hotelAttributes = new HotelAttributes(jApiReply.getJSONObject("Hotel Attributes"), parseErrors);
				}
				
				if (jApiReply.has("Request Attributes")) {
					JSONObject requestAttr = jApiReply.getJSONObject("Request Attributes");
					if (requestAttr.has("PNR")) {
						pnrAttributes = new PNRAttributes(requestAttr.getJSONObject("PNR"), parseErrors);
					}
					requestAttributes = new RequestAttributes(jApiReply.getJSONObject("Request Attributes"), parseErrors);
				}
				if (jApiReply.has("Flow")) {
					flow = new Flow(jApiReply.getJSONArray("Flow"), parseErrors, locations);
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "Bad EVA reply!", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());
		}
		if (parseErrors.size() > 0) {
			Log.w(TAG, "reply is "+fullReply);
		}
	}

	public boolean isHotelSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return false; // It is a train search!
			}
			if (locations[1].actions == null) {
				return true;
			} else {
				return locations[1].isHotelSearch();
			}
		}
		return false;
	}

	public boolean isFlightSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return false; // It is a train search!
			}
			if (locations[1].actions == null) {
				return true;
			} else {
				return locations[1].actions.contains("Get There");
			}
		}
		return false;
	}

	public boolean isTrainSearch() {
		if (locations != null && locations.length > 1) {
			if ((locations[0].requestAttributes != null)
					&& (locations[0].requestAttributes.transportType.contains("Train"))) {
				return true; // It is a train search!
			}
		}
		return false;
	}

}