package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.anim.AnimationUtils;
import com.espian.showcaseview.anim.AnimationUtils.AnimationEndListener;
import com.espian.showcaseview.targets.ViewTarget;
import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.flow.FlowElement;
import com.evaapis.crossplatform.flow.FlowElement.TypeEnum;
import com.nineoldandroids.view.ViewHelper;
import com.viewpagerindicator.TitlePageIndicator;
import com.virtual_hotel_agent.search.R;

public class RecordButtonTutorial extends BaseTutorial {

	public static final String NAME = "recordButtonTutorial";
	public static final String questionAskedTutorial = "questionAskedTutorial";
	public static final String hotelResultsTutorial = "hotelResultsTutorial";

	enum State {
		NotStarted,
		WaitingForMicrophoneClick,
		WaitingForRecordingEnd
	}
	
	private State state;
	
	public RecordButtonTutorial() {
		super(NAME);
		state = State.NotStarted;
	}


	public void start(Activity activity, final ViewPager pager, final TitlePageIndicator tabs) {
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
        
        showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
			
			@Override
			public void onShowcaseViewShow(ShowcaseView showcaseView) {
				AlphaAnimation anim = new AlphaAnimation(1f, 0.1f);
				anim.setDuration(500);
				anim.setRepeatCount(0);
				anim.setFillAfter(true);   
				tabs.setAnimation(anim);
				pager.startAnimation(anim);
				ViewHelper.setAlpha(hand, 1f);
			}
			
			@Override
			public void onShowcaseViewHide(ShowcaseView showcaseView) {
				AlphaAnimation anim = new AlphaAnimation(0.1f, 1f);
				anim.setFillAfter(true);
				anim.setRepeatCount(0);
				anim.setDuration(200);
				tabs.setAnimation(anim);
				pager.startAnimation(anim);
			}
			
			@Override
			public void onShowcaseViewDidHide(ShowcaseView _showcaseView) {
				((ViewGroup) showcaseView.getParent()).removeView(showcaseView);
				showcaseView = null;
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
			showcaseView.hide();
			state = State.NotStarted;
			setStatus(TutorialStatus.Played);
		}
	}

	@Override
	public void canceledRecording(Activity activity) {
		if (state == State.WaitingForRecordingEnd) {
			if (showcaseView != null) {
				showcaseView.setText("Canceled :(", 
						"Press 'back' again if you want to cancel the tutorial,\n"
						+ "or try again by tapping the microphone button again and say a search query such as 'Hotel in Las Vegas'");
			}
			state = State.WaitingForMicrophoneClick;	
		}
	}
}
