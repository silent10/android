package com.virtual_hotel_agent.search.models.expedia;

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
		mHotelId = XpediaDatabase.getSafeInt(jHotel,"hotelId");
		mName = XpediaDatabase.getSafeString(jHotel,"name");
		mHotelRating = XpediaDatabase.getSafeDouble(jHotel, "hotelRating");
		mConfidenceRating  = XpediaDatabase.getSafeDouble(jHotel, "confidenceRating");
		mTripAdvisorRating  = XpediaDatabase.getSafeDouble(jHotel, "tripAdvisorRating");
		mHighRate = XpediaDatabase.getSafeDouble(jHotel, "highRate");
		mLowRate  = XpediaDatabase.getSafeDouble(jHotel, "lowRate");
		mRateCurrencyCode = XpediaDatabase.getSafeString(jHotel,"rateCurrencyCode");
		mThumbNailUrl = XpediaDatabase.getSafeString(jHotel,"thumbNailUrl");

		mAddress1 = XpediaDatabase.getSafeString(jHotel,"address1");
		mCity = XpediaDatabase.getSafeString(jHotel,"city");
		mPostalCode = XpediaDatabase.getSafeString(jHotel,"postalCode");
		mCountryCode = XpediaDatabase.getSafeString(jHotel,"countryCode");
		mAirportCode = XpediaDatabase.getSafeString(jHotel,"airportCode");
		mSupplierType = XpediaDatabase.getSafeString(jHotel,"supplierType");
		mPropertyCategory = XpediaDatabase.getSafeInt(jHotel,"propertyCategory");
		mAmenityMask = XpediaDatabase.getSafeInt(jHotel,"amenityMask");
		mLocationDescription = XpediaDatabase.getSafeString(jHotel,"locationDescription");
		mShortDescription = XpediaDatabase.getSafeString(jHotel,"shortDescription");
		mLatitude = XpediaDatabase.getSafeDouble(jHotel, "latitude");
		mLongitude = XpediaDatabase.getSafeDouble(jHotel, "longitude");
		mProximityDistance  = XpediaDatabase.getSafeDouble(jHotel, "proximityDistance");
		mProximityUnit = XpediaDatabase.getSafeString(jHotel,"proximityUnit");
		mHotelInDestination = XpediaDatabase.getSafeBool(jHotel, "hotelInDestination");

		//mDeepLink = EvaXpediaDatabase.getSafeString(jHotel,"deepLink");


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

	public void updateRoomDetails(JSONObject jServerResponse) {

		try {
			
			if(jServerResponse==null) return;			
						
			JSONObject jServerResponseData = jServerResponse.getJSONObject("HotelRoomAvailabilityResponse");

			if (jServerResponseData.has("EanWsError")) {
				Log.w(TAG, "There was an error getting hotel details");
				Log.w(TAG, jServerResponseData.getJSONObject("EanWsError").toString(4));
				return;
			}
			
			mCurrentRoomDetails = new CurrentRoomDetails();
			
			mCurrentRoomDetails.mArrivalDate = XpediaDatabase.getSafeString(jServerResponseData, "arrivalDate");
			mCurrentRoomDetails.mDepartureDate = XpediaDatabase.getSafeString(jServerResponseData, "departureDate");
			mCurrentRoomDetails.mCheckInInstructions =XpediaDatabase.getSafeString(jServerResponseData, "checkInInstructions"); 
			
			int size = XpediaDatabase.getSafeInt(jServerResponseData,"@size");

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
			if (XpediaDatabase.PRINT_STACKTRACE)
				e.printStackTrace();
			return;
		}

	}
	
	public class CurrentRoomDetails {
		public String mArrivalDate;
		public String mDepartureDate;
		public String mCheckInInstructions;
	}
}


