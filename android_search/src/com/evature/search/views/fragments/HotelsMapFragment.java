package com.evature.search.views.fragments;

import com.evature.search.controllers.activities.HotelsMapActivity;

import android.app.Activity;
import android.support.v4.app.Fragment;

public class HotelsMapFragment extends ActivityHostFragment {

	@Override
	protected Class<? extends Activity> getActivityClass() {
		return HotelsMapActivity.class;
	}

}