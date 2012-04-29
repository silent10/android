package com.softskills.components;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;

abstract public class S3Activity extends Activity {
	public static boolean APP_INDICATOR = false;

	protected abstract void onCreated(Bundle savedInstanceState);
	
	
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		Window window = getWindow(); // Eliminates color banding
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState){
		if (APP_INDICATOR){
			super.onCreate(savedInstanceState);
			onCreated(savedInstanceState);
		}else{
			super.onCreate(null);
			finish();			
		}		
	}
}
