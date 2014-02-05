package com.virtual_hotel_agent.search.models.expedia;

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
		mRateChange = XpediaDatabase.getSafeBool(jRateInfo,"@rateChange");
		mPromo = XpediaDatabase.getSafeBool(jRateInfo,"@promo");
		mPriceBreakdown = XpediaDatabase.getSafeBool(jRateInfo,"@priceBreakdown");
		
//		mPromoId = EvaXpediaDatabase.getSafeInt(jRateInfo, "promoId");
		mPromoDescription = XpediaDatabase.getSafeString(jRateInfo, "promoDescription");
		mPromoDetailText = XpediaDatabase.getSafeString(jRateInfo, "promoDetailText");
		mNonRefundable = XpediaDatabase.getSafeBool(jRateInfo, "nonRefundable");
		mCancelllationPolicy = XpediaDatabase.getSafeString(jRateInfo, "cancellationPolicy");
		mGuaranteeRequired = XpediaDatabase.getSafeBool(jRateInfo, "guaranteeRequired");
		mDepositRequired = XpediaDatabase.getSafeBool(jRateInfo, "depositRequired");

		
		try {
			JSONObject jChargeableRateInfo = jRateInfo.getJSONObject("ChargeableRateInfo");
			
			mChargableRateInfo = new ChargeableRateInfo(jChargeableRateInfo);
			
		} catch (JSONException e) { 
			e.printStackTrace();
		}
	}

}
