package com.virtual_hotel_agent.search.controllers.activities;

import android.app.Activity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.View;

import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.VHAApplication;

public class BaseActivity extends ActionBarActivity {
	
	@Override
	protected void onResume() {
	    super.onResume();
	    VHAApplication.setCurrentActivity(this);

	}

	@Override
	protected void onPause() {
	   clearReferences();
	   super.onPause();
	}

	@Override
	protected void onDestroy() {        
	   clearReferences();
	   super.onDestroy();
	}

	private void clearReferences(){
          Activity currActivity = VHAApplication.getCurrentActivity();
          if (currActivity != null && currActivity.equals(this))
        	  VHAApplication.setCurrentActivity(null);
	}
}
