package com.evature.evasdk.evaapis.crossplatform;

import com.evature.evasdk.util.DLog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.evature.evasdk.evaapis.crossplatform.RequestAttributes.SortEnum.location;


public class ParsedText implements Serializable {

	static public class TimesMarkup {
		public String text;
		public String type; // eg. departure, arrival
		public int position;
		// value, related location
	}
	
	static public class LocationMarkup {
		public String text;
		public int position;
	}

	static public class HotelAttributesMarkup {
        public String text;
        public String type; // eg. departure, arrival
        public int position;
    }
	
	public ArrayList<TimesMarkup> times;
	public ArrayList<LocationMarkup> locations;
    public ArrayList<HotelAttributesMarkup> hotelAttributes;
	
	public ParsedText(JSONObject jsonObject, List<String> parseErrors) {
		if (jsonObject.has("Times")) {
			try {
				times = new ArrayList<TimesMarkup>();
				JSONArray jTimes = jsonObject.getJSONArray("Times");
				for (int index = 0; index < jTimes.length(); index++) {
					JSONObject jTime = jTimes.getJSONObject(index);
					TimesMarkup time = new TimesMarkup();
					time.text = jTime.optString("Text");
					time.position = jTime.optInt("Position", -1);
					time.type = jTime.optString("Type");
                    if (time.position != -1 && !"".equals(time.text)) {
                        times.add(time);
                    }
				}
			} catch (JSONException e) {
				DLog.e("ParsedText", "Error parsing JSON", e);
				parseErrors.add("Failed to parse Times in ParsedText");
			}
		}
		
		if (jsonObject.has("Locations")) {
			try {
				locations = new ArrayList<LocationMarkup>();
				JSONArray jLocations = jsonObject.getJSONArray("Locations");
				for (int index = 0; index < jLocations.length(); index++) {
					JSONObject jLocation = jLocations.getJSONObject(index);
					LocationMarkup location = new LocationMarkup();
					location.text = jLocation.optString("Text");
					location.position = jLocation.optInt("Position", -1);
                    if (location.position != -1 && !"".equals(location.text)) {
                        locations.add(location);
                    }
				}
			} catch (JSONException e) {
				DLog.e("ParsedText", "Error parsing JSON",e);
				parseErrors.add("Failed to parse Locations in ParsedText");
			}
		}

		if (jsonObject.has("Hotel Attributes")) {
            try {
                hotelAttributes = new ArrayList<HotelAttributesMarkup>();
                JSONArray jHotelAttrs = jsonObject.getJSONArray("Hotel Attributes");
                for (int index = 0; index < jHotelAttrs.length(); index++) {
                    JSONObject jHotelAttr = jHotelAttrs.getJSONObject(index);
                    HotelAttributesMarkup hotelAttr = new HotelAttributesMarkup();
                    hotelAttr.text = jHotelAttr.optString("Text", "");
                    hotelAttr.position = jHotelAttr.optInt("Position", -1);
                    hotelAttr.type = jHotelAttr.optString("Type", null);
                    if (hotelAttr.position != -1 && !"".equals(hotelAttr.text)) {
                        // don't highlight "hotel" accommodation type
                        if (!"Accommodation Type".equals(hotelAttr.type) || !"Hotel".equals(jHotelAttr.optString("Value"))) {
                            hotelAttributes.add(hotelAttr);
                        }
                    }
                }
            } catch (JSONException e) {
                DLog.e("ParsedText", "Error parsing JSON",e);
                parseErrors.add("Failed to parse Locations in ParsedText");
            }
        }
	}

}
