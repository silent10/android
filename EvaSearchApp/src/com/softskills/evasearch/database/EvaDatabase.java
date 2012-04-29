package com.softskills.evasearch.database;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.graphics.Bitmap;


public class EvaDatabase {

	// Indicator for printing stack trace (in a result of 77 hotels it can save up to 3 seconds)
	public static final boolean PRINT_STACKTRACE = false;
	
	String mCustomerSessionId;
	int mNumberOfRoomsRequested;
	public boolean mMoreResultsAvailable;
	String mCacheKey;
	String mCacheLocation;

	public HotelData mHotelData[];

	public HashMap<String,Bitmap> mImagesMap;
	
	static double getSafeDouble(JSONObject obj,String name)
	{
		double retVal;
		try {
			retVal = obj.getDouble(name);
		} catch (JSONException e) {
			retVal = -1;
			if (PRINT_STACKTRACE)
				e.printStackTrace();
		}
		return retVal;
	}


	static String getSafeString(JSONObject obj,String name)
	{
		String retVal=null;
		try {
			retVal = obj.getString(name);
		} catch (JSONException e) {
			retVal = null;
			if (PRINT_STACKTRACE)
				e.printStackTrace();
		}
		catch(OutOfMemoryError e)
		{
			e.printStackTrace();
		}
		return retVal;
	}

	static int getSafeInt(JSONObject obj,String name)
	{
		int retVal;
		try {
			retVal = obj.getInt(name);
		} catch (JSONException e) {
			retVal = -1;
			if (PRINT_STACKTRACE)
				e.printStackTrace();
		}
		return retVal;
	}

	static boolean getSafeBool(JSONObject obj,String name)
	{
		boolean retVal;
		try {
			retVal = obj.getBoolean(name);
		} catch (JSONException e) {
			retVal = false;
			if (PRINT_STACKTRACE)
				e.printStackTrace();
		}
		return retVal;
	}


	public EvaDatabase(JSONObject serverResponse)
	{
		mImagesMap = new HashMap<String, Bitmap>();
		
					
		try {						
			String response = getSafeString(serverResponse,"HotelListResponse");

			JSONObject responseObject = new JSONObject(response);

			mCustomerSessionId = getSafeString(responseObject,"customerSessionId");
			mNumberOfRoomsRequested= getSafeInt(responseObject, "numberOfRoomsRequested");
			setMoreResultsAvailable(getSafeBool(responseObject,"moreResultsAvailable"));
			mCacheKey= getSafeString(responseObject, "cacheKey");
			mCacheLocation= getSafeString( responseObject, "cacheLocation");

			responseObject = responseObject.getJSONObject("HotelList");

			int size = getSafeInt(responseObject, "@size");

			if(size==-1)
			{
				size = 1;
			}
			mHotelData = new HotelData[size];

			JSONArray jHotelList; 

			if(size>1)
			{
				jHotelList = responseObject.getJSONArray("HotelSummary");

				//long t_now = System.currentTimeMillis();
				
				for(int i=0;i<size;i++)
				{
					JSONObject jHotel = jHotelList.getJSONObject(i);

					mHotelData[i]=new HotelData(jHotel);
				}
				
				//Log.d("Time",""+(System.currentTimeMillis() - t_now));
			}
			else
			{
				JSONObject jHotel = responseObject.getJSONObject("HotelSummary");
				mHotelData[0]=new HotelData(jHotel);
			}
			
		}
		catch (JSONException e) {			
			if (PRINT_STACKTRACE)
				e.printStackTrace();
		}

	}

	public String mArrivalDateParam;
	public String mDepartureDateParam;
	public int mNumberOfAdultsParam;
	
	public void setNumberOfAdults(int paramFromUI) {
		mNumberOfAdultsParam = paramFromUI;
	}
	
	public int getNumberOfAdults() {
		return mNumberOfAdultsParam;
	}

	public void setArrivalDate(String paramFromEvatureResponse) {
		mArrivalDateParam = paramFromEvatureResponse;
		
	}


	public void setDepartueDate(String paramFromEvatureResponse) {
		mDepartureDateParam = paramFromEvatureResponse;
		
	}


	public String getNextQuery() {
		return "&" + "cacheKey=" + mCacheKey + "&" + "cacheLocation=" + mCacheLocation;
	}


	public void addData(EvaDatabase db) {
		if (db.mHotelData != null && db.mHotelData.length > 0)
		{
			mCacheKey = db.mCacheKey;
			mCacheLocation = db.mCacheLocation;
			setMoreResultsAvailable(db.isMoreResultsAvailable());
			
			HotelData[] newHotelData = new HotelData[mHotelData.length + db.mHotelData.length];
			int i=0;
			for (;i<mHotelData.length; i++)
				newHotelData[i] = mHotelData[i];
			for (;i<newHotelData.length; i++)
				newHotelData[i] = db.mHotelData[i-mHotelData.length];
			mHotelData = newHotelData;
		}
	}


	public boolean isMoreResultsAvailable() {
		return mMoreResultsAvailable;
	}


	public void setMoreResultsAvailable(boolean moreResultsAvailable) {
		this.mMoreResultsAvailable = moreResultsAvailable;
	}



}



