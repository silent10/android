package com.virtual_hotel_agent.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.evaapis.android.EvaComponent;

public class SettingsAPI {
	// WTF WTF WTF???
	public static String getCurrencyCode(Context context) {
		if (context == null) {
			return null;
		}
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		int currencyIndex = Integer.parseInt(sp.getString("vha_preference_currency", "-1"));
		if (currencyIndex == -1) {
			Editor edit = sp.edit();
			edit.putString("vha_preference_currency", "0");
			currencyIndex = 0;
			edit.commit();
		}
		String[] entries = context.getResources().getStringArray(R.array.entries_currency_preference);

		String currencyCode;
		if (currencyIndex < entries.length)
			currencyCode = entries[currencyIndex];
		else
			currencyCode = entries[0];

		return currencyCode;
	}

	public static String getCurrencySymbol(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		int currencyIndex = Integer.parseInt(sp.getString("vha_preference_currency", "-1"));
		if (currencyIndex == -1) {
			Editor edit = sp.edit();
			edit.putString("vha_preference_currency", "0");
			currencyIndex = 0;
			edit.commit();
		}
		String[] signs = context.getResources().getStringArray(R.array.currency_signs);

		String currencySymbol;
		if (currencyIndex < signs.length)
			currencySymbol = signs[currencyIndex];
		else
			currencySymbol = signs[0];

		return currencySymbol;
	}
	
	public static String getLocale(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String locale = sp.getString(EvaComponent.LOCALE_PREF_KEY, "");
		if (locale.equals("")) {
			Editor edit = sp.edit();
			edit.putString(EvaComponent.LOCALE_PREF_KEY, "US");
			edit.commit();
			locale = "US";
		}
		return locale;
	}
	
	public static final String EVA_KEY = "eva_key";
	public static final String EVA_SITE_CODE = "eva_site_code";

	public static String getEvaKey(Context context) {
		//SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		//String val= sp.getString(EVA_KEY, "");
		//if (val.equals("")) {
			Resources resources = context.getResources();
			String val = resources.getString(R.string.EVA_API_KEY);
//			Editor edit = sp.edit();
//			edit.putString(EVA_KEY, val);
//			edit.commit();
//		}
		return val;
	}
	
	public static String getEvaSiteCode(Context context) {
//		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
//		String val = sp.getString(EVA_SITE_CODE, "");
//		if (val.equals("")) {
			Resources resources = context.getResources();
			String val = resources.getString(R.string.EVA_SITE_CODE);
//			Editor edit = sp.edit();
//			edit.putString(EVA_SITE_CODE, val);
//			edit.commit();
//		}
		return val;
	}
	
	public static boolean getShowIntroTips(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean showIntro = sp.getBoolean("vha_show_intro", true);
		return showIntro;
	}
	
	public static void setShowIntroTips(Context context, boolean val) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		Editor edit = sp.edit();
		edit.putBoolean("vha_show_intro", val);
		edit.commit();
	}
}
