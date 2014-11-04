package com.evaapis.crossplatform;

import java.io.Serializable;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class EvaMoney  implements Serializable {
	private final static String TAG = "EvaMoney";

	public String Amount; 		// the amount of money.
	public String Currency;		// the currency used, per ISO 4217 codes
	public enum RestrictionType {
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
				Restriction = RestrictionType.valueOf( jMoney.getString("Restriction"));
			}
			if (jMoney.has("Per Person")) {
				PerPerson = jMoney.getBoolean("Per Person");
			}
			if (jMoney.has("End Of Range")) {
				EndOfRange = jMoney.getString("End Of Range");
			}
		} catch (JSONException e) {
			parseErrors.add("Error during parsing Money: "+e.getMessage());
			Log.e(TAG, "Money Parse error ", e);
		}
	}
}
