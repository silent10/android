package com.evature.util;

import java.util.ArrayList;

/****
 * Class to show Debug Logs - similar to android.util.Log
 * 
 * 1. Log text is appended with (file:line) - making the log line
 * double-clickable in Eclipse LogCat (jumps to logging source code) 
 * 2. All logs can disabled with a single boolean flag 
 * 3. Log listener can be added - making it possible to add custom 
 * logic everywhere an error log is called.
 * 
 * @author iftah
 */
public class DLog {
	
	// level constants
	public enum LogLevel {
		VERBOSE, DEBUG, INFO, WARN, ERROR, WTF
	}
	
	static public boolean DebugMode = true;

	
	public interface LogListener {
		/***
		 * @param level - see level constants above
		 * @param debugMode
		 *            - true when the application has set DLog to DebugMode
		 * @param tag
		 * @param text
		 * @param callingInfo
		 *            - "(file:line)" - location of the calling code
		 */
		void logActivated(LogLevel level, boolean debugMode, String tag,
				String text, String callingInfo);
		
		void logActivated(LogLevel level, boolean debugMode, String tag,
				String text, String callingInfo, Throwable e);
	}

	private static ArrayList<LogListener> listeners = new ArrayList<LogListener>();

	private static String getCallingInfo() {
		return getCallingInfo(5);
	}

	private static String getCallingInfo(int upframes) {
		try {
			StackTraceElement stackTraceElement = Thread.currentThread()
					.getStackTrace()[upframes];
			String fullClassName = stackTraceElement.getClassName();
			String className = fullClassName.substring(fullClassName
					.lastIndexOf(".") + 1);
			// String methodName = stackTraceElement.getMethodName();
			int lineNumber = stackTraceElement.getLineNumber();

			return "(" + className + ".java:" + lineNumber + ")";
		} catch (Exception e) {
			return "";
		}
	}

	public static synchronized void registerLogListener(LogListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	public static synchronized void unregisterLogListener(LogListener listener) {
		listeners.remove(listener);
	}

	private static void notifyListeners(LogLevel level, boolean debugMode, String tag,
			String text, String callingInfo) {
		if (listeners != null) {
			for (LogListener ll : listeners)
				ll.logActivated(level, debugMode, tag,
						text, callingInfo);
		}
	}
	
	private static void notifyListeners(LogLevel level, boolean debugMode, String tag,
			String text, String callingInfo, Throwable e) {
		if (listeners != null) {
			for (LogListener ll : listeners)
				ll.logActivated(level, debugMode, tag,
						text, callingInfo, e);
		}
	}

	public static void d(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.DEBUG, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.d(tag, txt + callingInfo);
	}

	public static void d(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.DEBUG, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.d(tag, txt + callingInfo, e);
	}

	public static void e(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.ERROR, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.e(tag, txt + callingInfo);
	}

	public static void e(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.ERROR, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.e(tag, txt + callingInfo, e);
	}

	public static String getStackTraceString(Throwable e) {
		return android.util.Log.getStackTraceString(e);
	}

	public static void i(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.INFO, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.i(tag, txt + callingInfo);
	}

	public static void i(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.INFO, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.i(tag, txt + callingInfo, e);
	}

	public static boolean isLoggable(String tag, int lvl) {
		return DebugMode && android.util.Log.isLoggable(tag, lvl);
	}

	public static void println(int lvl, String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.INFO, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.println(lvl, tag, txt + callingInfo);
	}

	public static void v(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.VERBOSE, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.v(tag, txt + callingInfo);
	}

	public static void v(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.VERBOSE, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.v(tag, txt + callingInfo, e);
	}

	public static void w(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WARN, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.w(tag, txt + callingInfo);
	}

	public static void w(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WARN, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.w(tag, txt + callingInfo, e);
	}

	public static void w(String tag, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WARN, DebugMode, tag, "", callingInfo, e);
		if (DebugMode)
			android.util.Log.w(tag, e);
	}

	public static void wtf(String tag, String txt) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WTF, DebugMode, tag, txt, callingInfo);
		if (DebugMode)
			android.util.Log.wtf(tag, txt + callingInfo);
	}

	public static void wtf(String tag, String txt, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WTF, DebugMode, tag, txt, callingInfo, e);
		if (DebugMode)
			android.util.Log.wtf(tag, txt + callingInfo, e);
	}

	public static void wtf(String tag, Throwable e) {
		String callingInfo = getCallingInfo();
		notifyListeners(LogLevel.WTF, DebugMode, tag, "", callingInfo, e);
		if (DebugMode)
			android.util.Log.wtf(tag, e);
	}
}
