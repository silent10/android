package com.evaapis.crossplatform;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class EvaLocation {
	private final String TAG = "EvaLocation";
	public int index; // A number representing the location index in the trip. Index numbers usually progress with the
						   // duration of the trip (so a location with index 11 is visited before a location with index
						   // 21). An index number is unique for a locations in Locations (unless the same location visited
						   // multiple times, for example home location at start and end of trip will have the same index)
						   // but ???????Alt Locations???????? may have multiple locations with the same index, indicating alternatives
						   // for the same part of a trip. Index numbers are not serial, so indexes can be (0,1,11,21,22,
						   // etc.). Index number ???????0???????? is unique and always represents the home location.

	public int next = -1; // The index number of the location in a trip, if known.
	public String allAirportCode; // Will be present in cities that have an ???????all airports???????? IATA code
									// e.g. San Francisco, New York, etc.
	public List<String> airports = null; // If a location is not an airport, this key provides 5 recommended airports
											// for this location. Airports are named by their IATA code.
	public String Geoid; // A global identifier for the location. IATA code for airports and Geoname ID for other
							// locations. Note: if Geoname ID is not defined for a location, a string representing the
							// name of the location will be given in as value instead. The format of this name is
							// currently not set and MAY CHANGE. If you plan to use this field, please contact us.
	public HashSet<String> actions; // Provides a list of actions requested for this location. Actions can include the
								// following values: ???????Get There???????? (request any way to be transported there, mostly
								// flights but can be train, bus etc.), ???????Get Accommodation????????, ???????Get Car????????.
	public RequestAttributes requestAttributes = null; // There are many general request attributes that apply to the
														// entire request and not just some portion of it. Examples:
														// ???????last minute deals???????? and ???????Low deposits????????.

	public double Latitude;
	public double Longitude;
	public String Type;
	public String Name;
	public EvaTime Departure = null; // Complex Eva Time object
	public EvaTime Arrival = null; // Complex Eva Time object
	public EvaTime Stay = null;
	public HashSet<String> purpose;
	public String derivedFrom = "";
	
	public FlightAttributes flightAttributes = null;

	public EvaLocation(JSONObject location, List<String> parseErrors) {
		try {
			index = location.getInt("Index");
			if (location.has("Next"))
				next = location.getInt("Next");
			if (location.has("All Airports Code"))
				allAirportCode = location.getString("All Airports Code");
			if (location.has("Geoid")) {
				try {
					Geoid = location.getString("Geoid");
				} catch (JSONException e) {
					Geoid = String.valueOf(location.getInt("Geoid"));
				}
			}
			if (location.has("Actions")) {
				JSONArray jActions = location.getJSONArray("Actions");
				actions = new HashSet<String>(jActions.length());
				for (int index = 0; index < jActions.length(); index++) {
					actions.add(new String(jActions.getString(index)));
				}
			}
			if (location.has("Derived From")) {
				derivedFrom = location.getString("Derived From");
			}
			if (location.has("Request Attributes")) {
				requestAttributes = new RequestAttributes(location.getJSONObject("Request Attributes"), parseErrors);
			}
			if (location.has("Departure")) {
				Departure = new EvaTime(location.getJSONObject("Departure"), parseErrors);
			}
			if (location.has("Arrival")) {
				Arrival = new EvaTime(location.getJSONObject("Arrival"), parseErrors);
			}
			if (location.has("Stay")) {
				Stay = new EvaTime(location.getJSONObject("Stay"), parseErrors);
			}
			if (location.has("Name")) {
				Name = location.getString("Name");
				// remove (GID=123454) at the end
				int ind = Name.indexOf(" (GID=");
				if (ind != -1) {
					Name = Name.substring(0, ind);
				}
			}
			if (location.has("Type")) {
				Type = location.getString("Type");
			}
			if (location.has("Longitude")) {
				Longitude = location.getDouble("Longitude");
			}
			if (location.has("Latitude")) {
				Latitude = location.getDouble("Latitude"); 
			}
			
			if (location.has("Airports")) {
				airports = new ArrayList<String>();
				String[] temp = location.getString("Airports").split(",");
				for (int i = 0; i < temp.length; i++) {
					airports.add(temp[i]);
				}
			}
			
			if (location.has("Flight Attributes")) {
				flightAttributes = new FlightAttributes(location.getJSONObject("Flight Attributes"), parseErrors);
			}
			if (location.has("Purpose")) {
				JSONArray jPurpose = location.getJSONArray("Purpose");
				purpose = new HashSet<String>(jPurpose.length());
				for (int index = 0; index < jPurpose.length(); index++) {
					purpose.add(new String(jPurpose.getString(index)));
				}
			}

		} catch (JSONException e) {
			Log.e(TAG, "Error parsing JSON",e);
			parseErrors.add("Error during parsing location attributes: "+e.getMessage());
		}

	}
	
	public String airportCode() {
		if (allAirportCode != null)
			return allAirportCode;
		if (airports != null && airports.size() > 0) {
			return airports.get(0);
		}
		return null;
	}

	public boolean isTransit() {
		if (purpose == null) {
			return false;
		}
		return purpose.contains("Transit");
	}

	public boolean isHotelSearch() {
		if (actions == null) {
			return false;
		}
		return actions.contains("Get Accommodation");
	}

	public boolean isDestination() {
		if (actions == null) {
			return false;
		}
		return actions.contains("Get There");	
	}
}
