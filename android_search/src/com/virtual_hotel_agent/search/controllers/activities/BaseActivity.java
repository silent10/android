package com.virtual_hotel_agent.search.controllers.activities;

import com.virtual_hotel_agent.search.VHAApplication;

import android.app.Activity;

public class BaseActivity extends Activity {
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
