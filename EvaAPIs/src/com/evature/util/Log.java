package com.evature.util;


public class Log {
	public static void d(String tag, String txt) {
		//android.util.Log.d(tag, txt);
	}
	
	public static void d(String tag, String txt, Throwable e) {
		//android.util.Log.d(tag, txt, e);
	}
	
	public static void e(String tag, String txt) {
		android.util.Log.e(tag, txt);
	}
	
	public static void e(String tag, String txt, Throwable e) {
		android.util.Log.e(tag, txt, e);
	}
	
	public static String getStackTraceString(Throwable e) {
		return android.util.Log.getStackTraceString(e);
	}
	
	public static void i(String tag, String txt) {
		android.util.Log.i(tag, txt);
	}
	
	public static void i(String tag, String txt, Throwable e) {
		android.util.Log.i(tag, txt, e);
	}
	
	public static boolean isLoggable(String tag, int lvl) {
		return android.util.Log.isLoggable(tag, lvl);
	}
	
	public static void println(int lvl, String tag, String txt) {
		android.util.Log.println(lvl, tag, txt);
	}
	
	public static void v(String tag, String txt) {
		android.util.Log.v(tag, txt);
	}
	
	public static void v(String tag, String txt, Throwable e) {
		android.util.Log.v(tag, txt, e);
	}
	
	public static void w(String tag, String txt) {
		android.util.Log.w(tag, txt);
	}
	
	public static void w(String tag, String txt, Throwable e) {
		android.util.Log.w(tag, txt, e);
	}
	
	
	public static void w(String tag, Throwable e) {
		android.util.Log.w(tag, e);
	}
	
	public static void wtf(String tag, String txt) {
		android.util.Log.wtf(tag, txt);
	}
	
	public static void wtf(String tag, String txt, Throwable e)  {
		android.util.Log.wtf(tag, txt, e);
	}
	
	public static void wtf(String tag, Throwable e) {
		android.util.Log.wtf(tag, e);
	}
}
