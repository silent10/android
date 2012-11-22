package com.evature.search.vayant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Flight {

	public String org;
	public String dst;
	public String ocxr;
	public String mcxr;
	public String dep;
	public String arr;
	public int seg;
	public Date arrivalDateTime;
	public Date departureDateTime;

	public Flight(JSONObject flight) {
		try {
			org = flight.getString("org");
			dst = flight.getString("dst");
			ocxr = flight.getString("ocxr");
			mcxr = flight.getString("mcxr");
			dep = flight.getString("dep");
			arr = flight.getString("arr");
			seg = flight.getInt("seg");

			// String mytime = "2011-12-03 12:00:19"; // 2012-06-28T14:35:00
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			try {
				arrivalDateTime = dateFormat.parse(arr);
				departureDateTime = dateFormat.parse(dep);
			} catch (ParseException e) {
				Log.e("VAYANT", "Bad Date = " + arr);
			}

		} catch (JSONException e) {
			Log.e("VAYANT", "Bad flight");
		}
	}

}
