package com.virtual_hotel_agent.search.controllers.tutorial;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.targets.ViewTarget;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.views.MainView;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;

public class ChatTutorial extends BaseTutorial {

	public static final String NAME = "chatTutorial";
	private static final String TAG = NAME;

	enum State {
		NotStarted,
		ShowMyChatItem,
		ShowEvaChatItem,
		ShowNewSession,
		Done
	}
	
	private State state;
	private ChatItem evaChatItem;
	private View evaChatView;
	
	public ChatTutorial() {
		super(NAME);
		state = State.NotStarted;
	}
	
	
	
	private void showEvaChatItem(ChatFragment chatFragment) {
		
		state = State.ShowEvaChatItem;
		if (evaChatItem != null) {
			showcaseView.setText("Virtual Agent Response", 
					"The red bordered balloons coming from the right side of the screen is the response of the Virtual Hotel Agent");
			
			chatFragment.fadeOutOtherChat(evaChatItem);
			TextView label = (TextView)evaChatView.findViewById(R.id.label);
			ViewTarget target = new ViewTarget(label);
			showcaseView.setShowcase(target, true);
			if (showcaseView.getVisibility() == View.GONE) {
				showcaseView.show();
			}
			evaChatItem = null;
			evaChatView = null;
			state = State.ShowNewSession;
		}
	}
	
	private void showClearSession(ChatFragment chatFragment) {
		MainView mainView = TutorialController.mainView;
		mainView.fadeInView(false, false, true);
		mainView.fadeOutView(true, true, false);
		showcaseView.setText("New Session", 
				"Tapping on this button will start a new session.");
		
		ViewTarget target = new ViewTarget(mainView.startNewSessionButton);
		showcaseView.setShowcase(target, true);
		if (showcaseView.getVisibility() == View.GONE) {
			showcaseView.show();
		}
		state = State.Done;
	}
	
	private void onClickShowcase(ChatFragment chatFragment) {
		switch (state) {
		case Done:
			showcaseView.hide();
			setStatus(TutorialStatus.Played);
			TutorialController.currentTutorial = TutorialController.NO_TUTORIAL;
			break;
		case ShowMyChatItem:
		case ShowEvaChatItem:
			showEvaChatItem(chatFragment);
			break;
		case ShowNewSession:
			showClearSession(chatFragment);
			break;
		}
	}

	
	@Override
	public void onAddChatItem(final ChatItem chatItem, View chatView, final ChatFragment chatFragment) {
		Log.i(TAG, "onAddChatItem: "+chatItem);
		if (state == State.NotStarted || state == State.Done) {
			// starting chat tutorial
			// temp for dev: allow state Done
			setStatus(TutorialStatus.PlayStarted);
			state = State.ShowMyChatItem;
		}
		
		if (chatItem.getType() == ChatType.VirtualAgent || chatItem.getType() == ChatType.DialogQuestion) {
			// save this chatItem for later
			evaChatItem = chatItem;
			evaChatView = chatView;
			if (state == State.ShowEvaChatItem) {
				// already was clicked
				showEvaChatItem(chatFragment);
			}
		}

		
		if (state == State.ShowMyChatItem && chatItem.getType() == ChatType.Me) {
			if (chatItem.getChat().toString().equals(ChatItem.START_NEW_SESSION)) {
				return;
			}
//				View chatView = chatFragment.getViewForChatItem(chatItem);
			if (chatView == null) {
				Log.w(TAG, "no view for chat item "+chatItem);
				state = State.NotStarted;
				setStatus(TutorialStatus.Unlocked);
				return;
			}
			TextView label = (TextView)chatView.findViewById(R.id.label);
			if (label.getText().toString().equals(chatItem.getChat().toString()) == false) {
				Log.w(TAG, "unexpected mismatch chatItem: "+chatItem.getChat()+"  and label: "+label.getText());
				return;
			}
			
			ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
	        co.block = true;
	        co.noButton = true;
	        co.scaleMultiplier = 0.75f;
	        co.textPadding = 4;
	        ViewTarget target = new ViewTarget(label);
	        
	        TutorialController.mainView.fadeOutView(true, false, true);
	        chatFragment.fadeOutOtherChat(chatItem);
	        
	        if (showcaseView != null) {
	        	showcaseView.hide();
	        	showcaseView = null;
	        }
	        
			showcaseView = ShowcaseView.insertShowcaseView(target, chatFragment.getActivity(), 
					"Your input", 
					"The blue bordered balloons coming from the left side of the screen are your inputs.\n\nTap anywhere to continue.", co);

			showcaseView.setOnShowcaseEventListener(new OnShowcaseEventListener() {
				
				@Override
				public void onShowcaseViewShow(ShowcaseView showcaseView) {
				}
				
				@Override
				public void onShowcaseViewHide(ShowcaseView showcaseView) {
					TutorialController.mainView.fadeInView(true, true, true);
					chatFragment.fadeInAll();
				}
				
				@Override
				public void onShowcaseViewDidHide(ShowcaseView _showcaseView) {
					((ViewGroup) showcaseView.getParent()).removeView(showcaseView);
					showcaseView = null;
				}
			});
			
			showcaseView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					ChatTutorial.this.onClickShowcase(chatFragment);
				}
			});
			
			showcaseView.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_UP) {
						ChatTutorial.this.onClickShowcase(chatFragment);
						return true;
					}
					return false;
				}
			});
			
			showcaseView.show();
		}
	}

	
	@Override public void cancel(Activity activity) {
		state = State.NotStarted;
		super.cancel(activity);
	};

	public boolean shouldStart(ChatItem chatItem, View chatView, Activity activity) {
		if (state != State.NotStarted && state != State.Done) {
			// only start if not started...
			// temp: allow restart (allow state Done)
			return false;
		}
		if (chatItem.getType() != ChatType.Me) {
			if (chatItem.getType() == ChatType.VirtualAgent || chatItem.getType() == ChatType.DialogQuestion) {
				// save this chatItem for later
				evaChatItem = chatItem;
				evaChatView = chatView;
			}
			// start only on "Me" chat
			return false;
		}
		if (chatItem.getChat().toString().equals(ChatItem.START_NEW_SESSION)) {
			// start new session isn't actually something the user said/typed - not good enough to trigger the ChatTutorial
			return false;
		}
			
		if (TutorialController.recordButtonTutorial.getStatus() != TutorialStatus.Played) {
			// don't start this one until the previous one is complete 
			return false;
		}
		
		if (getStatus() == TutorialStatus.Played) {
			// this was already played
			Toast.makeText(activity, "This tutorial was already played - replaying", Toast.LENGTH_LONG).show();
			//return false;  TODO: remove above line and restore the 'return'
		}

		return true;
	}
}
