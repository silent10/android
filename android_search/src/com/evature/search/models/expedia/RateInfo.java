package com.evature.search.models.expedia;

import org.json.JSONException;
import org.json.JSONObject;



public class RateInfo {

	private boolean mRateChange;
	private boolean mPromo;
	private boolean mPriceBreakdown;
	public ChargeableRateInfo mChargableRateInfo;

	public RateInfo(JSONObject jRateInfo) {
		mRateChange = EvaXpediaDatabase.getSafeBool(jRateInfo,"rateChange");
		mPromo = EvaXpediaDatabase.getSafeBool(jRateInfo,"promo");
		mPriceBreakdown = EvaXpediaDatabase.getSafeBool(jRateInfo,"priceBreakdown");
		
		try {
			JSONObject jChargeableRateInfo = jRateInfo.getJSONObject("ChargeableRateInfo");
			
			mChargableRateInfo = new ChargeableRateInfo(jChargeableRateInfo);
			
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

}
