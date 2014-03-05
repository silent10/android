package com.virtual_hotel_agent.search.controllers.activities;

import org.json.JSONObject;

import roboguice.activity.RoboFragmentActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface;
import com.virtual_hotel_agent.search.controllers.web_services.RoomsUpdaterTask;
import com.virtual_hotel_agent.search.views.fragments.RoomsSelectFragement;

public class SelectRoomActivity extends RoboFragmentActivity implements DownloaderTaskListenerInterface {
	
	public final static String HOTEL_INDEX= "hotel_index";

	RoomsSelectFragement mRoomSelectFragement;
	private int mHotelIndex;
	private RoomsUpdaterTask mRoomUpdater;
	@Inject Injector injector;
	
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
			mRoomUpdater = new RoomsUpdaterTask(this, mHotelIndex);
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
	public void endProgressDialog(int id, JSONObject result) {
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
		}
		mRoomUpdater = null;
		
		// First-time init; create fragment to embed in activity.
		mRoomSelectFragement = injector.getInstance(RoomsSelectFragement.class);
							// RoomsSelectFragement.newInstance(getHotelIndex());
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
	public void endProgressDialogWithError(int id, JSONObject result) {
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
