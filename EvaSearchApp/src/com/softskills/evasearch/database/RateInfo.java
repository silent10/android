package com.softskills.evasearch.database;

import org.json.JSONException;
import org.json.JSONObject;


public class RateInfo {

	private boolean mRateChange;
	private boolean mPromo;
	private boolean mPriceBreakdown;
	public ChargeableRateInfo mChargableRateInfo;

	public RateInfo(JSONObject jRateInfo) {
		mRateChange = EvaDatabase.getSafeBool(jRateInfo,"rateChange");
		mPromo = EvaDatabase.getSafeBool(jRateInfo,"promo");
		mPriceBreakdown = EvaDatabase.getSafeBool(jRateInfo,"priceBreakdown");
		
		try {
			JSONObject jChargeableRateInfo = jRateInfo.getJSONObject("ChargeableRateInfo");
			
			mChargableRateInfo = new ChargeableRateInfo(jChargeableRateInfo);
			
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

}