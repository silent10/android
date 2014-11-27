package com.evature.util;


public class Log {
	static public boolean DEBUG = false;
	private static String getCallingInfo() {
		return getCallingInfo(5);
	}
	private static String getCallingInfo(int upframes) {
		try {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[upframes];
			String fullClassName = stackTraceElement.getClassName();
		    String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
		    //String methodName = stackTraceElement.getMethodName();
		    int lineNumber = stackTraceElement.getLineNumber();
		    
		    return "("+className+".java:"+lineNumber+")";
		}
		catch (Exception e) {
			return "";
		}

	}
	
	public static void d(String tag, String txt) {
		if (DEBUG)
			android.util.Log.d(tag, txt+getCallingInfo());
	}
	
	public static void d(String tag, String txt, Throwable e) {
		if (DEBUG)
			android.util.Log.d(tag, txt+getCallingInfo(), e);
	}
	
	public static void e(String tag, String txt) {
		if (DEBUG)
			android.util.Log.e(tag, txt+getCallingInfo(6));  // error logs have another logError wrapper in VHA
	}
	
	public static void e(String tag, String txt, Throwable e) {
		if (DEBUG)
			android.util.Log.e(tag, txt+getCallingInfo(6), e);
	}
	
	public static String getStackTraceString(Throwable e) {
		return android.util.Log.getStackTraceString(e);
	}
	
	public static void i(String tag, String txt) {
		if (DEBUG)
			android.util.Log.i(tag, txt+getCallingInfo());
	}
	
	public static void i(String tag, String txt, Throwable e) {
		if (DEBUG)
			android.util.Log.i(tag, txt, e);
	}
	
	public static boolean isLoggable(String tag, int lvl) {
		return DEBUG && android.util.Log.isLoggable(tag, lvl);
	}
	
	public static void println(int lvl, String tag, String txt) {
		if (DEBUG)
			android.util.Log.println(lvl, tag, txt+getCallingInfo());
	}
	
	public static void v(String tag, String txt) {
		if (DEBUG)
			android.util.Log.v(tag, txt+getCallingInfo());
	}
	
	public static void v(String tag, String txt, Throwable e) {
		if (DEBUG)
			android.util.Log.v(tag, txt+getCallingInfo(), e);
	}
	
	public static void w(String tag, String txt) {
		if (DEBUG)
			android.util.Log.w(tag, txt+getCallingInfo());
	}
	
	public static void w(String tag, String txt, Throwable e) {
		if (DEBUG)
			android.util.Log.w(tag, txt+getCallingInfo(), e);
	}
	
	
	public static void w(String tag, Throwable e) {
		if (DEBUG)
			android.util.Log.w(tag, e);
	}
	
	public static void wtf(String tag, String txt) {
		if (DEBUG)
			android.util.Log.wtf(tag, txt+getCallingInfo());
	}
	
	public static void wtf(String tag, String txt, Throwable e)  {
		if (DEBUG)
			android.util.Log.wtf(tag, txt+getCallingInfo(), e);
	}
	
	public static void wtf(String tag, Throwable e) {
		if (DEBUG)
			android.util.Log.wtf(tag, e);
	}
}
