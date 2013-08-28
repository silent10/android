package com.evature.search.models.vayant;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;


public class VayantJourneys {

	private static final String TAG = "VayantJourneys";
	public Journey mJourneys[];
	public ArrayList<Journey> mOneWayJourneys;

	public VayantJourneys(JSONArray journeys) {
		mOneWayJourneys = new ArrayList<Journey>();
		mJourneys = new Journey[journeys.length()];
		for (int index = 0; index < journeys.length(); index++) {
			try {
				Journey journey = new Journey(journeys.getJSONArray(index));
				mJourneys[index] = journey;
				if (index > 0) {
					BookingSolution booking = journey.mBookingSolutions[0];
					boolean found = false;
					for (int j=0; j<index; j++) {
						BookingSolution otherBooking = mJourneys[j].mBookingSolutions[0];
						
						if (otherBooking.mOutboundPrice == booking.mOutboundPrice && 
							otherBooking.mFlights[0].departureDateTime.equals( booking.mFlights[0].departureDateTime ) &&
							otherBooking.mFlights[0].marketingCarrier.equals( booking.mFlights[0].marketingCarrier )) {
							found = true;
							break;
						}
					}
					if (found) {
						continue; // do not add to OneWayJourneys - it was already added
					}
				}
				mOneWayJourneys.add(journey);
			} catch (JSONException e) {
				Log.e(TAG, "Bad journeys array");
			}
		}

		Log.d(TAG, "# of journeys recieved = " + String.valueOf(journeys.length()));
	}
}
