package com.virtual_hotel_agent.search.models.expedia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;




public class HotelDetails {

	public String propertyDescription;
	
	public HotelDetails(JSONObject jobj) {
		try {
			JSONObject jImagesObject = jobj.getJSONObject("HotelImages");
			JSONArray jImagesArray = jImagesObject.getJSONArray("HotelImage");
			
			
			JSONObject jHotelDetails = jobj.getJSONObject("HotelDetails");
			
			propertyDescription = jHotelDetails.getString("propertyDescription");
			
			if (jHotelDetails.optString("drivingDirections", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Driving Directions &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("drivingDirections");
				propertyDescription += "&lt;/p&gt;";
			}
			
			if (jHotelDetails.optString("hotelPolicy", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Hotel Policy &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("hotelPolicy");
				propertyDescription += "&lt;/p&gt;";
			}
			
			if (jHotelDetails.optString("checkInInstructions", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Check In Instructions &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("checkInInstructions");
				propertyDescription += "&lt;/p&gt;";
			}
			
			if (jHotelDetails.optString("roomInformation", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Room Information &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("roomInformation");
				propertyDescription += "&lt;/p&gt;";
			}
			
			if (jHotelDetails.optString("propertyInformation", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Property Information &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("propertyInformation");
				propertyDescription += "&lt;/p&gt;";
			}
			
			if (jHotelDetails.optString("areaInformation", "").trim().equals("") == false) {
				propertyDescription += "&lt;p&gt; &lt;b&gt;Area Information &lt;/b&gt; &lt;br /&gt;";
				propertyDescription += jHotelDetails.getString("areaInformation");
				propertyDescription += "&lt;/p&gt;";
			}
			
			
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
			MainActivity.LogError("XPD","FullHotelDetails:JSON Element not found!");
		}
	}
	
	public class HotelImage
	{
		int hotelImageId;
		public String name;
//		int category;
		int type;
		public String caption;
		public String url;
		public String thumbnailUrl;
		
		HotelImage(JSONObject jobj)
		{
			try {
				hotelImageId = jobj.getInt("hotelImageId");
				name = jobj.getString("name");
//				category = jobj.getInt("category");
				type = jobj.getInt("type");
				if (jobj.has("caption"))
					caption = jobj.getString("caption");
				url = jobj.getString("url");
				thumbnailUrl = jobj.getString("thumbnailUrl");
			} catch (JSONException e) {
				MainActivity.LogError("XPD","HotelImage: JSON Element not found!");
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
				MainActivity.LogError("XPD","Amenity: JSON Element not found!");
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
				MainActivity.LogError("XPD","RoomType: JSON Element not found!");
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
