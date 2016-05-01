package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class EvaMoney  implements Serializable {
	private final static String TAG = "EvaMoney";

	public String amount; 		// the amount of money.
	public String currency;		// the currency used, per ISO 4217 codes
	public enum RestrictionType {
		Unknown, 
		Less,
		More,
		Least,
		Most,
		Medium
	}
	public RestrictionType restriction;	// restrictions can have the following values: ?Less?, ?More?, ?Least?, ?Most?, ?Medium?
	public Boolean perPerson;	// boolean to indicate whether or not the price is per-person.
	public String endOfRange;
	
	public EvaMoney(JSONObject jMoney, List<String> parseErrors) {
		try {
			if (jMoney.has("Amount")) {
				amount = jMoney.getString("Amount");
			}
			if (jMoney.has("Currency")) {
				currency = jMoney.getString("Currency");
			}
			if (jMoney.has("Restriction")) {
				try {
					restriction = RestrictionType.valueOf( jMoney.getString("Restriction"));
				}
				catch(IllegalArgumentException e) {
					parseErrors.add( "Unexpected Restriction"+jMoney.optString("Restriction"));
					DLog.w(TAG, "Unexpected Restriction", e);
					restriction = RestrictionType.Unknown;
				}
			}
			if (jMoney.has("Per Person")) {
				perPerson = jMoney.getBoolean("Per Person");
			}
			if (jMoney.has("End Of Range")) {
				endOfRange = jMoney.getString("End Of Range");
			}
		} catch (JSONException e) {
			parseErrors.add("Error during parsing Money: "+e.getMessage());
			DLog.e(TAG, "Money Parse error ", e);
		}
	}
}
