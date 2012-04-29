package com.softskills.evasearch;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.softskills.components.S3Activity;
import com.softskills.components.S3LocationUpdater;

public class EvaSplash extends Activity implements EvaDownloaderTaskInterface{
		
	HotelListDownloaderTask mDownloader;
	ProgressBar mProgressBar;
	TextView mTextViewProgress;
	
	Handler mLocationHandler =  new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mDownloader = new HotelListDownloaderTask(EvaSplash.this, "tonight",
					EvaSettingsAPI.getCurrencyCode(EvaSplash.this));
			
			mTextViewProgress.setText(R.string.asking_eva);
			
			mDownloader.execute();
	        
			mDownloader.attach(EvaSplash.this);
			
			super.handleMessage(msg);
		}};
	
	Handler mAppStarter = 	new Handler(){

		@Override
		public void handleMessage(Message msg) {
			Resources res = getResources();
	         
	         Intent intent = new Intent();
	                          
	         ComponentName component = new ComponentName("com.softskills.evasearch",EvaSearchMainScreen.class.getName());
	                          
	         intent.setComponent(component);
	         startActivity(intent);
	         
	         finish();
	         
	         super.handleMessage(msg);
		}};
		
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		S3Activity.APP_INDICATOR = true;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		   
        setContentView(R.layout.splash_portrait);
        
        mTextViewProgress = (TextView)findViewById(R.id.textViewSplashProgress);
		
		mTextViewProgress.setText(R.string.determining_location);
		
        S3LocationUpdater.initContext(this.getApplicationContext());
       
        mProgressBar = (ProgressBar)findViewById(R.id.progressBarSplashCurrentLocation);
      
		super.onCreate(savedInstanceState);
	}

	@Override
	public void endProgressDialog() {
		mProgressBar.setVisibility(View.GONE);
		
		mAppStarter.sendEmptyMessage(0);
	}

	@Override
	public void endProgressDialogWithError() {
		
		Toast.makeText(this, "Unable to determine current location", 3000).show();
		
		endProgressDialog();
		
	}

	@Override
	public void startProgressDialog() {
		mProgressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public void updateProgress(int mProgress) {
		
		String message="...";
		
		switch(mProgress)
		{
		case EvaDownloaderTaskInterface.PROGRESS_CREATE_HOTEL_DATA:
			message = getResources().getString(R.string.creating_objects);
			break;
		case EvaDownloaderTaskInterface.PROGRESS_EXPEDIA_HOTEL_FETCH:
			message = getResources().getString(R.string.contacting_expedia_server);
			break;
		case EvaDownloaderTaskInterface.PROGRESS_FINISH:
			message = getResources().getString(R.string.obtained_hotel_list);
			break;
		case EvaDownloaderTaskInterface.PROGRESS_FINISH_WITH_ERROR:
			message = getResources().getString(R.string.network_error);
			break;
		}
		
		mTextViewProgress.setText(message);
	}
	
	@Override
	protected void onPause() {
		try
		{
			S3LocationUpdater location = S3LocationUpdater.getInstance();
			location.stopGPS();
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}
		super.onPause();
	}
	
	boolean mFirstTime = true;
	
	@Override
	protected void onResume() {
		S3LocationUpdater.initContext(this.getApplicationContext());
		try
		{
			S3LocationUpdater location = S3LocationUpdater.getInstance();
			location.startGPS();
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}
		if(mFirstTime)
		{
			mFirstTime = false;
		
			//mAppStarter.sendEmptyMessage(0);
			mLocationHandler.sendEmptyMessageDelayed(0, 500);
		}
		super.onResume();
	}

	@Override
    public void onDestroy()
    {
        super.onDestroy();
/*
        View backgroundLayout = findViewById(R.id.splashBackgroundLayout);
        BitmapDrawable bd = (BitmapDrawable)backgroundLayout.getBackground();
        Bitmap bmp = bd.getBitmap();
        bmp.recycle();
        System.gc();*/
            }

	
}
