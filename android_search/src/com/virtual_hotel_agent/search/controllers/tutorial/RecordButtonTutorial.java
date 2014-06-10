package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.anim.AnimationUtils;
import com.espian.showcaseview.anim.AnimationUtils.AnimationEndListener;
import com.espian.showcaseview.targets.ViewTarget;
import com.evaapis.crossplatform.EvaApiReply;
import com.evature.util.Log;
import com.nineoldandroids.view.ViewHelper;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.tutorial.ChatTutorial.State;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.views.MainView;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;

public class RecordButtonTutorial extends BaseTutorial {

	public static final String NAME = "recordButtonTutorial";
	private static final String TAG = NAME;
//	public static final String questionAskedTutorial = "questionAskedTutorial";
//	public static final String hotelResultsTutorial = "hotelResultsTutorial";

	enum State {
		NotStarted,
		WaitingForMicrophoneClick,
		WaitingForRecordingEnd, 
		WaitingForMyChatItem
	}
	
	private State state;
	
	public RecordButtonTutorial() {
		super(NAME);
		state = State.NotStarted;
	}


	public void start(Activity activity) {
		setStatus(TutorialStatus.PlayStarted);
		
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.block = true;
        co.noButton = true;
        ViewTarget target = new ViewTarget(R.id.search_button, activity);
        
		showcaseView = ShowcaseView.insertShowcaseView(target, activity, 
				"Try the Record Button", 
				"Tap the microphone button to start recording an hotel search query. \n\n"
				+ "Go ahead, try tapping the microphone and then say 'Hotel in New York'", co);

		final View hand = showcaseView.getHand();
		hand.setPivotX(0);
		hand.setPivotY(0);
		hand.setRotation(-45);
		final int y = target.getPoint().y;
		final int x = target.getPoint().x;
		
		state = State.WaitingForMicrophoneClick;
		
        final AnimationEndListener handAnimEnded = new AnimationEndListener() {
		    @Override
		    public void onAnimationEnd() {
		    	if (state == State.WaitingForMicrophoneClick) {
		    		
		    		final AnimationEndListener that = this;
		            Handler handler = new Handler();
		            Runnable runnable = new Runnable() {
		                @Override
		                public void run() {
		                	if (state == State.WaitingForMicrophoneClick) {
			                	AnimationUtils.createMovementAnimation(hand, 0, 0,
					                    x+10, y-20, x+14, y+5,
					                    that
					    		).start();
		                	}
		                	else {
		                		if (showcaseView != null) {
		                			showcaseView.removeView(hand);
		                		}
		                	}
		                }
		            };
		            handler.postDelayed(runnable, 3000);
		    	}
		    	else {
		    		if (showcaseView != null) {
		    			showcaseView.removeView(hand);
		    		}
		    	}
		    }
		};
		
		AnimationUtils.createMovementAnimation(hand, 0, 0,
                x+10, y-20, x+14, y+5,
                handAnimEnded).start();
        
		showcaseView.setScaleMultiplier(0.95f);
        showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				TutorialController.mainView.fadeOutView(true, true, false);
				ViewHelper.setAlpha(hand, 1f);
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				TutorialController.mainView.fadeInView(true, true, false);
				
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView _showcaseView) {
				if (_showcaseView.getParent() != null) {
					((ViewGroup) _showcaseView.getParent()).removeView(_showcaseView);
				}
				if (showcaseView == _showcaseView) {
					showcaseView = null;
				}
			}
		});
        
        showcaseView.show();
	}

	@Override
	public void onMicrphonePressed(Activity activity) {
		// TODO: different text for started and stopped clicks
		if (showcaseView != null) {
			if (state == State.WaitingForMicrophoneClick) {
				showcaseView.setText("Say an hotel search query", "Excellent! now that the app is recording "
						+ "say a search query such as 'Hotel in New York'.\n\n"
						+ "You can cancel the recording by pressing 'back' or force stop the recording by "
						+ "pressing the microphone button again.");
				
				state = State.WaitingForRecordingEnd;
			}
			else {
				showcaseView.setText("Searching...", "Excellent!");
			}
		}
	}
	
	@Override
	public void onEvaReply(Activity activity, EvaApiReply reply) {
		if (state == State.WaitingForRecordingEnd) {
			// TODO: check if Eva parsed correctly
			state = State.WaitingForMyChatItem;
			showcaseView.hide();
			setStatus(TutorialStatus.Played);
		}
	}

	@Override
	public void canceledRecording(Activity activity) {
		if (state == State.WaitingForRecordingEnd) {
			if (showcaseView != null) {
				showcaseView.setText("Canceled :(", 
						"Press 'back' again if you want to cancel the tutorial,\n"
						+ "or try again: Tap the microphone button and say a search query \nsuch as 'Hotel in Las Vegas'");
			}
			state = State.WaitingForMicrophoneClick;	
		}
	}
	
	@Override public void cancel(Activity activity) {
		state = State.NotStarted;
		super.cancel(activity);
	};
}
