package com.evature.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;


public class HotelSummary {
	
	private static final String TAG = "HotelSummary";
	public int mHotelId = -1;
	public String mName;
	String mAddress1;
	public String mCity;
	String mPostalCode;
	public String mCountryCode;
	String mAirportCode;
	public String mSupplierType;
	int mPropertyCategory;
	public double mHotelRating;
	double mConfidenceRating;
	public int mAmenityMask;
	public double mTripAdvisorRating;
	String mLocationDescription;
	String mShortDescription;
	double mHighRate;
	public double mLowRate;
	String mRateCurrencyCode;
	public double mLatitude;
	public double mLongitude;
	double mProximityDistance;
	String mProximityUnit;
	boolean mHotelInDestination;
	public String mThumbNailUrl;
	String mDeepLink;
	public RoomDetails roomDetails[];
	
	public CurrentRoomDetails mCurrentRoomDetails;

	HotelSummary(JSONObject jHotel)
	{
		mHotelId = EvaXpediaDatabase.getSafeInt(jHotel,"hotelId");
		mName = EvaXpediaDatabase.getSafeString(jHotel,"name");
		mHotelRating = EvaXpediaDatabase.getSafeDouble(jHotel, "hotelRating");
		mConfidenceRating  = EvaXpediaDatabase.getSafeDouble(jHotel, "confidenceRating");
		mTripAdvisorRating  = EvaXpediaDatabase.getSafeDouble(jHotel, "tripAdvisorRating");
		mHighRate = EvaXpediaDatabase.getSafeDouble(jHotel, "highRate");
		mLowRate  = EvaXpediaDatabase.getSafeDouble(jHotel, "lowRate");
		mRateCurrencyCode = EvaXpediaDatabase.getSafeString(jHotel,"rateCurrencyCode");
		mThumbNailUrl = EvaXpediaDatabase.getSafeString(jHotel,"thumbNailUrl");

		mAddress1 = EvaXpediaDatabase.getSafeString(jHotel,"address1");
		mCity = EvaXpediaDatabase.getSafeString(jHotel,"city");
		mPostalCode = EvaXpediaDatabase.getSafeString(jHotel,"postalCode");
		mCountryCode = EvaXpediaDatabase.getSafeString(jHotel,"countryCode");
		mAirportCode = EvaXpediaDatabase.getSafeString(jHotel,"airportCode");
		mSupplierType = EvaXpediaDatabase.getSafeString(jHotel,"supplierType");
		mPropertyCategory = EvaXpediaDatabase.getSafeInt(jHotel,"propertyCategory");
		mAmenityMask = EvaXpediaDatabase.getSafeInt(jHotel,"amenityMask");
		mLocationDescription = EvaXpediaDatabase.getSafeString(jHotel,"locationDescription");
		mShortDescription = EvaXpediaDatabase.getSafeString(jHotel,"shortDescription");
		mLatitude = EvaXpediaDatabase.getSafeDouble(jHotel, "latitude");
		mLongitude = EvaXpediaDatabase.getSafeDouble(jHotel, "longitude");
		mProximityDistance  = EvaXpediaDatabase.getSafeDouble(jHotel, "proximityDistance");
		mProximityUnit = EvaXpediaDatabase.getSafeString(jHotel,"proximityUnit");
		mHotelInDestination = EvaXpediaDatabase.getSafeBool(jHotel, "hotelInDestination");

		mDeepLink = EvaXpediaDatabase.getSafeString(jHotel,"deepLink");


		JSONObject jRoomDetailsList;
		JSONObject jRoomDetails;

		try 
		{
			jRoomDetailsList = jHotel.getJSONObject("RoomRateDetailsList");
			
			try {
				JSONArray jRoomDetailsArray = jRoomDetailsList.getJSONArray("RoomRateDetails");
	
				roomDetails = new RoomDetails[jRoomDetailsArray.length()];
	
				for(int i=0; i<jRoomDetailsArray.length(); i++)
				{
					jRoomDetails = jRoomDetailsArray.getJSONObject(i);
					roomDetails[i] = new RoomDetails(jRoomDetails);
				}
	
	
			} catch (JSONException e) {
				try {
					JSONObject jRoomDetailsObj = jRoomDetailsList.getJSONObject("RoomRateDetails");
					roomDetails = new RoomDetails[1];
					roomDetails[0] = new RoomDetails(jRoomDetailsObj);
				} catch (JSONException e2) {
					Log.e(TAG, "Json Exception getting RoomRateDetails", e2);
					return;
				}
			}
		}
		catch (JSONException e2) {
			Log.e(TAG, "Json Exception getting RoomRateDetailsList", e2);
			return;
		}
	}

	public void updateRoomDetails(String str) {

		JSONObject jServerResponse;
		try {
			
			if(str==null) return;			
			
			jServerResponse = new JSONObject(str);
						
			JSONObject jServerResponseData = jServerResponse.getJSONObject("HotelRoomAvailabilityResponse");

			if (jServerResponseData.has("EanWsError")) {
				Log.w(TAG, "There was an error getting hotel details");
				Log.w(TAG, jServerResponseData.getJSONObject("EanWsError").toString(4));
				return;
			}
			
			mCurrentRoomDetails = new CurrentRoomDetails();
			
			mCurrentRoomDetails.mRateKey = EvaXpediaDatabase.getSafeString(jServerResponseData, "rateKey");
			mCurrentRoomDetails.mArrivalDate = EvaXpediaDatabase.getSafeString(jServerResponseData, "arrivalDate");
			mCurrentRoomDetails.mDepartureDate = EvaXpediaDatabase.getSafeString(jServerResponseData, "departureDate");
			
			int size = EvaXpediaDatabase.getSafeInt(jServerResponseData,"@size");

			if(size==-1) size = 1;

			roomDetails = new RoomDetails[size];

			if(size==1)
			{
				JSONObject jRoomDetails = jServerResponseData.getJSONObject("HotelRoomResponse");
				roomDetails[0] = new RoomDetails(jRoomDetails);
			}
			else
			{
				JSONArray jRoomDetalisArray = jServerResponseData.getJSONArray("HotelRoomResponse");
				for(int i=0; i<size; i++)
				{
					JSONObject jRoomDetails;
					jRoomDetails = jRoomDetalisArray.getJSONObject(i);
					roomDetails[i] = new RoomDetails(jRoomDetails);
				}
			}


		} catch (JSONException e) {			
			if (EvaXpediaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
			return;
		}

	}
	
	public class CurrentRoomDetails {
		public String mRateKey;
		public String mArrivalDate;
		public String mDepartureDate;
	}
}


