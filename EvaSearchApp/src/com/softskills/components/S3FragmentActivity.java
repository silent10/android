package com.softskills.components;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;

abstract public class S3FragmentActivity extends FragmentActivity {
	protected abstract void onCreated(Bundle savedInstanceState);
	
	@Override
	public void onAttachedToWindow(){
		super.onAttachedToWindow();
		Window window = getWindow(); // Eliminates color banding
		window.setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected final void onCreate(Bundle savedInstanceState)
	{
		if (S3Activity.APP_INDICATOR){
			super.onCreate(savedInstanceState);
			onCreated(savedInstanceState);			
		}else{
			super.onCreate(null);
			finish();			
		}		
	}	
}
