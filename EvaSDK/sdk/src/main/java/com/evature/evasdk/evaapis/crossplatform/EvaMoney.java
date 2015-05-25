package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


public class EvaMoney  implements Serializable {
	private final static String TAG = "EvaMoney";

	public String Amount; 		// the amount of money.
	public String Currency;		// the currency used, per ISO 4217 codes
	public enum RestrictionType {
		Unknown, 
		Less,
		More,
		Least,
		Most,
		Medium
	}
	public RestrictionType Restriction;	// restrictions can have the following values: ?Less?, ?More?, ?Least?, ?Most?, ?Medium?
	public Boolean PerPerson;	// boolean to indicate whether or not the price is per-person.
	public String EndOfRange;
	
	public EvaMoney(JSONObject jMoney, List<String> parseErrors) {
		try {
			if (jMoney.has("Amount")) {
				Amount = jMoney.getString("Amount");
			}
			if (jMoney.has("Currency")) {
				Currency = jMoney.getString("Currency");
			}
			if (jMoney.has("Restriction")) {
				try {
					Restriction = RestrictionType.valueOf( jMoney.getString("Restriction"));
				}
				catch(IllegalArgumentException e) {
					parseErrors.add( "Unexpected Restriction"+jMoney.optString("Restriction"));
					DLog.w(TAG, "Unexpected Restriction", e);
					Restriction = RestrictionType.Unknown;
				}
			}
			if (jMoney.has("Per Person")) {
				PerPerson = jMoney.getBoolean("Per Person");
			}
			if (jMoney.has("End Of Range")) {
				EndOfRange = jMoney.getString("End Of Range");
			}
		} catch (JSONException e) {
			parseErrors.add("Error during parsing Money: "+e.getMessage());
			DLog.e(TAG, "Money Parse error ", e);
		}
	}
}
