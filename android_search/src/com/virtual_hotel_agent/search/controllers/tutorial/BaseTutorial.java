package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.espian.showcaseview.ShowcaseView;
import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;

public class BaseTutorial {
	private static final String TAG = "BaseTutorial";
	
	enum TutorialStatus {
		Locked,      // prerequisites not met yet
		Unlocked,    // prerequisites met, tutorial not played yet
		PlayStarted, // tutorial is playing at this time
		Played		 // tutorial was played
	}

	TutorialStatus  status;
	String name;
	ShowcaseView showcaseView;
	
	public BaseTutorial(String name) {
		this.name = name;
	}
	

	public TutorialStatus getStatus() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(VHAApplication.getAppContext());

		String statusStr = sp.getString(name, TutorialStatus.Locked.toString());
		try {
			status = TutorialStatus.valueOf(statusStr);
			return status;
		}
		catch(Exception e) {
			Log.w(TAG, "Failed to convert status str for tutorial "+name+" to enum. value is: "+statusStr);
			status = TutorialStatus.Locked;
			return status;
		}
	}
	
	public void setStatus(TutorialStatus status) {
		this.status = status;
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(VHAApplication.getAppContext());
		Editor edit = sp.edit();
		edit.putString(name, status.toString());
		edit.commit();
		if (status == TutorialStatus.Played) {
			TutorialController.currentTutorial = TutorialController.NO_TUTORIAL;
		}
	}
	

	public void cancel(Activity activity) {
		if (status == TutorialStatus.PlayStarted)
			setStatus(TutorialStatus.Unlocked);
		if (showcaseView != null)
			showcaseView.hide();
		Toast.makeText(activity, "You can replay this tutorial again later by choosing 'Tutorial' from the menu", Toast.LENGTH_LONG).show();
	}
	
	// events - to be overridden by implementations
	public void onMicrphonePressed(Activity activity) {}
	public void onEvaReply(Activity activity, EvaApiReply reply) {}
	public void canceledRecording(Activity activity) {}
	
	public boolean onBackPressed(Activity activity) { 
		if (TutorialController.currentTutorial != TutorialController.NO_TUTORIAL) {
			TutorialController.currentTutorial.cancel(activity);
			TutorialController.currentTutorial = TutorialController.NO_TUTORIAL;
			return true;
		}
		return false;
	}




}
