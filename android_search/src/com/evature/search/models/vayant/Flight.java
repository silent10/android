package com.evature.search.models.vayant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Flight {

	public String origin;
	public String destination;
	public String operatingCarrier;
	public String marketingCarrier;
	public int segment;
	public Date arrivalDateTime;
	public Date departureDateTime;

	public Flight(JSONObject flight) {
		try {
			origin = flight.getString("org");
			destination = flight.getString("dst");
			operatingCarrier = flight.getString("ocxr");
			marketingCarrier = flight.getString("mcxr");
			String departureDate = flight.getString("dep");
			String arrivalDate = flight.getString("arr");
			segment = flight.getInt("seg");

			// String mytime = "2011-12-03 12:00:19"; // 2012-06-28T14:35:00
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			try {
				arrivalDateTime = dateFormat.parse(arrivalDate);
				departureDateTime = dateFormat.parse(departureDate);
			} catch (ParseException e) {
				Log.e("VAYANT", "Bad Date = " + arrivalDate);
			}

		} catch (JSONException e) {
			Log.e("VAYANT", "Bad flight");
		}
	}

}
