package com.evature.search.models.vayant;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class BookingSolution {

	public Flight mFlights[];
	public double mTotalPrice;
	public double mOutboundPrice;
	public double mInboundPrice;
	public String mCurrency;
	public List<Segment> mSegments = new ArrayList<Segment>();

	public BookingSolution(JSONObject solution) {
		JSONArray flights;
		try {
			flights = solution.getJSONArray("Flights");
			mFlights = new Flight[flights.length()];
			for (int index = 0; index < flights.length(); index++) {
				Flight flight = new Flight(flights.getJSONObject(index));
				mFlights[index] = flight;
				if (flight.segment > 2) {
					Log.e("VAYANT", "unexpexcted segment - "+flight.segment);
					continue;
				}
				while (flight.segment > mSegments.size()) {
					mSegments.add(new Segment());
				}
				mSegments.get(flight.segment - 1).addFlight(flight);
			}
			
			JSONObject jTotalPrice = solution.getJSONObject("Price").getJSONObject("Total");
			mTotalPrice = jTotalPrice.optDouble("sum");
			if (solution.has("OutboundPrice")) {
				mOutboundPrice = solution.getJSONObject("OutboundPrice").getJSONObject("Total").optDouble("sum");
			}
			if (solution.has("InboundPrice")) {
				mInboundPrice = solution.getJSONObject("InboundPrice").getJSONObject("Total").optDouble("sum");
			}
			mCurrency = jTotalPrice.optString("cur");
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e("VAYANT", "Bad solution",e);
		}

	}
}
