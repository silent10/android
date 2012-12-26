package com.evature.search;

import android.app.Activity;
import android.support.v4.app.Fragment;

public class HotelsMapFragment extends ActivityHostFragment {

	@Override
	protected Class<? extends Activity> getActivityClass() {
		return HotelsMapActivity.class;
	}


	public static Fragment newInstance() {
		HotelsMapFragment f = new HotelsMapFragment();
		return f;
	}
}