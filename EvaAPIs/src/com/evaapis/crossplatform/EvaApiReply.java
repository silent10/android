package com.evaapis.crossplatform;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.flow.Flow;
import com.evature.util.DLog;

public class EvaApiReply implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private final String TAG = "EvaApiReply";
	public String sayIt;
	public String sessionId;
	public String transactionId;
	public String processedText;
	public String originalInputText;
	public EvaChat chat;
	public EvaDialog dialog;
	public EvaLocation[] locations;
	public EvaLocation[] alt_locations;
	public Map<String, String> ean;
	public Sabre sabre;
	public RequestAttributes requestAttributes;
	public Map<String, Boolean> geoAttributes;
	public FlightAttributes flightAttributes;
	public HotelAttributes hotelAttributes;
	public ServiceAttributes serviceAttributes;
	public CruiseAttributes cruiseAttributes;
	public EvaTravelers travelers;
	public EvaMoney money;
	public PNRAttributes pnrAttributes;
	public Flow flow;  // covers the top level understanding of what the user asks for, and what to do next
	
	public String errorMessage; // error code returned from Eva service
	public List<EvaWarning> evaWarnings = new ArrayList<EvaWarning>();
	public List<String>  parseErrors = new ArrayList<String>();  // errors identified during the parsing
	public ParsedText parsedText;
	
	public transient JSONObject JSONReply;


	public EvaApiReply(String fullReply) {
		initFromJson(fullReply);
	}
	
	private void initFromJson(String fullReply) {
		parseErrors = new ArrayList<String>();
		evaWarnings = new ArrayList<EvaWarning>();
		try {
			this.JSONReply = new JSONObject(fullReply);
			JSONObject jFullReply = JSONReply;
			if (DLog.DebugMode) {
				DLog.d(TAG, "Eva Reply:");
				// Android log buffer truncates extra long text - so split to lines into chunks
				String replyStr = jFullReply.toString(2);
				int start = 0;
				int chunkSize = 512;
				int end = chunkSize;
				while (end < replyStr.length()) {
					android.util.Log.d(TAG, replyStr.substring(start, end));
					start = end;
					end += chunkSize;
				}
				android.util.Log.d(TAG, replyStr.substring(start, replyStr.length()));

				// splitting by \n isn't good - slows down the device too much when connected by USB to LogCat
//				String[] splitLines = jFullReply.toString(2).split("\n");
//				for (String line : splitLines) {
//					// don't go through the listeners and (File:Line) suffix of DLog
//					android.util.Log.d(TAG, line);
//				}
			}
			
			boolean status = jFullReply.optBoolean("status", false);
			if (!status) {
				errorMessage = jFullReply.optString("message", "Unknown Error");
			}
			else {
				if (jFullReply.has("session_id")) {
					sessionId = jFullReply.getString("session_id");
				}
				if (jFullReply.has("transaction_key")) {
					transactionId = jFullReply.getString("transaction_key");
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
				if (jApiReply.has("Last Utterance Parsed Text")) {
					parsedText = new ParsedText(jApiReply.getJSONObject("Last Utterance Parsed Text"), parseErrors);
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
				if (jApiReply.has("Service Attributes")) {
					serviceAttributes = new ServiceAttributes(jApiReply.getJSONObject("Service Attributes"), parseErrors);
				}
				
				if (jApiReply.has("Request Attributes")) {
					JSONObject requestAttr = jApiReply.getJSONObject("Request Attributes");
					if (requestAttr.has("PNR")) {
						pnrAttributes = new PNRAttributes(requestAttr.getJSONObject("PNR"), parseErrors);
					}
					requestAttributes = new RequestAttributes(jApiReply.getJSONObject("Request Attributes"), parseErrors);
				}
				
				if (jApiReply.has("Cruise Attributes")) {
					cruiseAttributes = new CruiseAttributes(jApiReply.getJSONObject("Cruise Attributes"), parseErrors);
				}
				
				if (jApiReply.has("Flow")) {
					flow = new Flow(jApiReply.getJSONArray("Flow"), parseErrors, locations);
				}
			}
		} catch (JSONException e) {
			DLog.e(TAG, "Bad EVA reply!", e);
			parseErrors.add("Exception during parsing: "+e.getMessage());
		}
		if (parseErrors.size() > 0) {
			DLog.w(TAG, "reply is "+fullReply);
		}
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		// write only the JSON - to save space and time
		oos.writeObject(JSONReply.toString());
	}

	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		initFromJson((String)ois.readObject());
	}

}
