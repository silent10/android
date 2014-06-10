package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Toast;

import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.controllers.tutorial.BaseTutorial.TutorialStatus;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.views.MainView;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;

public class TutorialController {

	private static final String TAG = "TutorialController";
	
	
	public static final BaseTutorial NO_TUTORIAL = new BaseTutorial("<null>"); 
	
	public static BaseTutorial currentTutorial = NO_TUTORIAL;
	
	static RecordButtonTutorial recordButtonTutorial = new RecordButtonTutorial();
	static ChatTutorial chatTutorial = new ChatTutorial();
	static QuestionTutorial questionTutorial = new QuestionTutorial();
	
	public static MainView mainView;

	
	
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
	public static void showRecordButtonTutorial(Activity activity) {
		if (currentTutorial != NO_TUTORIAL) {
			Log.w(TAG, "Unexpected showRecordButtonTutorial when another tutorial is running: "+currentTutorial.name);
			currentTutorial.cancel(activity);
		}
		RecordButtonTutorial tutorial = recordButtonTutorial;
		TutorialStatus status = tutorial.getStatus();
		if (status == TutorialStatus.Played) {
			// this was already played
//			Toast.makeText(activity, "This tutorial was already played - replaying", Toast.LENGTH_LONG).show();
//			tutorial.setStatus(TutorialStatus.Unlocked);
			return; // TODO: remove above two lines and restore the 'return'
		}
		currentTutorial  = tutorial;
		tutorial.start(activity);
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

	public static void onAddChatItem(ChatItem chatItem, View row, ChatFragment chatFragment) {
//		if (currentTutorial == NO_TUTORIAL) {
//			FragmentActivity activity = chatFragment.getActivity();
//			if (chatTutorial.shouldStart(chatItem, row, activity)) {
//				currentTutorial = chatTutorial;
//			}
//			else if (questionTutorial.shouldStart(chatItem, row, activity)) {
//				currentTutorial = questionTutorial;
//			}
//		}
		
		currentTutorial.onAddChatItem(chatItem, row, chatFragment);
	}
}
