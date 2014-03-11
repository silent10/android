package com.virtual_hotel_agent.search.views.fragments;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.evature.util.Log;
import com.google.inject.Inject;
import com.nhaarman.listviewanimations.swinginadapters.SingleAnimationAdapter;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.events.ChatItemClicked;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.views.adapters.ChatAdapter;
import com.virtual_hotel_agent.search.views.adapters.ChatAnimAdapter;

public class ChatFragment extends RoboFragment  implements OnItemClickListener {
	
	public interface DialogClickHandler {
		public void onClick(SpannableString dialog, int responseId);
	}
	
//	@Override
//	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//		inflater.inflate(R.menu.mainmenu, menu);
//	}

	@Inject protected EventManager eventManager;
	
	@Inject private ChatItemList mChatListModel;
	
	DialogClickHandler mDialogClickHandler;
	
	private ChatAdapter mChatAdapter;

	private SingleAnimationAdapter mAnimAdapter;

	private ViewGroup root;
	static final String TAG = "ChatFragment";

//	public static Fragment newInstance(ChatItemList chatListModel) {
//		ChatFragment f = new ChatFragment();
//		f.mChatListModel = chatListModel;
//		return f;
//	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");

		if (root != null) {
			Log.w(TAG, "Fragment initialized twice");
			((ViewGroup) root.getParent()).removeView(root);
			return root;
		}
		
		root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		ListView chatListView = (ListView) root.findViewById(R.id.chat_list);
		
		// Connect the data of the chat history to the view:
//		mChatListModel.loadInstanceState(savedInstanceState);
		mChatAdapter = new ChatAdapter(getActivity(), R.layout.row_vha_chat, R.id.label, mChatListModel);

		mAnimAdapter = new ChatAnimAdapter(mChatAdapter); //new SwingBottomInAnimationAdapter(
				//new SwingRightInAnimationAdapter(mChatAdapter);//);
		mAnimAdapter.setAbsListView(chatListView);
        chatListView.setAdapter(mAnimAdapter);
        	        
//		chatListView.setAdapter(mChatAdapter);
		
		chatListView.setOnItemClickListener(this);
		return root;
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
	}
	
	public void invalidate() { 
		if (mChatAdapter != null)
			mChatAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ChatItem item = (ChatItem) view.getTag();
		if (item == null) {
			return;
		}
		
		switch (item.getType()) {
		case DialogAnswer:
			DialogAnswerChatItem dialogItem = (DialogAnswerChatItem) item;
			DialogQuestionChatItem question = dialogItem.getQuestion();
			if (question.isAnswered()) {
				return; // already answered
			}
			dialogItem.setChosen();
			// remove un-chosen answers
			for (DialogAnswerChatItem answer : question.getAnswers()) {
				if (answer.isChosen() == false) {
					mChatAdapter.remove(answer);
				}
			}
			mChatAdapter.notifyDataSetChanged();
			mDialogClickHandler.onClick(dialogItem.getChat(), dialogItem.getIndex());
			break;
//		case Me:
//			item.setEditMode();
//			mChatAdapter.notifyDataSetChanged();
//			break;
		case VirtualAgentContinued:
		case VirtualAgentWelcome:
		case VirtualAgent:
			Log.i(TAG, "Eva item clicked: "+item.getChat());
			eventManager.fire(new ChatItemClicked(item) );
//			mChatAdapter.notifyDataSetChanged();
			break;
		}
		
	}

	public void setDialogHandler(DialogClickHandler handler) {
		mDialogClickHandler = handler;
	}

	public boolean isReady() {
		return mChatAdapter != null;
	}

	public void clearChat() {
		mAnimAdapter.reset();
		mChatListModel.clear();
	}
	
	
}


