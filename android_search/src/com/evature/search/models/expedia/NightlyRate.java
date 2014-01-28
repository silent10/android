package com.evature.search.models.expedia;

import org.json.JSONObject;



public class NightlyRate {

	public boolean mPromo;
	public double mRate;
	public double mBaseRate;

	public NightlyRate(JSONObject jRate) {
		mPromo = EvaXpediaDatabase.getSafeBool(jRate, "@promo");
		mRate = EvaXpediaDatabase.getSafeDouble(jRate, "@rate");
		mBaseRate = EvaXpediaDatabase.getSafeDouble(jRate, "@baseRate");

	}

}
