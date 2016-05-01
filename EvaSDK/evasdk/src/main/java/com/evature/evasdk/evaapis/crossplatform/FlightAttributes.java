package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class FlightAttributes  implements Serializable {

	private static final String TAG = "FlightAttributes";
	public Boolean nonstop = null; // A Non stop flight - Boolean attribute.
	public Boolean redeye = null; // A Red eye flight - Boolean attribute.
	public Boolean only = null; // The request is specifically asking for just-a-flight (and no hotel, car etc.) - Boolean
							// attribute
	public Boolean oneWay = null; // Specific request for one way trip. Example: ???????united airlines one way flights to ny????????
	public Boolean twoWay = null; // Specific request for round trip. Example: ???????3 ticket roundtrip from tagbilaran to manila/
							// 1/26/2011-1/30/2011????????
	public String[] airlines;
	public enum FoodType {
        Unknown, // shouldnt get this one

        // Religious:
        Kosher, GlattKosher, Muslim, Hindu,
        // Vegetarian:
        Vegetarian, Vegan, IndianVegetarian, RawVegetarian, OrientalVegetarian, LactoOvoVegetarian,
        LactoVegetarian, OvoVegetarian, JainVegetarian,
        // Medical meals:
        Bland, Diabetic, FruitPlatter, GlutenFree, LowSodium, LowCalorie, LowFat, LowFibre,
        NonCarbohydrate, NonLactose, SoftFluid, SemiFluid, UlcerDiet, NutFree, LowPurine,
        LowProtein, HighFibre,
        // Infant and child:
        Baby, PostWeaning, Child, // In airline jargon, baby and infant < 2 years. 1 year < Toddler < 3 years.
        // Other:
        Seafood, Japanese

    };
    public FoodType food;
	public enum SeatType { Unknown, Window, Aisle };
	public SeatType seatType;
	public enum SeatClass {
		Unknown,
		First, Business, Premium, Economy
	};
	public SeatClass[] seatClass;

	public FlightAttributes(JSONObject jFlightAttributes, List<String> parseErrors) {
		try {
			if (jFlightAttributes.has("Nonstop"))
				nonstop = jFlightAttributes.getBoolean("Nonstop");
			if (jFlightAttributes.has("Redeye"))
				redeye = jFlightAttributes.getBoolean("Redeye");
			if (jFlightAttributes.has("Only"))
				only = jFlightAttributes.getBoolean("Only");
			if (jFlightAttributes.has("Two-Way"))
				twoWay = jFlightAttributes.getBoolean("Two-Way");
			
			if (jFlightAttributes.has("Airline")) {
				JSONArray jAirlines = jFlightAttributes.getJSONArray("Airline");
				airlines = new String[jAirlines.length()];
				for (int i=0; i< jAirlines.length(); i++) {
					JSONObject jAirline = jAirlines.getJSONObject(i);
					airlines[i] = jAirline.getString("IATA");
				}
			}
			if (jFlightAttributes.has("Food")) {
                try {
                    food = FoodType.valueOf(jFlightAttributes.getString("Food").replace(" ","").replace("-",""));
                }
                catch(IllegalArgumentException e) {
                    parseErrors.add("Unexpected FoodType " + jFlightAttributes.optString("Food"));
                    DLog.w(TAG, "Unexpected FoodType", e);
                    food = FoodType.Unknown;
                }
			}
			
			if (jFlightAttributes.has("Seat")) {
				try {
					seatType = SeatType.valueOf(jFlightAttributes.getString("Seat"));
				}
				catch(IllegalArgumentException e) {
					parseErrors.add( "Unexpected SeatType "+jFlightAttributes.optString("Seat"));
					DLog.w(TAG, "Unexpected SeatType", e);
					seatType = SeatType.Unknown;
				}
			}
			if (jFlightAttributes.has("Seat Class")) {
				JSONArray jSeatClass = jFlightAttributes.getJSONArray("Seat Class");
				seatClass = new SeatClass[jSeatClass.length()];
				for (int i=0; i<jSeatClass.length(); i++) {
					try {
						seatClass[i] = SeatClass.valueOf(jSeatClass.getString(i));
					}
					catch(IllegalArgumentException e) {
						parseErrors.add( "Unexpected SeatClass"+jSeatClass.optString(i));
						DLog.w(TAG, "Unexpected SeatClass", e);
						seatClass[i] = SeatClass.Unknown;
					}
				}
			}
			
		} catch (JSONException e) {
			DLog.e(TAG, "Parsing JSON", e);
			parseErrors.add("Exception during parsing flight attributes: "+e.getMessage());
		}
	}
}
