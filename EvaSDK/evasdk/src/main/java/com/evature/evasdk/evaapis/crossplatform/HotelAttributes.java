package com.evature.evasdk.evaapis.crossplatform;

import android.support.v4.util.Pair;

import com.evature.evasdk.util.DLog;
import com.evature.evasdk.util.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class HotelAttributes  implements Serializable {
	private static final String TAG = "HotelAttributes";

	// The chain of the hotel (e.g. Sheraton)
	public static class HotelChain {
		public String name;
		public String simpleName;
		public String gdsCode;
		public String evaCode;

        @Override public String toString() {
            return simpleName;
        }
	}
	public ArrayList<HotelChain> chains;

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
	
	public enum PoolType {
		Unknown,
	    Any, Indoor, Outdoor
	}
	public PoolType pool;
	
	public enum AccommodationType {
		Unknown, 
		Chalet, Villa, Apartment, Motel, Camping, Hostel, MobileHome, GuestHouse, HolidayVillage,
		HotelResidence, GuestAccommodations, Resort, Hotel,	Zimmer, Farm, YouthHostel, Bungalow, Inn
	}
	public AccommodationType accomodation;

	//Parking facilities, contains these attributes:
	public Boolean parkingFacilities;
	public Boolean parkingValet;
	public Boolean parkingFree;

    public static class Room {


        public enum RoomType {
            Unknown,
            Room,  FamilyRoom, Suite
        }
        public RoomType roomType;

        public enum BedType {
            Unknown,
            Twin, King, Queen, Single, Double, Triple, Quadruple, Quintuple, Sextuple, Crib
        }
        public Pair<BedType, Integer>[] bedTypes; //  A list of pairs, each pair Type of bed and number of beds (int)

        public enum InternetType {
            Unknown,
            InternetService,  FreeInternetService, WiFi, FreeWiFi
        }

        public InternetType internet;

        public int multiple;  // How many of the same rooms (e.g. 3 airconditioned rooms)

        public Boolean airCondition;
        public Boolean cable; //  Cable TV in the room (boolean, if exists)
        public Boolean kitchen; // A Kitchen in the room (boolean, if exists)
        public Boolean miniBar; // Mini-bar in the room (boolean, if exists)
        public Boolean tub; // A hot tub in the room (boolean, if exists)
        public Boolean smoking; // Either Smoking (True) or Non Smoking (False) room (boolean, if exists)

        private Room(JSONObject jRoom) {
            String jRoomType = jRoom.optString("Room Type", null);
            if (jRoomType != null) {
                try {
                    roomType = RoomType.valueOf(jRoomType.replaceAll(" ", ""));
                } catch (IllegalArgumentException e) {
                    DLog.w(TAG, "Unexpected RoomType in Flow element", e);
                    roomType = RoomType.Unknown;
                }
            }

            String jInternetType = jRoom.optString("Internet", null);
            if (jInternetType != null) {
                try {
                    internet = InternetType.valueOf(StringUtils.toCamelCase(jInternetType));
                } catch (IllegalArgumentException e) {
                    DLog.w(TAG, "Unexpected InternetType in Flow element", e);
                    internet = InternetType.Unknown;
                }
            }

            if (jRoom.has("Air Con")) {
                airCondition = jRoom.optBoolean("Air Con");
            }
            if (jRoom.has("Cable")) {
                cable = jRoom.optBoolean("Cable");
            }
            if (jRoom.has("Kitchen")) {
                kitchen = jRoom.optBoolean("Kitchen");
            }
            if (jRoom.has("Tub")) {
                tub = jRoom.optBoolean("Tub");
            }
            if (jRoom.has("Mini Bar")) {
                miniBar = jRoom.optBoolean("Mini Bar");
            }
            if (jRoom.has("Smoking")) {
                smoking = jRoom.optBoolean("Smoking");
            }
            if (jRoom.has("Multiple")) {
                multiple = jRoom.optInt("Multiple");
            }
        }
    }
	public ArrayList<Room> rooms;
	
	// TODO: Ski

    public HotelAttributes() {}

	public HotelAttributes(JSONObject jHotelAttributes, List<String> parseErrors) {
		try {
			if (jHotelAttributes.has("Chain")) {
				if (jHotelAttributes.get("Chain") instanceof JSONArray) {
					JSONArray jChains = jHotelAttributes.getJSONArray("Chain");
					this.chains = new ArrayList<HotelAttributes.HotelChain>(jChains.length());
					for (int i=0; i<jChains.length(); i++) {
						JSONObject jChain = jChains.getJSONObject(i);
						HotelChain chain = new HotelChain();
						chain.name = jChain.getString("Name");
						chain.simpleName = jChain.getString("simple_name");
						chain.gdsCode = jChain.getString("gds_code");
						chain.evaCode = jChain.getString("eva_code");
						this.chains.add(chain);
					}
				}
				else {
					this.chains = new ArrayList<HotelAttributes.HotelChain>(1);
					HotelChain chain = new HotelChain();
					chain.name = jHotelAttributes.getString("Chain");
					this.chains.add(chain);
				}
			}
            else {
                this.chains = new ArrayList<HotelAttributes.HotelChain>();
            }
		} catch (JSONException e) {
			DLog.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing hotel attributes: "+e.getMessage());
		}
		
		try {	
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
				minStars = jQuality.isNull(0) ? null : jQuality.getInt(0);
				maxStars = jQuality.isNull(1) ? null : jQuality.getInt(1);
			}
			
			if (jHotelAttributes.has("Pool")) {
				String poolType = jHotelAttributes.getString("Pool");
				try {
					pool = PoolType.valueOf(poolType);
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected PoolType", e);
					pool = PoolType.Unknown;
				}
			}

			if (jHotelAttributes.has("Accommodation Type")) {
				String accomodationType = jHotelAttributes.getString("Accommodation Type");
				try {
					accomodation = AccommodationType.valueOf(accomodationType.replaceAll(" ", ""));
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected AccommodationType in Flow element", e);
					accomodation = AccommodationType.Unknown;
				}
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
			DLog.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing hotel attributes: "+e.getMessage());
		}

        try {
            if (jHotelAttributes.has("Rooms")) {
                if (jHotelAttributes.get("Rooms") instanceof JSONArray) {
                    JSONArray jRooms = jHotelAttributes.getJSONArray("Rooms");
                    this.rooms = new ArrayList<HotelAttributes.Room>(jRooms.length());
                    for (int i=0; i<jRooms.length(); i++) {
                        JSONObject jRoom = jRooms.getJSONObject(i);
                        Room room = new Room(jRoom);
                        this.rooms.add(room);
                    }
                }
            }
            else {
                this.chains = new ArrayList<HotelAttributes.HotelChain>();
            }
        } catch (JSONException e) {
            DLog.e(TAG, "Parsing JSON", e);
            parseErrors.add("Exception during parsing hotel attributes: "+e.getMessage());
        }
	}
}
