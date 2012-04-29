package com.softskills.evasearch.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HotelSummary {
	
	public int mHotelId;
	public String mName;
	String mAddress1;
	String mAddress2;
	public String mCity;
	String mPostalCode;
	public String mCountryCode;
	String mAirportCode;
	String mSupplierType;
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
		mHotelId = EvaDatabase.getSafeInt(jHotel,"hotelId");
		mName = EvaDatabase.getSafeString(jHotel,"name");
		mHotelRating = EvaDatabase.getSafeDouble(jHotel, "hotelRating");
		mConfidenceRating  = EvaDatabase.getSafeDouble(jHotel, "confidenceRating");
		mTripAdvisorRating  = EvaDatabase.getSafeDouble(jHotel, "tripAdvisorRating");
		mHighRate = EvaDatabase.getSafeDouble(jHotel, "highRate");
		mLowRate  = EvaDatabase.getSafeDouble(jHotel, "lowRate");
		mRateCurrencyCode = EvaDatabase.getSafeString(jHotel,"rateCurrencyCode");
		mThumbNailUrl = EvaDatabase.getSafeString(jHotel,"thumbNailUrl");

		mAddress1 = EvaDatabase.getSafeString(jHotel,"address1");
		mAddress2 = EvaDatabase.getSafeString(jHotel,"address2");
		mCity = EvaDatabase.getSafeString(jHotel,"city");
		mPostalCode = EvaDatabase.getSafeString(jHotel,"postalCode");
		mCountryCode = EvaDatabase.getSafeString(jHotel,"countryCode");
		mAirportCode = EvaDatabase.getSafeString(jHotel,"airportCode");
		mSupplierType = EvaDatabase.getSafeString(jHotel,"supplierType");
		mPropertyCategory = EvaDatabase.getSafeInt(jHotel,"propertyCategory");
		mAmenityMask = EvaDatabase.getSafeInt(jHotel,"amenityMask");
		mLocationDescription = EvaDatabase.getSafeString(jHotel,"locationDescription");
		mShortDescription = EvaDatabase.getSafeString(jHotel,"shortDescription");
		mLatitude = EvaDatabase.getSafeDouble(jHotel, "latitude");
		mLongitude = EvaDatabase.getSafeDouble(jHotel, "longitude");
		mProximityDistance  = EvaDatabase.getSafeDouble(jHotel, "proximityDistance");
		mProximityUnit = EvaDatabase.getSafeString(jHotel,"proximityUnit");
		mHotelInDestination = EvaDatabase.getSafeBool(jHotel, "hotelInDestination");

		mDeepLink = EvaDatabase.getSafeString(jHotel,"deepLink");


		JSONObject jRoomDetailsList;
		JSONObject jRoomDetails;

		try 
		{
			jRoomDetailsList = jHotel.getJSONObject("RoomRateDetailsList");

			int size = EvaDatabase.getSafeInt(jRoomDetailsList,"size");

			if(size==-1) size = 1;

			roomDetails = new RoomDetails[size];

			JSONArray jRoomDetalisArray = jRoomDetailsList.getJSONArray("RoomRateDetails");
			for(int i=0; i<size; i++)
			{
				jRoomDetails = jRoomDetalisArray.getJSONObject(i);
				roomDetails[i] = new RoomDetails(jRoomDetails);
			}


		} catch (JSONException e) {
			if (EvaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
			return;
		}
	}

	public void updateRoomDetails(String str) {

		JSONObject jServerResponse;
		try {
			
			if(str==null) return;			
			
			jServerResponse = new JSONObject(str);
						
			JSONObject jServerResponseData = jServerResponse.getJSONObject("HotelRoomAvailabilityResponse");

			mCurrentRoomDetails = new CurrentRoomDetails();
			
			mCurrentRoomDetails.mRateKey = EvaDatabase.getSafeString(jServerResponseData, "rateKey");
			mCurrentRoomDetails.mArrivalDate = EvaDatabase.getSafeString(jServerResponseData, "arrivalDate");
			mCurrentRoomDetails.mDepartureDate = EvaDatabase.getSafeString(jServerResponseData, "departureDate");
			
			int size = EvaDatabase.getSafeInt(jServerResponseData,"@size");

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
			if (EvaDatabase.PRINT_STACKTRACE)
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


