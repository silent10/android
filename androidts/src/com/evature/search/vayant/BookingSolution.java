package com.evature.search.vayant;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BookingSolution {

	public Flight mFlights[];
	public double mPrice;
	public List<Segment> mSegments = new ArrayList<Segment>();

	public BookingSolution(JSONObject solution) {
		JSONArray flights;
		try {
			flights = solution.getJSONArray("Flights");
			mFlights = new Flight[flights.length()];
			for (int index = 0; index < flights.length(); index++) {
				Flight flight = new Flight(flights.getJSONObject(index));
				mFlights[index] = flight;
				if (flight.seg > mSegments.size()) {
					mSegments.add(new Segment());
				}
				mSegments.get(flight.seg - 1).addFlight(flight);
			}
			mPrice = solution.getJSONObject("Price").getJSONObject("Total").optDouble("sum");
		} catch (JSONException e) {
			Log.e("VAYANT", "Bad solution");
		}

	}
}
