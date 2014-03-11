package com.virtual_hotel_agent.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.R;

public class BookingFragement extends RoboFragment {

	private static final String TAG = "BookingFragement";
	private View mView = null;
	private int mHotelIndex;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mView != null) {
			Log.w(TAG, "Fragment initialized twice");
			((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		
		
		Context context = BookingFragement.this.getActivity();
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "Booking fragment")
				    .build()
				);

		
		mView = inflater.inflate(R.layout.fragment_bookingsummary, container, false);

		return mView;
	}

	private void fillData() {
		
	}

	public void changeHotelId(int hotelIndex) {
		if (hotelIndex == -1)
			return;
		
		Log.i(TAG, "Setting hotelId to "+hotelIndex+", was "+mHotelIndex);
		if (mHotelIndex == hotelIndex) {
			return;
		}
		mHotelIndex = hotelIndex;
		fillData();
	}


}
