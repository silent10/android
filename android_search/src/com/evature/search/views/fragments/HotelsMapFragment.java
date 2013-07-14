package com.evature.search.views.fragments;

import android.app.Activity;

import com.evature.search.controllers.activities.HotelsMapActivity;

public class HotelsMapFragment extends ActivityHostFragment {

	@Override
	protected Class<? extends Activity> getActivityClass() {
		return HotelsMapActivity.class;
	}

}