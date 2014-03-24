package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class ChargeableRateInfo {

	private double mCommissionableUsdTotal;
	public double mTotal;
	public double mSurchargeTotal;
	public double mNightlyRateTotal;
	public double mAverageBaseRate;
	public double mAverageRate;
	private double mMaxNightlyRate;
	public String mCurrencyCode;	
	public NightlyRate[] mNightlyRatesPerRoom;
	public Surcharge[] mSurcharges;
	
	public double mTotalBaseRate;
	public double mTotalDiscountRate;

	public ChargeableRateInfo(JSONObject jChargeableRateInfo) {
		mCommissionableUsdTotal= XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@commissionableUsdTotal");
		mTotal = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@total");
		mSurchargeTotal = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@surchargeTotal");
		mNightlyRateTotal = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@nightlyRateTotal");
		mAverageBaseRate = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@averageBaseRate");
		mAverageRate = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@averageRate");
		mMaxNightlyRate = XpediaDatabase.getSafeDouble(jChargeableRateInfo,"@maxNightlyRate");
		mCurrencyCode = XpediaDatabase.getSafeString(jChargeableRateInfo,"@currencyCode");
		
		if(mAverageBaseRate==-1.0)
		{
			mAverageBaseRate = mMaxNightlyRate;
		}
		
		try {
			JSONObject jNightlyRatesObj = jChargeableRateInfo.getJSONObject("NightlyRatesPerRoom");
			int size = XpediaDatabase.getSafeInt(jNightlyRatesObj,"@size");
			
			
			if(size==-1)
			{
				size = 1;
			}
			
			mTotalBaseRate = 0;
			mTotalDiscountRate = 0;
			mNightlyRatesPerRoom = new NightlyRate[size];
			
			if(size>1)
			{
				JSONArray jNightlyRates = jNightlyRatesObj.getJSONArray("NightlyRate");
				for(int i=0;i<mNightlyRatesPerRoom.length;i++)
				{
					JSONObject jRate = jNightlyRates.getJSONObject(i);
				
					mNightlyRatesPerRoom[i] = new NightlyRate(jRate);
					
					mTotalBaseRate += mNightlyRatesPerRoom[i].mBaseRate;
					mTotalDiscountRate += mNightlyRatesPerRoom[i].mRate;
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
		
		if (jChargeableRateInfo.has("Surcharges")) {
			try {
				JSONObject jSurcharges = jChargeableRateInfo.getJSONObject("Surcharges");
				
				int size = XpediaDatabase.getSafeInt(jSurcharges,"@size");
				
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
			if (XpediaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
			}
		}
		
		
		

	}

}
