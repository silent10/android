package com.softskills.evasearch.database;

import org.json.JSONObject;


public class Surcharge {

	private double mAmount;
	private String mType;

	public Surcharge(JSONObject jSurcharge) {
		mAmount = EvaDatabase.getSafeDouble(jSurcharge, "@amount");
		mType = EvaDatabase.getSafeString(jSurcharge, "@type");
	}

}
