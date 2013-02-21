package com.evature.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class EvaCheckoutActivity extends FragmentActivity{
	
	public final static String HOTEL_INDEX= "hotel_index";

	private static final String ACTIVITY_STATE = "ActivityState";
	
	CalendarFragment mCalendarFragment = null;
	
	int mHotelIndex;
	
	private final int ACTIVITY_STATE_CALENDAR = 1;
	private final int ACTIVITY_STATE_ROOM_SELECTION = 2;
	
	int mActivityState = ACTIVITY_STATE_CALENDAR;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		
		if(intent.getExtras()!=null)
		{
			mHotelIndex = intent.getExtras().getInt(HOTEL_INDEX);
		}
		
		setContentView(R.layout.eva_checkout_screen);
			
		if (savedInstanceState == null) 
		{
			// First-time init; create fragment to embed in activity.
			mCalendarFragment =  CalendarFragment.newInstance(this);
			Fragment newFragment =(Fragment)mCalendarFragment;
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.hotelList, newFragment);	
			ft.commit();
	   }
	   
		
	}

	RoomsSelectFragement mRoomSelectFragement;
	
	public void selectRoom() {
	 
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		
		if(mRoomSelectFragement!=null)
		{
			ft.remove(mRoomSelectFragement);
		}
		
		mRoomSelectFragement =  RoomsSelectFragement.newInstance(this,mHotelIndex);
		
		ft.replace(R.id.hotelList, mRoomSelectFragement);	
//		ft.addToBackStack(null);
		ft.commit();		
		mActivityState = ACTIVITY_STATE_ROOM_SELECTION;
	}

}
