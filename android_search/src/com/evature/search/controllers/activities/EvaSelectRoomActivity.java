package com.evature.search.controllers.activities;

import roboguice.activity.RoboFragmentActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.evature.search.R;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaRoomsUpdaterTask;
import com.evature.search.views.fragments.RoomsSelectFragement;

public class EvaSelectRoomActivity extends RoboFragmentActivity implements EvaDownloaderTaskInterface {
	
	public final static String HOTEL_INDEX= "hotel_index";

	RoomsSelectFragement mRoomSelectFragement;
	private int mHotelIndex;
	private EvaRoomsUpdaterTask mRoomUpdater;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		
		if(intent.getExtras()!=null)
		{
			setHotelIndex(intent.getExtras().getInt(HOTEL_INDEX));
		}
		
		setContentView(R.layout.eva_checkout_screen);
			
		if (savedInstanceState == null) 
		{
			mRoomUpdater = new EvaRoomsUpdaterTask(this, mHotelIndex);
			mRoomUpdater.attach(this);
			mRoomUpdater.execute();
	   }
	   
		
	}

	public int getHotelIndex() {
		return mHotelIndex;
	}

	public void setHotelIndex(int mHotelIndex) {
		this.mHotelIndex = mHotelIndex;
	}


	ProgressDialog mProgressDialog;

	@Override
	public void endProgressDialog(int id, String result) {
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
		}
		mRoomUpdater = null;
		
		// First-time init; create fragment to embed in activity.
		mRoomSelectFragement =  RoomsSelectFragement.newInstance(getHotelIndex());
		Fragment newFragment =(Fragment)mRoomSelectFragement;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.hotelList, newFragment);	
		ft.commit();

	}

	@Override
	public void startProgressDialog(int id) {
		if(mRoomUpdater!=null)
		{
			mProgressDialog = ProgressDialog.show(this,
					"Getting Room Availability", "Contacting search server", true,
					false);
		}
		
	}

	@Override
	public void endProgressDialogWithError(int id, String result) {
		mRoomUpdater = null;		

		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void updateProgress(int id, DownloaderStatus mProgress) {
	}

}
