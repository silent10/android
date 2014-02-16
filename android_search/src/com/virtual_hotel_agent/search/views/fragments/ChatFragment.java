package com.virtual_hotel_agent.search.views.fragments;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import roboguice.util.Ln;
import android.os.Bundle;
import android.text.SpannableString;
import com.evature.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.inject.Inject;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.events.ChatItemClicked;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.views.adapters.ChatAdapter;

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
	static final String TAG = "ChatFragment";

//	public static Fragment newInstance(ChatItemList chatListModel) {
//		ChatFragment f = new ChatFragment();
//		f.mChatListModel = chatListModel;
//		return f;
//	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Ln.d("onCreateView");
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		ListView chatListView = (ListView) root.findViewById(R.id.chat_list);
		
		// Connect the data of the chat history to the view:
//		mChatListModel.loadInstanceState(savedInstanceState);
		mChatAdapter = new ChatAdapter(getActivity(), R.layout.row_eva_chat, R.id.label, mChatListModel);
		chatListView.setAdapter(mChatAdapter);
		
		chatListView.setOnItemClickListener(this);
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		Ln.d("onSaveInstanceState");
		super.onSaveInstanceState(instanceState);
		
//		mChatListModel.saveInstanceState(instanceState);
	}

//	public void addChatItem(ChatItem chatItem) {
//		
//		mChatAdapter.add(chatItem);
//	}
	
	public void invalidate() { 
		if (mChatAdapter != null)
			mChatAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		ChatItem item = (ChatItem) view.getTag();
		
		
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
	
	
}


