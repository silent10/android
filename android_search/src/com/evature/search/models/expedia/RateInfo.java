package com.evature.search.models.expedia;

import org.json.JSONException;
import org.json.JSONObject;



public class RateInfo {

	private boolean mRateChange;
	private boolean mPromo;
	private boolean mPriceBreakdown;
	
//	private int mPromoId;
	public String mPromoDescription;
	public boolean mNonRefundable;
	public String mCancelllationPolicy;
	public boolean mGuaranteeRequired;
	public boolean mDepositRequired;

	public String mPromoDetailText;
//	promoType
//	taxRate
//	rateChange
//	rateType
//	currentAllotment
//	cancelPolicyInfoList
	
	public ChargeableRateInfo mChargableRateInfo;

	public RateInfo(JSONObject jRateInfo) {
		mRateChange = EvaXpediaDatabase.getSafeBool(jRateInfo,"@rateChange");
		mPromo = EvaXpediaDatabase.getSafeBool(jRateInfo,"@promo");
		mPriceBreakdown = EvaXpediaDatabase.getSafeBool(jRateInfo,"@priceBreakdown");
		
//		mPromoId = EvaXpediaDatabase.getSafeInt(jRateInfo, "promoId");
		mPromoDescription = EvaXpediaDatabase.getSafeString(jRateInfo, "promoDescription");
		mPromoDetailText = EvaXpediaDatabase.getSafeString(jRateInfo, "promoDetailText");
		mNonRefundable = EvaXpediaDatabase.getSafeBool(jRateInfo, "nonRefundable");
		mCancelllationPolicy = EvaXpediaDatabase.getSafeString(jRateInfo, "cancellationPolicy");
		mGuaranteeRequired = EvaXpediaDatabase.getSafeBool(jRateInfo, "guaranteeRequired");
		mDepositRequired = EvaXpediaDatabase.getSafeBool(jRateInfo, "depositRequired");

		
		try {
			JSONObject jChargeableRateInfo = jRateInfo.getJSONObject("ChargeableRateInfo");
			
			mChargableRateInfo = new ChargeableRateInfo(jChargeableRateInfo);
			
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

}
