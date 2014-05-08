package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.Log;
import com.viewpagerindicator.TitlePageIndicator;
import com.virtual_hotel_agent.search.controllers.tutorial.BaseTutorial.TutorialStatus;

public class TutorialController {

	private static final String TAG = "TutorialController";
	
	
	public static final BaseTutorial NO_TUTORIAL = new BaseTutorial("<null>"); 
	
	public static BaseTutorial currentTutorial = NO_TUTORIAL;
	

	
	/***
	 * Tutorial #1 -  Urge the user to start recording
	 * ------------
	 *  	 Activated:  app onCreate
	 *   Unlocked when:  always
	 *  Completed when:  tapped microphone
	 *  
	 * @param activity
	 * @param pager
	 * @param tabs
	 */
	public static void showRecordButtonTutorial(Activity activity, final ViewPager pager, final TitlePageIndicator tabs) {
		if (currentTutorial != NO_TUTORIAL) {
			Log.w(TAG, "Unexpected showRecordButtonTutorial when another tutorial is running: "+currentTutorial.name);
		}
		RecordButtonTutorial tutorial = new RecordButtonTutorial();
		TutorialStatus status = tutorial.getStatus();
		if (status == TutorialStatus.Played) {
			// this was already played
			Toast.makeText(activity, "This tutorial was already played - replaying", Toast.LENGTH_LONG).show();
			tutorial.setStatus(TutorialStatus.Unlocked);
			//return;  TODO: remove above two lines and restore the 'return'
		}
		currentTutorial  = tutorial;
		tutorial.start(activity, pager, tabs);
	}
	
	
	
	public static void onMicrophonePressed(Activity activity) {
		currentTutorial.onMicrphonePressed(activity);
	}
	
	public static void onEvaReply(Activity activity, EvaApiReply reply) {
		currentTutorial.onEvaReply(activity, reply);
	}

	/***
	 * @return true if back button was handled
	 */
	public static boolean onBackPressed(Activity activity) {
		return currentTutorial.onBackPressed(activity);
	}

	public static void canceledRecording(Activity activity) {
		currentTutorial.canceledRecording(activity);
	}
}
