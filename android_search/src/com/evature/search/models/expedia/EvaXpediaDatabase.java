package com.evature.search.models.expedia;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import roboguice.util.Ln;

import android.graphics.Bitmap;
import android.util.Log;



public class EvaXpediaDatabase {

	public static final boolean PRINT_STACKTRACE = true;

	private static final String TAG = "EvaXpediaDatabase";
	
	public static int retries=0;
	
	String mCustomerSessionId;
	int mNumberOfRoomsRequested;
	public boolean mMoreResultsAvailable;
	String mCacheKey;
	String mCacheLocation;
	
	public boolean hasError = false;
	public boolean unrecoverableError = false;
	public String errorMessage = null;
	public String errorVerboseMessage = null;
	public String errorHandling = null;
	

	public HotelData mHotelData[];

	public HashMap<String,Bitmap> mImagesMap;
	
	public static double getSafeDouble(JSONObject obj,String name)
	{
		double retVal;
		if (obj.has(name)) {
			try {
				retVal = obj.getDouble(name);
			} catch (JSONException e) {
				retVal = -1;
				Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
				if (PRINT_STACKTRACE)
					e.printStackTrace();
			}
		}
		else {
			retVal = -1;
			Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
		}
		return retVal;
	}


	public static String getSafeString(JSONObject obj,String name)
	{
		String retVal=null;
		if (obj.has(name)) {
			try {
				retVal = obj.getString(name);
			} catch (JSONException e) {
				Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
				if (PRINT_STACKTRACE)
					e.printStackTrace();
			}
			catch(OutOfMemoryError e)
			{
				Log.w(TAG, "Out of memory error: "+name);
				e.printStackTrace();
			}
		}
		else {
			Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
			Log.d(TAG, "");
		}
		return retVal;
	}

	public static int getSafeInt(JSONObject obj,String name)
	{
		int retVal;
		if (obj.has(name)) {
			try {
				retVal = obj.getInt(name);
			} catch (JSONException e) {
				retVal = -1;
				Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
				if (PRINT_STACKTRACE)
					e.printStackTrace();
			}
		}
		else {
			Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
			retVal = -1;
		}
		return retVal;
	}

	public static boolean getSafeBool(JSONObject obj,String name)
	{
		boolean retVal;
		if (obj.has(name)) {
			try {
				retVal = obj.getBoolean(name);
			} catch (JSONException e) {
				retVal = false;
				Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
				if (PRINT_STACKTRACE)
					e.printStackTrace();
			}
		}
		else {
			retVal = false;
			Log.w(TAG, "Expedia Json parse error: "+name+ " not found");
		}
			
		return retVal;
	}


	public EvaXpediaDatabase(JSONObject serverResponse)
	{
		mImagesMap = new HashMap<String, Bitmap>();
					
		try {						
			String response = getSafeString(serverResponse,"HotelListResponse");

			JSONObject responseObject = new JSONObject(response);

			mCustomerSessionId = getSafeString(responseObject,"customerSessionId");
			if (responseObject.has("EanWsError")) {
				JSONObject wsError = responseObject.getJSONObject("EanWsError");
				hasError = true;
				errorHandling = getSafeString(wsError, "handling");
				errorMessage = getSafeString(wsError, "presentationMessage");
				errorVerboseMessage = getSafeString(wsError, "verboseMessage");
				Log.w(TAG, "Xpedia Web service error: "+errorVerboseMessage);
				if ("UNRECOVERABLE".equals(errorHandling)) {
					unrecoverableError = true;
					Log.w(TAG, "Unrecoverable error returned from Xpedia Web service");
					return;
				}
			}
			retries=0;
			mNumberOfRoomsRequested= getSafeInt(responseObject, "numberOfRoomsRequested");
			mMoreResultsAvailable = getSafeBool(responseObject,"moreResultsAvailable");
			mCacheKey= getSafeString(responseObject, "cacheKey");
			mCacheLocation= getSafeString( responseObject, "cacheLocation");

			responseObject = responseObject.getJSONObject("HotelList");

			int size = getSafeInt(responseObject, "@size");
			Ln.d("Hotel List Response:  size=%s  cacheKey=%s  moreAvail=%s  location=%s", size, mCacheKey, mMoreResultsAvailable, mCacheLocation);

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
	
	private int mExpediaHotelId; // last viewed hotel
	
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


	public void addData(EvaXpediaDatabase db) {
		if (db.mHotelData != null && db.mHotelData.length > 0)
		{
			mCacheKey = db.mCacheKey;
			mCacheLocation = db.mCacheLocation;
			mMoreResultsAvailable = db.isMoreResultsAvailable();
			
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


	public void setHotelId(int id) {
		mExpediaHotelId = id;
	}

	public int getHotelId() {
		return mExpediaHotelId;
	}


}



