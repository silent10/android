package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.evature.search.R;
import com.evature.search.models.ChatItem;
import com.evature.search.models.ChatItemList;
import com.evature.search.views.adapters.ChatAdapter;

public class ChatFragment extends RoboFragment {

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainmenu, menu);
	}

	private ChatItemList mChatListModel;
	private ChatAdapter mChatAdapter;
	static final String TAG = "ChatFragment";

	public static Fragment newInstance(ChatItemList chatListModel) {
		ChatFragment f = new ChatFragment();
		f.mChatListModel = chatListModel;
		return f;
	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		ListView chatListView = (ListView) root.findViewById(R.id.chat_list);
		
		// Connect the data of the chat history to the view:
		mChatListModel.loadInstanceState(savedInstanceState);
		mChatAdapter = new ChatAdapter(this, R.layout.row, R.id.label, mChatListModel);
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

	public ChatItemList getChatListModel() {
		return mChatListModel;
	}

}
