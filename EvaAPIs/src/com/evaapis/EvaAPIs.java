package com.evaapis;

import android.content.Context;

import com.evature.util.ExternalIpAddressGetter;

public class EvaAPIs {
	
	public static String API_KEY="UNKNOWN";
	public static String SITE_CODE="UNKNOWN";
	private static ExternalIpAddressGetter mExternalIpAddressGetter;

	
	public static double getLongitude()
	{
		EvatureLocationUpdater location=null;
		try {
			location = EvatureLocationUpdater.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return location.getLongitude();
	}
	
	public static double getLatitude()
	{
		EvatureLocationUpdater location=null;
		try {
			location = EvatureLocationUpdater.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		return location.getLatitude();
	}
	
	
	public static void setKeys(String api_key, String site_code)
	{
		API_KEY = api_key;
		SITE_CODE = site_code;
	}
	
	public static void start(Context appContext)
	{
		mExternalIpAddressGetter = new ExternalIpAddressGetter();
		EvatureLocationUpdater.initContext(appContext);
	}
}
