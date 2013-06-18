package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.evature.search.R;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.views.adapters.ChatAdapter;
import com.google.inject.Inject;

public class ChatFragment extends RoboFragment {

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainmenu, menu);
	}

	@Inject private ChatItemList mChatListModel;
	
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
		Log.d(TAG, "onCreateView");
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		ListView chatListView = (ListView) root.findViewById(R.id.chat_list);
		
		// Connect the data of the chat history to the view:
		mChatListModel.loadInstanceState(savedInstanceState);
		mChatAdapter = new ChatAdapter(getActivity(), R.layout.row_eva_chat, R.id.label, mChatListModel);
		chatListView.setAdapter(mChatAdapter);

		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(instanceState);
		
		mChatListModel.saveInstanceState(instanceState);
	}

	public void addChatItem(ChatItem chatItem) {
		mChatAdapter.add(chatItem);
	}

}