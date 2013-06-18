package com.evature.search.models.expedia;

import org.json.JSONObject;



public class Surcharge {

	private double mAmount;
	private String mType;

	public Surcharge(JSONObject jSurcharge) {
		mAmount = EvaXpediaDatabase.getSafeDouble(jSurcharge, "@amount");
		mType = EvaXpediaDatabase.getSafeString(jSurcharge, "@type");
	}

}
