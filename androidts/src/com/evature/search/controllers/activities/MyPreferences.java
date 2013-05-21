/**
 * 
 */
package com.evature.search.controllers.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.evature.search.R;
import com.evature.search.R.string;
import com.evature.search.R.xml;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class MyPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	// list summary will show current selection: http://stackoverflow.com/a/531927/78234
	private ListPreference mListPreferenceCategory;
	private ListPreference mEnginePreference;
	private static final String LANGUAGE_LIST_KEY = "languages";
	private static final String ENGINE_KEY = "engine";

	// This approach was deprecated, but the new fragments approach is not backwards compatible!!!
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences); // Yeah, I know, I know...
		mEnginePreference = (ListPreference) findPreference(ENGINE_KEY); // Yeah, I know, I know...
		mListPreferenceCategory = (ListPreference) findPreference(LANGUAGE_LIST_KEY); // Yeah, I know, I know...
		if (mListPreferenceCategory != null) {
			Bundle b = getIntent().getExtras();
			ArrayList<String> languages = b.getStringArrayList("mLanguages");
			List<String> displayNames = new ArrayList<String>();
			List<String> languageCodes = new ArrayList<String>();
			if (languages != null) {
				for (String language : languages) {
					if (language.indexOf('-') != -1 && language.length() == 5) { // Gonna skip some here that are harder
																					// to parse! like "latin"
						Locale locale = new Locale(language.substring(0, 2), language.substring(3, 5));
						displayNames.add(locale.getDisplayName());
						languageCodes.add(language);
					}
				}
				mListPreferenceCategory.setEntries(displayNames.toArray(new CharSequence[displayNames.size()]));
				mListPreferenceCategory.setEntryValues(languageCodes.toArray(new CharSequence[languageCodes.size()]));
			} else {
				mListPreferenceCategory.setSummary(getResources().getString(R.string.summary_of_empty_languages_list));
				mListPreferenceCategory.setEnabled(false); // Gray out the preferences?
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Setup the initial summary values
		mListPreferenceCategory.setSummary("Current value is " + mListPreferenceCategory.getEntry());
		mEnginePreference.setSummary("Using " + mEnginePreference.getEntry());
		// Set up a listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();

		// Unregister the listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Let's do something a preference value changes
		if (key.equals(LANGUAGE_LIST_KEY)) {
			mListPreferenceCategory.setSummary("Current value is " + mListPreferenceCategory.getEntry());
		}
		if (key.equals(ENGINE_KEY)) {
			mEnginePreference.setSummary("Using " + mEnginePreference.getEntry());
		}
	}

}

// Remove a preference: http://stackoverflow.com/questions/2240326/remove-hide-a-preference-from-the-screen
