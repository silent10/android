package com.evature.android_demo;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.evature.util.DLog;

@ReportsCrashes(formKey = "dDk0dGxhc1B6Z05vaXh3Q0xxWnhnZlE6MQ")
public class DemoApplication extends Application {

	@Override
	public void onCreate() {

		//if (BuildConfig.DEBUG == false) { // Not when in debug mode!
			// The following line triggers the initialization of ACRA
			ACRA.init(this);
		//}
		DLog.DebugMode = true;

		super.onCreate();
	}
}