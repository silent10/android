package com.virtual_hotel_agent.search.views.fragments;

import java.util.ArrayList;
import java.util.Random;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.evature.util.Log;
import com.google.inject.Inject;
import com.nhaarman.listviewanimations.appearance.OnAnimEndCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.virtual_hotel_agent.search.BuildConfig;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.events.ChatItemModified;
import com.virtual_hotel_agent.search.controllers.events.ToggleMainButtonsEvent;
import com.virtual_hotel_agent.search.controllers.tutorial.TutorialController;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.views.adapters.ChatAdapter;
import com.virtual_hotel_agent.search.views.adapters.ChatAnimAdapter;

public class ChatFragment extends RoboFragment implements OnItemClickListener {
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.mainmenu, menu);
//	}

	@Inject protected EventManager eventManager;
	@Inject private ChatItemList mChatListModel;
	
	private ChatAdapter mChatAdapter;

	private ChatAnimAdapter mAnimAdapter;
	
	private int editedChatItemIndex = -1;

	private ViewGroup root;

	private ListView mChatListView;
	static final String TAG = "ChatFragment";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		if (root != null) {
			Log.w(TAG, "Fragment initialized twice");
			((ViewGroup) root.getParent()).removeView(root);
			return root;
		}
		
		root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		mChatListView = (ListView) root.findViewById(R.id.chat_list);
		
		// Connect the data of the chat history to the view:
		mChatAdapter = new ChatAdapter(this, R.layout.row_vha_chat, R.id.label, mChatListModel);

		mAnimAdapter = new ChatAnimAdapter(mChatAdapter, 0, 300, new MyOnDismissCallback(), new MyOnAnimEndCallback());
		mAnimAdapter.setAbsListView(mChatListView);
		mChatListView.setAdapter(mAnimAdapter);
        

        	        
//		chatListView.setAdapter(mChatAdapter);
		
		mChatListView.setOnItemClickListener(this);
		
		
		return root;
	}
	
	@Override 
	public void onActivityCreated(Bundle savedInstanceState) {
		Log.d(TAG, "ChatFragment onActivityCreated");
		super.onActivityCreated(savedInstanceState);
		mChatListModel.loadInstanceState(savedInstanceState);
		if (mChatListModel.size() == 0) {
			showIntro();
		}
	};
	
	
	private class MyOnDismissCallback implements OnDismissCallback {

		@Override
		public void onDismiss(ViewGroup listView, int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) {
            	mChatAdapter.remove(position);
            }
		}
    }
	
	private class MyOnAnimEndCallback implements OnAnimEndCallback {

		@Override
		public void onAnimEnd(View row) {
			ChatItem chatItem = (ChatItem) row.getTag();
			if (chatItem == null) {
				Log.w(TAG, "unexpected null chatItem");
			}
			else {
				TutorialController.onAddChatItem(chatItem, row, ChatFragment.this);
			}
		}
	}

	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(instanceState);
		mChatListModel.saveInstanceState(instanceState);
	}
	
	
	public void addChatItem(ChatItem chatItem) {
		if (editedChatItemIndex != -1 && chatItem.getType() == ChatType.Me) {
			// adding a "me" chat - close the editted me-chat
			closeEditChatItem(false);
		}
		if (mChatAdapter != null) {
			mChatAdapter.add(chatItem);
		}
		else if (mChatListModel != null) {
			mChatListModel.add(chatItem);
		}
	}
	
	public void invalidate() { 
		if (mChatAdapter != null)
			mChatAdapter.notifyDataSetChanged();
	}
	
	private boolean shownExamples = false;
	private void showExamples() {
		if (shownExamples) {
			return;
		}
		shownExamples = true;
		String[] examples = {
				"  Hotel tonight", 
				"  Hotel in Paris, Monday to Friday", 
				"  Hotel near the Eiffel tower",
				"  3 star hotel near the Eiffel tower",
				"  Sort by price",
				"  New York tonight, for less than 150",
				"  A Hilton hotel in Miami Florida"
			};
		String greeting = getResources().getString(R.string.examples_greetings);
		String examplesString = "";
		for (String example : examples) {
			examplesString += "\n"+example;
		}
		SpannableString chatFormatted = new SpannableString(greeting+examplesString);
		chatFormatted.setSpan( new StyleSpan(Typeface.ITALIC), greeting.length(), chatFormatted.length(), 0);

		ChatItem chatItem = new ChatItem(chatFormatted, null, null, ChatType.VirtualAgentContinued);
		addChatItem(chatItem);
	}
	

	private void showIntro() {
		String greeting =  getResources().getString(R.string.greeting);
		if (SettingsAPI.getShowIntroTips(getActivity())) {
			ChatItem chat = new ChatItem(greeting,null, null, ChatType.VirtualAgent);
			addChatItem(chat);
			showExamples();
		}
		else {
			int pos = greeting.length();
			String seeExamples = "\nTap here to see some examples.";
			greeting += new SpannedString(seeExamples);
			SpannableString sgreet = new SpannableString(greeting);
			int col = getResources().getColor(R.color.vha_chat_no_session_text);
			sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
			sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
			ChatItem chat = new ChatItem(sgreet,null, null, ChatType.VirtualAgentWelcome);
			addChatItem(chat);
		}
		
		//mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}


	private static Random randomGenerator = new Random();
	private static final String exampleMonth = "June";

	private static final float FADE_TO = 0.14f;
	
	private final String tests[] = { "Hotel tonight", 
			"Hotel in Madrid "+exampleMonth+" 22nd to 24th", 
			"Hotel in Paris "+exampleMonth+" 22nd to 24th", 
			"Hilton Hotel in Miami "+exampleMonth+" 22nd to 24th" };

	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ChatItem item = (ChatItem) view.getTag();
		if (item == null) {
			if (editedChatItemIndex != -1) {
				closeEditChatItem(false);
			}
			return;
		}
		
		switch (item.getType()) {
		case DialogAnswer:
			clickedDialogAnswer(item);
			break;
		case VirtualAgentWelcome:
			if (editedChatItemIndex != -1) {
				closeEditChatItem(false);
			}
			else {
				showExamples();
			}
			break;
			
		case VirtualAgentContinued:
			// only on debug build - click examples runs a random example
			if (BuildConfig.DEBUG) {
				String t = tests[randomGenerator.nextInt(tests.length)];
				addChatItem(new ChatItem(t));
				VHAApplication.EVA.searchWithText(t, null, false);
			}
			else {
				editVirtualAgentChat(item, position);
			}
			break;
			
		case DialogQuestion:
		case VirtualAgent:
			editVirtualAgentChat(item, position);
			break;
			
		case Me:
			editMeChat(item, position);
			break;
		}		
	}

	private void clickedDialogAnswer(ChatItem item) {
		DialogAnswerChatItem dialogItem = (DialogAnswerChatItem) item;
		DialogQuestionChatItem question = dialogItem.getQuestion();
		if (question.isAnswered()) {
			return; // already answered
		}
		dialogItem.setChosen();
		// remove un-chosen answers
		ArrayList<Integer> indexesToRemove = new ArrayList<Integer>();
		for (DialogAnswerChatItem answer : question.getAnswers()) {
			if (answer.isChosen() == false) {
				indexesToRemove.add(mChatListModel.indexOf(answer));
				//mChatAdapter.remove(answer);
			}
		}
		mAnimAdapter.animateDismiss(indexesToRemove);
		//mChatAdapter.notifyDataSetChanged();
		
		addChatItem(new ChatItem(dialogItem.getChat()));
		
		// todo: move this to mainActivity?
		VHAApplication.EVA.replyToDialog(dialogItem.getIndex());
	}

	private void editVirtualAgentChat(ChatItem item, int position) {

		if (editedChatItemIndex != -1) {
			closeEditChatItem(false);
		}
		else {
			if (item.getType() == ChatType.DialogQuestion) {
				// search for "Me" chat after
				for (int i=position+1; i<mChatListModel.size(); i++) {
					ChatItem itemAfter = mChatListModel.get(i);
					if (itemAfter.getType() == ChatType.Me) {
						editMeChat(itemAfter, i);
						return;
					}
				}
				// no me chat after question - add one
				addUtterance();
			}
			else {
				// search for last "Me" chat before
				for (int i=mChatListModel.size()-1; i>0; i--) {
					ChatItem itemBefore = mChatListModel.get(i);
					if (itemBefore.getType() == ChatType.Me) {
						editMeChat(itemBefore, i);
						return;
					}
				}
				// no me-chat - add one
				addUtterance();
			}
		}

	}

	private void editMeChat(ChatItem current, int position) {
		if (editedChatItemIndex != -1) {
			closeEditChatItem(false);
		}
		else {
			// you can only edit the last utterance
			for (int i=position+1; i< mChatListModel.size(); i++ ) {
				ChatItem itemAfter = mChatListModel.get(i);
				if (itemAfter.getType() == ChatType.Me) {
					Toast.makeText(getActivity(), "You can only modify your last utterance", Toast.LENGTH_SHORT).show();
					return;
				}
			}
			
			
			if (current.getStatus() != Status.InEdit) {
				current.setStatus(Status.InEdit);
				eventManager.fire(new ToggleMainButtonsEvent(false));
				editedChatItemIndex = position;
				mChatAdapter.notifyDataSetChanged();
			}
		}
	}
	
	public boolean isReady() {
		return mChatAdapter != null;
	}

	public void clearChat() {
		shownExamples = false;
		editedChatItemIndex = -1;
		mAnimAdapter.reset();
		mChatListModel.clear();
	}


//	@Override
//	public boolean onTouch(View v, MotionEvent event) {
//		Log.d(TAG, "touch: "+v.getId());
//		return false;
//	}
	
	public void voiceResponseToChatItem(ChatItem chatItem, SpannableString chat) {
		if (editedChatItemIndex == -1) {
			Log.w(TAG, "voice response but no edited item");
			return;
		}
		ChatItem editedChatItem = mChatListModel.get(editedChatItemIndex);
		if (editedChatItem != chatItem) {
			Log.w(TAG, "voice response to item, but editing another item");
			return;
		}
		chatItem.setChat(chat);
		dismissItemsFromPosition(editedChatItemIndex+1);
		closeEditChatItem(false);
	}

	public View getViewForChatItem(ChatItem chatItem) {
		return mChatListView.findViewWithTag(chatItem);
	}
	
	/***
	 * Close the chat-utterance that is being editted right now
	 * 
	 * @param isSubmitted - true if submit modification, false if revert to pre-modified text (or remove new utterance)
	 */
	private void closeEditChatItem(boolean isSubmitted) {
		if (editedChatItemIndex == -1) {
			VHAApplication.logError(TAG, "Unexpected closed edit chat item");
			return;
		}
		if (getActivity() == null) {
			VHAApplication.logError(TAG, "no activity connected to chat Fragment");
		} 
		else {
			InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			if (imm == null) {
				VHAApplication.logError(TAG, "no input method manager");
			}
			else {
				imm.hideSoftInputFromWindow(mChatListView.getWindowToken(), 0);
			}
		}
		
		ChatItem editedChatItem = mChatListModel.get(editedChatItemIndex);
		editedChatItem.setStatus(Status.ToSearch);
		eventManager.fire(new ToggleMainButtonsEvent(true));
		String preModifiedString = editedChatItem.getChat().toString();
		if (isSubmitted) {
			View rowView = getViewForChatItem(editedChatItem);
			if (rowView == null) {
				VHAApplication.logError(TAG, "Unexpected edited row not found");
				return;
			}
			EditText editText = (EditText)rowView.findViewById(R.id.editText);
			if (editText == null) {
				VHAApplication.logError(TAG, "Unexpected editText not found");
				return;
			}
			// if the pre-edit text is empty - this is a new chat to be added - not existing chat to edit
			boolean editLastUtterance = false == preModifiedString.equals("");
			String newText = editText.getText().toString();
			editedChatItem.setChat(newText);
	
			dismissItemsFromPosition(editedChatItemIndex+1);
			eventManager.fire(new ChatItemModified(editedChatItem, false, editLastUtterance));
		}
		else {
			// not submitting - just canceling edit
			// if this chat was empty text (new chat) - cancel adding it
			if (preModifiedString.equals("")) {
				mAnimAdapter.animateDismiss(editedChatItemIndex);
			}
		}
		
		editedChatItemIndex = -1;
		mChatAdapter.notifyDataSetChanged();
	}
	
	private void dismissItemsFromPosition(int position) {
		ArrayList<Integer>  itemsToDismiss = new ArrayList<Integer>();
		for (int i=position; i<mChatListModel.size(); i++) {
			itemsToDismiss.add(i);
		}
		
		mAnimAdapter.animateDismiss(itemsToDismiss);
	}
	
	public final OnClickListener deleteHandler = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "Clicked delete");
			if (editedChatItemIndex == -1) {
				VHAApplication.logError(TAG, "Unexpected delete no edit chat item");
				return;
			}
			ChatItem editedChatItem = mChatListModel.get(editedChatItemIndex);
			String preModifiedString = editedChatItem.getChat().toString();
			boolean editLastUtterance = false == preModifiedString.isEmpty();
			
			dismissItemsFromPosition(editedChatItemIndex);
			editedChatItemIndex = -1;
			if (editLastUtterance) {
				// non empty last utterance - send to server 
				eventManager.fire(new ChatItemModified(null, false, true));
			}
			eventManager.fire(new ToggleMainButtonsEvent(true));
		}
	};

	public final OnEditorActionListener editorActionHandler = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			Log.d(TAG, "editor Action "+actionId);
			if (editedChatItemIndex == -1) {
				VHAApplication.logError(TAG, "Unexpected execute no edit chat item");
				return false;
			}
			closeEditChatItem(true);
			return false;
		}
	};

	public OnClickListener micButtonHandler = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (editedChatItemIndex == -1) {
				VHAApplication.logError(TAG, "Unexpected microphone no edit chat item");
				return;
			}
			ChatItem editedChatItem = mChatListModel.get(editedChatItemIndex);
			// if the pre-edit text is empty - this is a new chat to be added - not existing chat to edit
			boolean editLastUtterance = false == editedChatItem.getChat().toString().isEmpty();

			eventManager.fire(new ChatItemModified(editedChatItem, true, editLastUtterance));
		}
	};

	/**
	 * If a chat item is edited  - back will close the edit
	 * @return  true if back button was useful
	 */
	public boolean handleBackPressed() {
		if (editedChatItemIndex == -1) {
			return false;
		}
		closeEditChatItem(false);
		return true;
	}

	public void addUtterance() {
		if (editedChatItemIndex != -1) {
			closeEditChatItem(false);
		}
		
		ChatItem editChat = new ChatItem("");
		editChat.setStatus(Status.InEdit);
		eventManager.fire(new ToggleMainButtonsEvent(false));
		addChatItem(editChat);
		editedChatItemIndex = mChatListModel.size()-1;

	}

	public void fadeOutOtherChat(ChatItem chatItem) {
		AlphaAnimation animFadeOut = new AlphaAnimation(1f, FADE_TO);
		animFadeOut.setDuration(500);
		animFadeOut.setRepeatCount(0);
		animFadeOut.setFillAfter(true);

		AlphaAnimation animFadeIn = new AlphaAnimation(FADE_TO, 1f);
		animFadeIn.setFillAfter(true);
		animFadeIn.setRepeatCount(0);
		animFadeIn.setDuration(200);
		
		View viewToNotFade = getViewForChatItem(chatItem);
		for (int i=0; i<mChatListView.getChildCount(); i++) {
			View current = mChatListView.getChildAt(i);
			if (current == viewToNotFade) {
				current.startAnimation(animFadeIn);
			}
			else {
				if (current.getTag() != null)
					current.startAnimation(animFadeOut);
			}
		}
	}
	
	public void fadeInAll() {
		AlphaAnimation anim = new AlphaAnimation(FADE_TO, 1f);
		anim.setFillAfter(true);
		anim.setRepeatCount(0);
		anim.setDuration(200);
		for (int i=0; i<mChatListView.getChildCount(); i++) {
			View current = mChatListView.getChildAt(i);
			if (current.getTag() != null)
				current.startAnimation(anim);
		}
	}
	
}


