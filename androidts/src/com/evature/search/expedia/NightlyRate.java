package com.evature.search.expedia;

import org.json.JSONObject;



public class NightlyRate {

	private boolean mPromo;
	private double mRate;
	private double mBaseRate;

	public NightlyRate(JSONObject jRate) {
		mPromo = EvaXpediaDatabase.getSafeBool(jRate, "promo");
		mRate = EvaXpediaDatabase.getSafeDouble(jRate, "rate");
		mBaseRate = EvaXpediaDatabase.getSafeDouble(jRate, "baseRate");

	}

}
