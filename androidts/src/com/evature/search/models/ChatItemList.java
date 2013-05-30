package com.evature.search.models;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;

public class ChatItemList {
	static final String TAG = "ChatItemList";
	
	protected List<ChatItem> mChatListEva = new ArrayList<ChatItem>();


	public List<ChatItem> getItemList() {
		return mChatListEva;
	}
	
	public void setItemList(List<ChatItem> list) {
		mChatListEva = list;
	}

	public ChatItem get(int position) {
		return mChatListEva.get(position);
	}

	public void saveInstanceState(Bundle instanceState) {
		instanceState.putParcelableArrayList("mChatListEva", (ArrayList<ChatItem>) mChatListEva);
	}
	public void loadInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			Log.d(TAG, "Loading ChatItem from savedInstanceState");
			// Restore last state for checked position.
			mChatListEva = savedInstanceState.getParcelableArrayList("mChatListEva");
		}
		else {
			mChatListEva = new ArrayList<ChatItem>();
		}
	}


}
