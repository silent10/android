package com.virtual_hotel_agent.search.views.fragments;

import android.app.Activity;

import com.virtual_hotel_agent.search.controllers.activities.HotelsMapActivity;

public class HotelsMapFragment extends ActivityHostFragment {

	@Override
	protected Class<? extends Activity> getActivityClass() {
		return HotelsMapActivity.class;
	}

}