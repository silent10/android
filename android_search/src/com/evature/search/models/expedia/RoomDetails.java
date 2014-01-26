package com.evature.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class RoomDetails {

	private static final String TAG = "RoomDetails";
	private String mRoomTypeCode;
	private String mRateCode;
	private int mMaxRoomOccupancy;
	private int mQuotedRoomOccupancy;
	private int mMinGuestAge;
	public String mRoomDescription;
	private int mPromoId;
	public String mPromoDescription;
	private int mCurrentAllotment;
	private boolean mPropertyAvailable;
	private boolean mPropertyRestricted;
	private int mExpediaPropertyId;
	private String mRateKey;
	public RateInfo mRateInfo;
	private ValueAdd[] mValueAdds;
	String mRateDescription;
	public String mRoomTypeDescription;
	public String mDeepLink;
	//public String mSupplierType;

	
	
	public RoomDetails(JSONObject jsonObject) {
		
		mRoomTypeDescription =	EvaXpediaDatabase.getSafeString(jsonObject, "roomTypeDescription");
		mRoomTypeCode = EvaXpediaDatabase.getSafeString(jsonObject, "roomTypeCode");
		mRateCode = EvaXpediaDatabase.getSafeString(jsonObject, "rateCode");
		mMaxRoomOccupancy = EvaXpediaDatabase.getSafeInt(jsonObject, "maxRoomOccupancy");
		mQuotedRoomOccupancy = EvaXpediaDatabase.getSafeInt(jsonObject, "quotedRoomOccupancy");
		mMinGuestAge = EvaXpediaDatabase.getSafeInt(jsonObject, "minGuestAge");
		mRoomDescription  = EvaXpediaDatabase.getSafeString(jsonObject, "roomDescription");
		mPromoId = EvaXpediaDatabase.getSafeInt(jsonObject, "promoId");
		mPromoDescription = EvaXpediaDatabase.getSafeString(jsonObject, "promoDescription");
		mCurrentAllotment = EvaXpediaDatabase.getSafeInt(jsonObject, "currentAllotment");
		mPropertyAvailable = EvaXpediaDatabase.getSafeBool(jsonObject, "propertyAvailable");
		mPropertyRestricted = EvaXpediaDatabase.getSafeBool(jsonObject, "propertyRestricted");
		mExpediaPropertyId = EvaXpediaDatabase.getSafeInt(jsonObject, "expediaPropertyId");
		//mRateKey =  EvaDatabase.getSafeString(jsonObject, "rateKey");
		mRateDescription = EvaXpediaDatabase.getSafeString(jsonObject, "rateDescription");
		mDeepLink = EvaXpediaDatabase.getSafeString(jsonObject, "deepLink");
		
		
		try {
			JSONObject jRateInfos = jsonObject.getJSONObject("RateInfos");
			JSONObject jRateInfo = jRateInfos.getJSONObject("RateInfo");
			
			mRateInfo = new RateInfo(jRateInfo);
			
			if (jsonObject.has("ValueAdds")) {
				JSONObject jValueAdds = jsonObject.getJSONObject("ValueAdds");
				
				int size = EvaXpediaDatabase.getSafeInt(jValueAdds, "@size");
				
				if(size==-1) size =1;
				
				mValueAdds = new ValueAdd[size];
				
				if(size==1)
				{
					JSONObject jValueAdd = jValueAdds.getJSONObject("ValueAdd");
					mValueAdds[0] = new ValueAdd(jValueAdd);
				}
				else
				{
					JSONArray jValueAddsArray = jValueAdds.getJSONArray("ValueAdd");
					for(int i=0;i<size;i++)
					{
						JSONObject jValueAdd = jValueAddsArray.getJSONObject(i);
						mValueAdds[i] = new ValueAdd(jValueAdd);
					}
				}
			}
		} catch (JSONException e) {	
			if (EvaXpediaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
		}
	}
	
	public String buildTravelUrl(int hotelId, String supplierType, String checkin, String checkout, int adultsCount, 
			int childNum, int ageChild1, int ageChild2, int ageChild3,
			String rateKey) {
		/*
			&targetId=AREA-572b0850-4e3f-469b-87b2-c17ed3ea049b|cities
			
			&SSgroup=control
			&isNPRRoom=false
			&selectedPrice=976.65
			&pagename=ToStep1
			&supplierType=E
		 */

		
		
		String url = "https://www.travelnow.com/templates/352395/hotels/";
		url += hotelId + "/";
		url += "book?lang=en";
		url += "&currency=" + mRateInfo.mChargableRateInfo.mCurrencyCode;
		url += "&secureUrlFromDataBridge=https%3A%2F%2Fwww.travelnow.com";
		//url += "&requestVersion=V2";
		url += "&standardCheckin=" + checkin;
		url += "&standardCheckout=" + checkout;
		url += "&isNPRRoom=false";
		
		url += "&checkin=" + checkin;
		url += "&checkout=" + checkout;
		
		//url += "&rating=0";
		url += "&roomsCount=1";
		url += "&rooms[0].adultsCount=" + adultsCount;
		if (childNum > 3) {
			childNum = 3;
		}
		if (childNum < 0) {
			childNum = 0;
		}
		url += "&rooms[0].childrenCount="+childNum;
		if (childNum > 0) {
			url += "&rooms[0].children[0].age="+ageChild1;
			if (childNum > 1) {
				url += "&rooms[0].children[1].age="+ageChild2;
				if (childNum > 2) {
					url += "&rooms[0].children[2].age="+ageChild3;
				}
			}
		}
		
		//url += "&filter.sortedBy=traveler_hl";
		//url += "&filter.lowPrice=0";
		//url += "&filter.highPrice=2147483647";
		//url += "&filter.travelerOpinion=0";
		//url += "&filter.breakfastIncluded=false";
		url += "&subscriptionInfo.termsConditionAgreement=false";
		url += "&subscriptionInfo.wantNews=false";
		url += "&subscriptionInfo.wantNewsletters=false";
		//url += "&asyncSearch=true";
		url += "&rateCode=" + mRateCode;
		url += "&roomTypeCode=" + mRoomTypeCode;
		//url += "&hrnQuoteKey=" + rateKey;
		if (mRateInfo.mChargableRateInfo.mTotal > 0) {
			url += "&selectedPrice=" + mRateInfo.mChargableRateInfo.mTotal;//mAverageBaseRate;
		}
		//url += "&linkId=HotSearch:Hot:ResultsList:Book";
		url += "&pagename=ToStep1";
//		if (mSupplierType.equals("E"))
//			url += "&supplierType=H";
//		else
			url += "&supplierType=" + supplierType;
		Log.i(TAG, "Rate Code: "+mRateCode + "   room type: "+mRoomTypeCode+ "  selectedPrice: "+mRateInfo.mChargableRateInfo.mTotal);
		return url;
	}

}
