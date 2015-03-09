package com.evaapis.crossplatform;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.evaapis.crossplatform.EvaLocation.TypeEnum;
import com.evature.util.DLog;

public class CruiseAttributes {
	private static final String TAG = "CruiseAttributes";
	
	public static class Cruiseline {
		public String name;
		public String key;
	}
	
	public static class Cruiseship {
		public String name;
		public String key;
	}
	
	public Cruiseline cruiselines[];
	public Cruiseship cruiseships[];
	
	public enum CabinEnum {
		Other, 
		
		Windowless,
		RegularCabin,
		Balcony,
		PortHole, 
		Picture,
		FullWall,
		Internal,
		Oceanview,
		Window,
		External,
		Suite,
		Cabin,
		MiniSuite,
		FamilySuite,
		PresidentialSuite
	};
	
	public CabinEnum cabin;

	public enum PoolEnum {
		Other,
		Any, Indoor, Outdoor, Children
	};
	
	public PoolEnum  pool;

	public boolean family;
	public boolean romantic;
	public boolean adventure;
	public boolean childFree; // adult only
	public boolean yacht;
	public boolean barge;
	public boolean sailingShip;
	public boolean riverCruise;
	public boolean forSingles;
	public boolean forGays;
	public boolean steamboat;
	public boolean petFriendly;
	public boolean yoga;
	public boolean landTour;
	public boolean oneWay;
	
	public enum BoardEnum {
		Other,
		FullBoard, AllInclusive
	};
	public BoardEnum board;

	public Integer minStars;
	public Integer maxStars;

	public enum ShipSizeEnum {
		Other,
		Small, Medium, Large
	}
	public ShipSizeEnum shipSize;

	

	public CruiseAttributes(JSONObject cruiseAttributes, List<String> parseErrors) {

		try {
			if (cruiseAttributes.has("Cruiseline")) {
				JSONArray jCruiseLines = cruiseAttributes.getJSONArray("Cruiseline");
				cruiselines = new Cruiseline[jCruiseLines.length()];
				for (int i = 0; i < jCruiseLines.length(); i++) {
					JSONObject jCruiseline = jCruiseLines.getJSONObject(i);
					Cruiseline cruiseline = new Cruiseline();
					cruiseline.name = jCruiseline.getString("Name");
					if (jCruiseline.has("Keys")) {
						JSONObject jKeys = jCruiseline.getJSONObject("Keys");
						Iterator<String> keys = jKeys.keys();
						if (keys.hasNext()) {
							String key = keys.next();
							cruiseline.key = jKeys.getString(key);
						}
					}
					cruiselines[i] = cruiseline;
				}
			}
		
			if (cruiseAttributes.has("Cruiseship")) {
				JSONArray jCruiseships = cruiseAttributes.getJSONArray("Cruiseship");
				cruiseships = new Cruiseship[jCruiseships.length()];
				for (int i = 0; i < jCruiseships.length(); i++) {
					JSONObject jCruiseship = jCruiseships.getJSONObject(i);
					Cruiseship cruiseship = new Cruiseship();
					cruiseship.name = jCruiseship.getString("Name");
					if (jCruiseship.has("Keys")) {
						JSONObject jKeys = jCruiseship.getJSONObject("Keys");
						Iterator<String> keys = jKeys.keys();
						if (keys.hasNext()) {
							String key = keys.next();
							cruiseship.key = jKeys.getString(key);
						}
					}
					cruiseships[i] = cruiseship;
				}
			}
			
			family = cruiseAttributes.optBoolean("Family");
			romantic = cruiseAttributes.optBoolean("Romantic");
			adventure = cruiseAttributes.optBoolean("Adventure");
			childFree = cruiseAttributes.optBoolean("Child Free");
			yacht = cruiseAttributes.optBoolean("Yacht");
			barge = cruiseAttributes.optBoolean("Barge");
			sailingShip = cruiseAttributes.optBoolean("Sailing Ship");
			riverCruise = cruiseAttributes.optBoolean("River Cruise");
			forSingles = cruiseAttributes.optBoolean("For Singles");
			forGays = cruiseAttributes.optBoolean("For Gays");
			steamboat = cruiseAttributes.optBoolean("Steamboat");
			petFriendly = cruiseAttributes.optBoolean("Pet Friendly");
			yoga = cruiseAttributes.optBoolean("Yoga");
			landTour = cruiseAttributes.optBoolean("Land Tour");
			oneWay = cruiseAttributes.optBoolean("One Way");
			
			if (cruiseAttributes.has("Cabin")) {
				try {
					cabin = CabinEnum.valueOf(cruiseAttributes.getString("Cabin").replace(" ", "").replace("-", ""));
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected Cabin Type", e);
					cabin = CabinEnum.Other;
				}
			}
			
			if (cruiseAttributes.has("Pool")) {
				try {
					pool = PoolEnum.valueOf(cruiseAttributes.getString("Pool").replace(" ", ""));
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected Pool Type", e);
					pool = PoolEnum.Other;
				}
			}
			
			if (cruiseAttributes.has("Board")) {
				try {
					board = BoardEnum.valueOf(cruiseAttributes.getString("Board").replace(" ", ""));
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected Board Type", e);
					board = BoardEnum.Other;
				}
			}
			
			if (cruiseAttributes.has("Ship Size")) {
				try {
					shipSize = ShipSizeEnum.valueOf(cruiseAttributes.getString("Ship Size").replace(" ", ""));
				}
				catch(IllegalArgumentException e) {
					DLog.w(TAG, "Unexpected ShipSize Type", e);
					shipSize = ShipSizeEnum.Other;
				}
			}
			
			if (cruiseAttributes.has("Quality")) {
				JSONArray jQuality = cruiseAttributes.getJSONArray("Quality");
				minStars = jQuality.get(0) != null ?  jQuality.getInt(0) : null;
				maxStars = jQuality.get(1) != null ? jQuality.getInt(1) : null;
			}
			
		} catch (JSONException e) {
			DLog.e(TAG, "Problem parsing JSON", e);
			parseErrors
					.add("Error parsing Cruise Attr: " + e.getMessage());
		}
	}

}
