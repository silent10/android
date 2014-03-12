package com.virtual_hotel_agent.search.models.expedia;

import java.lang.ref.SoftReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.virtual_hotel_agent.search.controllers.activities.MainActivity;




public class HotelDetails {

	public String propertyDescription;
	private SoftReference<String>  cachedPropertyDesc;
	
	private void optionalAppend(StringBuilder sb, JSONObject jObj, String element, String title) {
		String value = jObj.optString(element, "").trim();
		if (value.equals("") == false) {
			sb.append("&lt;p&gt; &lt;b&gt;")
			  .append(title)
			  .append("&lt;/b&gt; &lt;br /&gt;")
			  .append(value)
			  .append("&lt;/p&gt;");
		}
	}
	
	public HotelDetails(JSONObject jobj) {
		try {
			JSONObject jImagesObject = jobj.getJSONObject("HotelImages");
			JSONArray jImagesArray = jImagesObject.getJSONArray("HotelImage");
			
			JSONObject jHotelSummary = jobj.optJSONObject("HotelSummary");
			
			JSONObject jHotelDetails = jobj.getJSONObject("HotelDetails");

			StringBuilder descriptionStr = new StringBuilder();
			
			descriptionStr.append(jHotelDetails.optString("propertyDescription", ""));
			
			optionalAppend(descriptionStr, jHotelDetails, "drivingDirections", "Driving Directions" );
			optionalAppend(descriptionStr, jHotelDetails, "hotelPolicy", "Hotel Policy" );
			optionalAppend(descriptionStr, jHotelDetails, "checkInInstructions", "Check In Instructions" );
			optionalAppend(descriptionStr, jHotelDetails, "roomInformation", "Room Information" );
			optionalAppend(descriptionStr, jHotelDetails, "propertyInformation", "Property Information" );
			
			if (jHotelSummary != null && jHotelSummary.optString("address1", "").trim().equals("") == false) {
				descriptionStr.append("&lt;p&gt; &lt;b&gt;Address &lt;/b&gt; &lt;br /&gt;");
				descriptionStr.append(jHotelSummary.getString("address1")).append("&lt;br /&gt;");
				descriptionStr.append(jHotelSummary.optString("city",""));
				if (jHotelSummary.optString("stateProvinceCode", "").trim().equals("") == false) {
					descriptionStr.append(", ").append(jHotelSummary.getString("stateProvinceCode"));
				}
				descriptionStr.append("&lt;br /&gt;");
				descriptionStr.append(jHotelSummary.optString("postalCode", "")).append(", ");
				descriptionStr.append(jHotelSummary.optString("countryCode", "")).append("&lt;br /&gt;");
				
			}
			
			optionalAppend(descriptionStr, jHotelDetails, "areaInformation", "Area Information" );
			
			
			propertyDescription = descriptionStr.toString();
			cachedPropertyDesc = new SoftReference<String>(propertyDescription);
			
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
			
//			JSONObject jRoomTypesObject = jobj.getJSONObject("RoomTypes");
//			JSONArray jRoomTypesArray = jRoomTypesObject.getJSONArray("RoomType");
//			roomTypes = new RoomType[jRoomTypesArray.length()];
//			for( index=0; index<jRoomTypesArray.length(); index++)
//			{
//				roomTypes[index]=new RoomType(jRoomTypesArray.getJSONObject(index));
//			}

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
	
//	class RoomType
//	{
//		public RoomType(JSONObject jsonObject) {
//			try {
//				description = jsonObject.getString("description");
//				descriptionLong = jsonObject.getString("descriptionLong");
//				roomTypeId = jsonObject.getInt("@roomTypeId");
//				roomCode = jsonObject.getInt("@roomCode");
//				if (jsonObject.has("RoomAmenities")) {
//					JSONObject jAmenitiesObject = jsonObject.getJSONObject("RoomAmenities");
//					JSONArray jAmenitiesArray = jAmenitiesObject.getJSONArray("RoomAmenity");
//					roomAmenities = new Amenity[jAmenitiesArray.length()];					
//					for( int index=0; index<jAmenitiesArray.length(); index++)
//					{
//						roomAmenities[index]=new Amenity(jAmenitiesArray.getJSONObject(index));
//					}
//				}
//			} catch (JSONException e) {
//				MainActivity.LogError("XPD","RoomType: JSON Element not found!");
//			}
//			
//			
//		}
//		String descriptionLong;
//		Amenity roomAmenities[];
//		int roomTypeId;
//		int roomCode;
//		String description;
//	}
//	
//	RoomType roomTypes[];

	public boolean restoreFromCache() {
		if (propertyDescription != null) {
			return true;
		}
		String cached = cachedPropertyDesc.get();
		if (cached != null) {
			propertyDescription = cached;
			return true;
		}
		return false;
	}
	
	/***
	 * Allow the property description string to be removed from memory
	 */
	public void allowMemRelease() {
		propertyDescription = null;
	}
	
}
