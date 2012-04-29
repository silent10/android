package com.softskills.evasearch;

import java.util.Locale;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.softskills.components.S3FragmentActivity;

public class EvaSearchMainScreen extends S3FragmentActivity implements EvaDownloaderTaskInterface, OnInitListener{

	@Override
	public void onBackPressed() {
		mDownloadedHotelIndex = -1;
		super.onBackPressed();
	}


	private static final String HOTEL_INDEX = "hotelIndex";
	private static final int DATA_CHECK_CODE = 1;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(HOTEL_INDEX, mDownloadedHotelIndex);
		super.onSaveInstanceState(outState);
	}


	@Override
	protected void onDestroy() {
		if(mHotelDownloader!=null)
		{
			mHotelDownloader.detach();
		}
		
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
		}
		super.onDestroy();
	}
	

	HotelListFragment mHotelListFragment;
	ShowHotelFragment mHotelFragment;
	
	static EvaHotelDownloaderTask mHotelDownloader = null;

	@Override
	protected void onCreated(Bundle savedInstanceState) {
		
		setContentView(R.layout.eva_main_small_screen);

		if (savedInstanceState == null) {
			// First-time init; create fragment to embed in activity.
			mHotelListFragment =  HotelListFragment.newInstance();
			Fragment newFragment =(Fragment)mHotelListFragment;
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.hotelList, newFragment);
			ft.commit();
		}	 
		else
		{
			if(mHotelDownloader!=null)
			{
				mHotelDownloader.attach(this);
				mProgressDialog = ProgressDialog.show(this,
						"Getting Hotel Information", "Contacting search server", true,
						false); 
			}
			else
			{
			/*	mDownloadedHotelIndex = savedInstanceState.getInt(HOTEL_INDEX);
				if(mDownloadedHotelIndex!=-1)
				{
					mHotelFragment = ShowHotelFragment.newInstance(mDownloadedHotelIndex);
					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();				
					ft.add(R.id.hotelList, mHotelFragment);
					ft.addToBackStack(null);
					ft.commit(); 					
				}*/
			}
		}
		
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent,DATA_CHECK_CODE);
	}

	private TextToSpeech mTts;
	protected void onActivityResult(
	        int requestCode, int resultCode, Intent data) {
	    if (requestCode == DATA_CHECK_CODE) {
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
	            // success, create the TTS instance
	            mTts = new TextToSpeech(this, this);
	        } else {
	            // missing data, install it
	            Intent installIntent = new Intent();
	            installIntent.setAction(
	                TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
	            startActivity(installIntent);
	        }
	        
	        if(mTts!=null)
	        {
	        	mTts.setLanguage(Locale.US);
	        }

	    }
	}	
		
/*	
	private Handler mMessageLoop = new Handler()
	{
		@Override
		public void handleMessage(Message msg) {

			switch(msg.what)
			{
			case HOTEL_INFO_DOWNLOADED:
				String hotelInfo = (String)msg.obj;
				int hotelIndex = msg.arg1;

				Fragment hotelInfoFragment = ShowHotelFragment.newInstance(hotelInfo, hotelIndex);

				FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.add(R.id.hotelList, hotelInfoFragment);
				ft.commit();
				break;
			}		
		}
	};
*/
	
	int mDownloadedHotelIndex = -1;
	private ProgressDialog mProgressDialog = null;

	

		public void showHotelDetails(int hotelIndex)
		{
			if(EvaSearchApplication.getDb()==null) 
			{
				return;
			}
			
			if(mHotelDownloader!=null)
			{
				if(false==mHotelDownloader.cancel(true))
				{
					return;
				}
			}
			
			mHotelDownloader = new EvaHotelDownloaderTask(this, hotelIndex);
			
			mHotelDownloader.execute();
		}


		@Override
		public void endProgressDialog() {
			
			if(mProgressDialog!=null)
			{
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
						
			mDownloadedHotelIndex = mHotelDownloader.getHotelIndex();
			
			mHotelFragment = ShowHotelFragment.newInstance(mDownloadedHotelIndex);
					
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();				
			ft.replace(R.id.hotelList, mHotelFragment);
			ft.addToBackStack(null);
			ft.commit();
						
			mHotelDownloader = null;
		}


		@Override
		public void startProgressDialog() {
			if(mHotelDownloader!=null)
			{
				mProgressDialog = ProgressDialog.show(this,
					"Getting Hotel Information", "Contacting search server", true,
					false);
			}			
		}


		@Override
		public void endProgressDialogWithError() {
			mDownloadedHotelIndex = -1;
			mHotelDownloader = null;		
			
			if(mProgressDialog!=null)
			{
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
			
		}


		@Override
		public void updateProgress(int mProgress) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onInit(int status) {
			// TODO Auto-generated method stub
			
		};

		void speak(String phrase)
		{
			mTts.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
		}

}


