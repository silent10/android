package com.evaapis.crossplatform;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evature.util.Log;

public class HotelAttributes {
	private static final String TAG = "HotelAttributes";

	// The chain of the hotel (e.g. Sheraton)
	public String chain;

	// The hotel board:
	public Boolean selfCatering;
	public Boolean bedAndBreakfast;
	public Boolean halfBoard;
	public Boolean fullBoard;
	public Boolean allInclusive;
	public Boolean drinksInclusive;

	// The quality of the hotel, measure in Stars
	public Integer minStars = null;
	public Integer maxStars = null;
	
	// TODO:  Numeric Rating, Rating
	
	public enum Amenities {
		ChildFree, 		// A Child free hotel (adults only) 
		Business,		// A Business hotel
		AirportShuttle,  // Airport Shuttle services
		Casino,
		Fishing,         // The hotel has fishing facilities (or is located next to fishing facilities)
		SnowConditions,  // The area has good snow conditions
		Snorkeling,  // The hotel has snorkeling facilities (or is located next to snorkeling facilities)
		Diving,     // The hotel has diving facilities (or is located next to diving facilities)
		Activity,  // An Activity hotel 
		Ski,
		SkiInOut,
		Golf,
		KidsForFree,
		City,
		Family,
		PetFriendly,
		Romantic,
		Adventure,
		Designer,
		Gym,
		Quiet,
		MeetingRoom,
		Restaurant,
		Gourmet,
		Disabled,
		Spa,
		Castle,
		Sport,
		Countryside
	}
	
	public HashSet<Amenities> amenities = new HashSet<Amenities>();
	
	private static final Map<String, Amenities> EVA_CODE_TO_AMENITIES;
	static {
		Map<String, Amenities> aMap = new HashMap<String, Amenities>();
        aMap.put("Child Free", Amenities.ChildFree);
        aMap.put("Business", Amenities.Business);
		aMap.put("Airport Shuttle", Amenities.AirportShuttle);
		aMap.put("Casino", Amenities.Casino);
		aMap.put("Fishing", Amenities.Fishing);
		aMap.put("Snow Conditions", Amenities.SnowConditions);
		aMap.put("Snorkeling", Amenities.Snorkeling);
		aMap.put("Diving", Amenities.Diving);
		aMap.put("Activity", Amenities.Activity);
		aMap.put("Ski", Amenities.Ski);
		aMap.put("Ski In/Out", Amenities.SkiInOut);
		aMap.put("Golf", Amenities.Golf);
		aMap.put("Kids for free", Amenities.KidsForFree);
		aMap.put("City", Amenities.City);
		aMap.put("Family", Amenities.Family);
		aMap.put("Pet Friendly", Amenities.PetFriendly);
		aMap.put("Romantic", Amenities.Romantic);
		aMap.put("Adventure", Amenities.Adventure);
		aMap.put("Designer", Amenities.Designer);
		aMap.put("Gym", Amenities.Gym);
		aMap.put("Quiet", Amenities.Quiet);
		aMap.put("Meeting Room", Amenities.MeetingRoom);
		aMap.put("Restaurant", Amenities.Restaurant);
		aMap.put("Gourmet", Amenities.Gourmet);
		aMap.put("Disabled", Amenities.Disabled);
		aMap.put("Spa", Amenities.Spa);
		aMap.put("Castle", Amenities.Castle);
		aMap.put("Sport", Amenities.Sport);
		aMap.put("Countryside", Amenities.Countryside);
        EVA_CODE_TO_AMENITIES = Collections.unmodifiableMap(aMap);
	}
	
	public static enum PoolType {
	    Any, Indoor, Outdoor
	}
	public PoolType pool;
	
	public static enum AccommodationType {
		Chalet, Villa, Apartment, Motel, Camping, Hostel, MobileHome, GuestHouse, HolidayVillage,
		HotelResidence, GuestAccommodations, Resort, Hotel,	Zimmer, Farm, YouthHostel, Bungalow, Inn
	}
	public AccommodationType accomodation;

	public Boolean Only; // The request is specifically asking for just-a-hotel (and no flight, car etc.)
	 
	//Parking facilities, contains these attributes:
	public Boolean parkingFacilities;
	public Boolean parkingValet;
	public Boolean parkingFree;
	
	
	
	// TODO: Rooms
	// TODO: Ski
	
	public HotelAttributes(JSONObject jHotelAttributes, List<String> parseErrors) {
		try {
			if (jHotelAttributes.has("Chain")) {
				chain = jHotelAttributes.getString("Chain");
			}
			
			if (jHotelAttributes.has("Board")) {
				JSONArray jBoard = jHotelAttributes.getJSONArray("Board");
				for (int i = 0; i < jBoard.length(); i++) {
					if ("Self Catering".equals(jBoard.getString(i))) {
						selfCatering = true;
					} else if ("Bed and Breakfast".equals(jBoard.getString(i))) {
						bedAndBreakfast = true;
					} else if ("Half Board".equals(jBoard.getString(i))) {
						halfBoard = true;
					} else if ("Full Board".equals(jBoard.getString(i))) {
						fullBoard = true;
					} else if ("All Inclusive".equals(jBoard.getString(i))) {
						allInclusive = true;
					} else if ("Drinks Inclusive".equals(jBoard.getString(i))) {
						drinksInclusive = true;
					}
				}
			}
			
			if (jHotelAttributes.has("Quality")) {
				JSONArray jQuality = jHotelAttributes.getJSONArray("Quality");
				minStars = jQuality.getInt(0);
				maxStars = jQuality.getInt(1);
			}
			
			if (jHotelAttributes.has("Pool")) {
				String poolType = jHotelAttributes.getString("Pool");
				pool = PoolType.valueOf(poolType);
			}

			if (jHotelAttributes.has("Accommodation Type")) {
				String accomodationType = jHotelAttributes.getString("Accommodation Type");
				accomodation = AccommodationType.valueOf(accomodationType.replaceAll(" ", ""));
			}
			
			if (jHotelAttributes.has("Parking")) {
				JSONObject parking = jHotelAttributes.getJSONObject("Parking");
				if (parking.has("Facilities")) {
					parkingFacilities = parking.getBoolean("Facilities");
				}
				if (parking.has("Valet")) {
					parkingValet = parking.getBoolean("Valet");
				}
				if (parking.has("Free")) {
					parkingFree = parking.getBoolean("Free");
				}
			}

			for (String evaCode : EVA_CODE_TO_AMENITIES.keySet()) {
				if (jHotelAttributes.has(evaCode)) {
					Amenities amenty = EVA_CODE_TO_AMENITIES.get(evaCode);
					if (jHotelAttributes.getBoolean(evaCode)) {
						amenities.add(amenty);
					}
				}
			}
		} catch (JSONException e) {
			Log.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing hotel attributes: "+e.getMessage());
		}
	}
}
