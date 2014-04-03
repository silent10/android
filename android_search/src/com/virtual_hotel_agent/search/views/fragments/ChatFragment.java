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
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
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
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.virtual_hotel_agent.search.BuildConfig;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.events.ChatItemModified;
import com.virtual_hotel_agent.search.controllers.events.ToggleMainButtonsEvent;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.views.adapters.ChatAdapter;
import com.virtual_hotel_agent.search.views.adapters.ChatAdapter.MeRowHolder;
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
//		mChatListModel.loadInstanceState(savedInstanceState);
		mChatAdapter = new ChatAdapter(this, R.layout.row_vha_chat, R.id.label, mChatListModel);

		mAnimAdapter = new ChatAnimAdapter(mChatAdapter, 0, 300, new MyOnDismissCallback());
		mAnimAdapter.setAbsListView(mChatListView);
		mChatListView.setAdapter(mAnimAdapter);
        

        	        
//		chatListView.setAdapter(mChatAdapter);
		
		mChatListView.setOnItemClickListener(this);
		
		showIntro();
		
		return root;
	}
	
	private class MyOnDismissCallback implements OnDismissCallback {

        @Override
        public void onDismiss(final AbsListView listView, final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
            	mChatAdapter.remove(position);
            }
        }
    }

	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(instanceState);
		
//		mChatListModel.saveInstanceState(instanceState);
	}

	public void addChatItem(ChatItem chatItem) {
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
	private final String tests[] = { "Hotel tonight", 
			"Hotel in Madrid "+exampleMonth+" 22nd to 24th", 
			"Hotel in Paris "+exampleMonth+" 22nd to 24th", 
			"Hilton Hotel in Miami "+exampleMonth+" 22nd to 24th" };

	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ChatItem item = (ChatItem) view.getTag();
		if (item == null) {
			editVirtualAgentChat(item, position);
			return;
		}
		
		switch (item.getType()) {
		case DialogAnswer:
			clickedDialogAnswer(item);
			break;
		case VirtualAgentWelcome:
			Log.i(TAG, "Eva item clicked: "+item.getChat());
			showExamples();
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
		// search for "Me" chat before
		for (int i=position-1; i>0; i--) {
			ChatItem itemBefore = mChatListModel.get(i);
			if (itemBefore.getType() == ChatType.Me) {
				editMeChat(itemBefore, i);
				return;
			}
		}
		
		// finished loop - no "Me" chat was found - add one and edit it
		ChatItem editChat = new ChatItem("");
		editChat.setStatus(Status.InEdit);
		eventManager.fire(new ToggleMainButtonsEvent(false));
		addChatItem(editChat);
		editedChatItemIndex = mChatListModel.size()-1;
	}

	private void editMeChat(ChatItem current, int position) {
		if (editedChatItemIndex == position) {
			// this chat item is already in edit - close it
			closeEditChatItem(false);
			return;
		}

		// you can only edit the last utterance
		for (int i=position+1; i< mChatListModel.size(); i++ ) {
			ChatItem itemAfter = mChatListModel.get(i);
			if (itemAfter.getType() == ChatType.Me) {
				Toast.makeText(getActivity(), "You can only modify your last utterance", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		
		if (editedChatItemIndex != -1) {
			closeEditChatItem(false);
		}
		if (current.getStatus() != Status.InEdit) {
			current.setStatus(Status.InEdit);
			eventManager.fire(new ToggleMainButtonsEvent(false));
			editedChatItemIndex = position;
			mChatAdapter.notifyDataSetChanged();
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
		closeEditChatItem(false);
		dismissItemsFromPosition(editedChatItemIndex+1);
	}

	private void closeEditChatItem(boolean isSubmitted) {
		InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mChatListView.getWindowToken(), 0);
			
		if (editedChatItemIndex == -1) {
			VHAApplication.logError(TAG, "Unexpected closed edit chat item");
			return;
		}
		ChatItem editedChatItem = mChatListModel.get(editedChatItemIndex);
		editedChatItem.setStatus(Status.ToSearch);
		eventManager.fire(new ToggleMainButtonsEvent(true));
		if (isSubmitted) {
			//View rowView = mChatListView.getChildAt(editedChatItemIndex - mChatListView.getFirstVisiblePosition() );
			View rowView = mChatListView.findViewWithTag(editedChatItem);
			if (rowView == null) {
				VHAApplication.logError(TAG, "Unexpected edited row not found");
				return;
			}
			EditText editText = (EditText)rowView.findViewById(R.id.editText);
			if (editText == null) {
				VHAApplication.logError(TAG, "Unexpected editText not found");
				return;
			}
			String newText = editText.getText().toString();
			editedChatItem.setChat(newText);
	
			dismissItemsFromPosition(editedChatItemIndex+1);
			eventManager.fire(new ChatItemModified(editedChatItem, false));
		}
		else {
			// not submitting - just canceling edit
			// if this chat was empty text (new chat) - cancel adding it
			mAnimAdapter.animateDismiss(editedChatItemIndex);
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
			dismissItemsFromPosition(editedChatItemIndex);
			editedChatItemIndex = -1;
			eventManager.fire(new ChatItemModified(null, false));
			eventManager.fire(new ToggleMainButtonsEvent(true));
		}
	};

	public final OnFocusChangeListener focusChangedHandler = new OnFocusChangeListener() {
		
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			EditText et = (EditText) v;
			Log.d(TAG, "Focus: "+hasFocus+"  text: "+et.getText());
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
			ChatItem chatItem = mChatListModel.get(editedChatItemIndex);
			eventManager.fire(new ChatItemModified(chatItem, true));
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
	
}


