package com.evaapis.crossplatform;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.DLog;

public class FlightAttributes  implements Serializable {

	private static final String TAG = "FlightAttributes";
	public Boolean nonstop = null; // A Non stop flight - Boolean attribute.
	public Boolean redeye = null; // A Red eye flight - Boolean attribute.
	public Boolean only = null; // The request is specifically asking for just-a-flight (and no hotel, car etc.) - Boolean
							// attribute
	public Boolean oneWay = null; // Specific request for one way trip. Example: ???????united airlines one way flights to ny????????
	public Boolean twoWay = null; // Specific request for round trip. Example: ???????3 ticket roundtrip from tagbilaran to manila/
							// 1/26/2011-1/30/2011????????
	public String[] airlines;
	public String food;
	public enum SeatType { Unknown, Window, Aisle };
	public SeatType seatType;
	public enum SeatClass {
		Unknown,
		First, Business, Premium, Economy
	};
	public SeatClass[] seatClass;

	public FlightAttributes(JSONObject jFlightAttributes, List<String> parseErrors) {
		try {
			if (jFlightAttributes.has("Nonstop"))
				nonstop = jFlightAttributes.getBoolean("Nonstop");
			if (jFlightAttributes.has("Redeye"))
				redeye = jFlightAttributes.getBoolean("Redeye");
			if (jFlightAttributes.has("Only"))
				only = jFlightAttributes.getBoolean("Only");
			if (jFlightAttributes.has("Two-Way"))
				twoWay = jFlightAttributes.getBoolean("Two-Way");
			
			if (jFlightAttributes.has("Airline")) {
				JSONArray jAirlines = jFlightAttributes.getJSONArray("Airline");
				airlines = new String[jAirlines.length()];
				for (int i=0; i< jAirlines.length(); i++) {
					JSONObject jAirline = jAirlines.getJSONObject(i);
					airlines[i] = jAirline.getString("IATA");
				}
			}
			if (jFlightAttributes.has("Food")) {
				food = jFlightAttributes.getString("Food");
			}
			
			if (jFlightAttributes.has("Seat")) {
				try {
					seatType = SeatType.valueOf(jFlightAttributes.getString("Seat"));
				}
				catch(IllegalArgumentException e) {
					parseErrors.add( "Unexpected SeatType "+jFlightAttributes.optString("Seat"));
					DLog.w(TAG, "Unexpected SeatType", e);
					seatType = SeatType.Unknown;
				}
			}
			if (jFlightAttributes.has("Seat Class")) {
				JSONArray jSeatClass = jFlightAttributes.getJSONArray("Seat Class");
				seatClass = new SeatClass[jSeatClass.length()];
				for (int i=0; i<jSeatClass.length(); i++) {
					try {
						seatClass[i] = SeatClass.valueOf(jSeatClass.getString(i));
					}
					catch(IllegalArgumentException e) {
						parseErrors.add( "Unexpected SeatClass"+jSeatClass.optString(i));
						DLog.w(TAG, "Unexpected SeatClass", e);
						seatClass[i] = SeatClass.Unknown;
					}
				}
			}
			
		} catch (JSONException e) {
			DLog.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing flight attributes: "+e.getMessage());
		}
	}
}
