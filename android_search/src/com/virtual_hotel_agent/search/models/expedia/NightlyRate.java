package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONObject;



public class NightlyRate {

	public boolean mPromo;
	public double mRate;
	public double mBaseRate;

	public NightlyRate(JSONObject jRate) {
		mPromo = XpediaDatabase.getSafeBool(jRate, "@promo");
		mRate = XpediaDatabase.getSafeDouble(jRate, "@rate");
		mBaseRate = XpediaDatabase.getSafeDouble(jRate, "@baseRate");

	}

}
