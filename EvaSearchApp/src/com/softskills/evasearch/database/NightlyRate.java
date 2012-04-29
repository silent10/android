package com.softskills.evasearch.database;

import org.json.JSONObject;


public class NightlyRate {

	private boolean mPromo;
	private double mRate;
	private double mBaseRate;

	public NightlyRate(JSONObject jRate) {
		mPromo = EvaDatabase.getSafeBool(jRate, "promo");
		mRate = EvaDatabase.getSafeDouble(jRate, "rate");
		mBaseRate = EvaDatabase.getSafeDouble(jRate, "baseRate");

	}

}
