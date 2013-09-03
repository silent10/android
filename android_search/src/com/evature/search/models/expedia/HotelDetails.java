package com.evature.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import android.util.Log;

public class HotelDetails {

	public String propertyDescription;
	
	public HotelDetails(JSONObject jobj) {
		try {
			JSONObject jImagesObject = jobj.getJSONObject("HotelImages");
			JSONArray jImagesArray = jImagesObject.getJSONArray("HotelImage");
			
			
			JSONObject jHotelDetails = jobj.getJSONObject("HotelDetails");
			
			propertyDescription = jHotelDetails.getString("propertyDescription");
			
			hotelImages = new HotelImage[jImagesArray.length()];
			
			int index;
			
			for( index=0; index<jImagesArray.length(); index++)
			{
				hotelImages[index]=new HotelImage(jImagesArray.getJSONObject(index));
			}
			
			JSONObject jAmenitiesObject = jobj.getJSONObject("PropertyAmenities");
			JSONArray jAmenitiesArray = jAmenitiesObject.getJSONArray("PropertyAmenity");
			propertyAmenities = new Amenity[jAmenitiesArray.length()];
			for( index=0; index<jAmenitiesArray.length(); index++)
			{
				propertyAmenities[index]=new Amenity(jAmenitiesArray.getJSONObject(index));
			}
			
			JSONObject jRoomTypesObject = jobj.getJSONObject("RoomTypes");
			JSONArray jRoomTypesArray = jRoomTypesObject.getJSONArray("RoomType");
			roomTypes = new RoomType[jRoomTypesArray.length()];
			for( index=0; index<jRoomTypesArray.length(); index++)
			{
				roomTypes[index]=new RoomType(jRoomTypesArray.getJSONObject(index));
			}

			jobj = jobj.getJSONObject("HotelSummary"); 
						
		} catch (JSONException e) {
			Log.e("XPD","FullHotelDetails:JSON Element not found!");
			e.printStackTrace();
		}
	}
	
	public class HotelImage
	{
		int hotelImageId;
		String name;
		int category;
		int type;
		String caption;
		public String url;
		String thumbnailUrl;
		
		HotelImage(JSONObject jobj)
		{
			try {
				hotelImageId = jobj.getInt("hotelImageId");
				name = jobj.getString("name");
				category = jobj.getInt("category");
				type = jobj.getInt("type");
				if (jobj.has("caption"))
					caption = jobj.getString("caption");
				url = jobj.getString("url");
				thumbnailUrl = jobj.getString("thumbnailUrl");
			} catch (JSONException e) {
				Log.e("XPD","HotelImage: JSON Element not found!");
				e.printStackTrace();
			}
			
		
		}
	}
	
	public HotelImage hotelImages[];
	
	class Amenity
	{
		Amenity(JSONObject jobj)
		{
			try {
				amenityId = jobj.getInt("amenityId");	
				amenity = jobj.getString("amenity");	
			} catch (JSONException e) {
				Log.e("XPD","Amenity: JSON Element not found!");
				e.printStackTrace();
			}
		}
		
		int amenityId;
		String amenity;
	}
	
	Amenity propertyAmenities[];
	
	String customerSessionId;
	
	class RoomType
	{
		public RoomType(JSONObject jsonObject) {
			try {
				description = jsonObject.getString("description");
				descriptionLong = jsonObject.getString("descriptionLong");
				roomTypeId = jsonObject.getInt("@roomTypeId");
				roomCode = jsonObject.getInt("@roomCode");
				if (jsonObject.has("RoomAmenities")) {
					JSONObject jAmenitiesObject = jsonObject.getJSONObject("RoomAmenities");
					JSONArray jAmenitiesArray = jAmenitiesObject.getJSONArray("RoomAmenity");
					roomAmenities = new Amenity[jAmenitiesArray.length()];					
					for( int index=0; index<jAmenitiesArray.length(); index++)
					{
						roomAmenities[index]=new Amenity(jAmenitiesArray.getJSONObject(index));
					}
				}
			} catch (JSONException e) {
				Log.e("XPD","RoomType: JSON Element not found!");
				if (EvaXpediaDatabase.PRINT_STACKTRACE)
					e.printStackTrace();
			}
			
			
		}
		String descriptionLong;
		Amenity roomAmenities[];
		int roomTypeId;
		int roomCode;
		String description;
	}
	
	RoomType roomTypes[];
	
	
}
