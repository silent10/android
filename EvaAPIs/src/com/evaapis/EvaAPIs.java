package com.evaapis;

import com.evature.util.ExternalIpAddressGetter;

public class EvaAPIs {
	
	public static String API_KEY="UNKNOWN";
	public static String SITE_CODE="UNKNOWN";
	private static ExternalIpAddressGetter mExternalIpAddressGetter;

	public static void setKeys(String api_key, String site_code)
	{
		API_KEY = api_key;
		SITE_CODE = site_code;
	}
	
	public static void start()
	{
		mExternalIpAddressGetter = new ExternalIpAddressGetter();
	}
}
