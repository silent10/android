package com.evature.search.controllers.activities;

import roboguice.activity.RoboFragmentActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.evature.search.R;
import com.evature.search.controllers.web_services.HotelListDownloaderTask;
import com.evature.search.views.fragments.CalendarFragment;
import com.evature.search.views.fragments.RoomsSelectFragement;

public class EvaGuestCountActivity extends RoboFragmentActivity{
	
	public final static String HOTEL_INDEX= "hotel_index";

	RoomsSelectFragement mRoomSelectFragement;
	private int mHotelIndex;
	
	
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
//			// First-time init; create fragment to embed in activity.
//			EvaDownloaderTask postGuestsDownloader = new HotelListDownloaderTask (
//					ChildAgeFragment.this.getActivity(),
//					mEvaCheckoutActivity.getHotelIndex());
//
//			mRoomSelectFragement =  ChildAgeFragment.newInstance(postGuestsDownloader);
			Fragment newFragment =(Fragment)mRoomSelectFragement;
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.hotelList, newFragment);	
			ft.commit();
	   }
	   
		
	}

	public int getHotelIndex() {
		return mHotelIndex;
	}

	public void setHotelIndex(int mHotelIndex) {
		this.mHotelIndex = mHotelIndex;
	}

}
