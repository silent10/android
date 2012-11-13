package com.evaapis;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FlightAttributes {

	private static final String TAG = "FlightAttributes";
	Boolean nonstop = null; // A Non stop flight - Boolean attribute.
	Boolean redeye = null; // A Red eye flight - Boolean attribute.
	Boolean only = null; // The request is specifically asking for just-a-flight (and no hotel, car etc.) - Boolean
							// attribute
	Boolean oneWay = null; // Specific request for one way trip. Example: “united airlines one way flights to ny”
	Boolean twoWay = null; // Specific request for round trip. Example: “3 ticket roundtrip from tagbilaran to manila/
							// 1/26/2011-1/30/2011”

	public FlightAttributes(JSONObject jFlightAttributes) {
		try {
			if (jFlightAttributes.has("Nonstop"))
				nonstop = jFlightAttributes.getBoolean("Nonstop");
			if (jFlightAttributes.has("Redeye"))
				redeye = jFlightAttributes.getBoolean("Redeye");
			if (jFlightAttributes.has("Only"))
				only = jFlightAttributes.getBoolean("Only");
			if (jFlightAttributes.has("Two-Way"))
				twoWay = jFlightAttributes.getBoolean("Two-Way");
		} catch (JSONException e) {
			Log.e(TAG, "Parsing JSON");
		}
	}
}
