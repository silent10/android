package com.softskills.evasearch.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ChargeableRateInfo {

	private double mCommissionableUsdTotal;
	private double mTotal;
	private double mSurchargeTotal;
	private double mNightlyRateTotal;
	public double mAverageBaseRate;
	private double mAverageRate;
	private double mMaxNightlyRate;
	public String mCurrencyCode;	
	private NightlyRate[] mNightlyRatesPerRoom;
	private Surcharge[] mSurcharges;

	public ChargeableRateInfo(JSONObject jChargeableRateInfo) {
		mCommissionableUsdTotal= EvaDatabase.getSafeDouble(jChargeableRateInfo,"commissionableUsdTotal");
		mTotal = EvaDatabase.getSafeDouble(jChargeableRateInfo,"total");
		mSurchargeTotal = EvaDatabase.getSafeDouble(jChargeableRateInfo,"surchargeTotal");
		mNightlyRateTotal = EvaDatabase.getSafeDouble(jChargeableRateInfo,"nightlyRateTotal");
		mAverageBaseRate = EvaDatabase.getSafeDouble(jChargeableRateInfo,"@averageBaseRate");
		mAverageRate = EvaDatabase.getSafeDouble(jChargeableRateInfo,"averageRate");
		mMaxNightlyRate = EvaDatabase.getSafeDouble(jChargeableRateInfo,"@maxNightlyRate");
		mCurrencyCode = EvaDatabase.getSafeString(jChargeableRateInfo,"@currencyCode");
		
		if(mAverageBaseRate==-1.0)
		{
			mAverageBaseRate = mMaxNightlyRate;
		}
		
		try {
			JSONObject jNightlyRatesObj = jChargeableRateInfo.getJSONObject("NightlyRatesPerRoom");
			int size = EvaDatabase.getSafeInt(jNightlyRatesObj,"@size");
			
			
			if(size==-1)
			{
				size = 1;
			}
			
			mNightlyRatesPerRoom = new NightlyRate[size];
			
			if(size>1)
			{
				JSONArray jNightlyRates = jNightlyRatesObj.getJSONArray("NightlyRate");
				for(int i=0;i<mNightlyRatesPerRoom.length;i++)
				{
					JSONObject jRate = jNightlyRates.getJSONObject(i);
				
					mNightlyRatesPerRoom[i] = new NightlyRate(jRate);
				}
			}
			else
			{ 
				JSONObject jRate = jNightlyRatesObj.getJSONObject("NightlyRate");
				
				mNightlyRatesPerRoom[0] = new NightlyRate(jRate);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			JSONObject jSurcharges = jChargeableRateInfo.getJSONObject("Surcharges");
			
			int size = EvaDatabase.getSafeInt(jSurcharges,"size");
			
			if(size==-1)
			{
				size=1;
			}
							
			mSurcharges = new Surcharge[size];
			
			if(size==1)
			{
				JSONObject jSurcharge = jSurcharges.getJSONObject("Surcharge");
				
				mSurcharges[0] = new Surcharge(jSurcharge);
			}
			else
			{
				JSONArray jSurchargesArray = jSurcharges.getJSONArray("Surcharge"); 
				for(int i=0;i<size;i++)
				{
					JSONObject jSurcharge = jSurchargesArray.getJSONObject(i);
					mSurcharges[i]=new Surcharge(jSurcharge);
				}
			}
			
		} catch (JSONException e) {
			if (EvaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
		}
		
		
		

	}

}
