package com.evature.android_demo;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class DemoPreferences extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences); // Yeah, I know, I know...
	}
}
