package com.evature.search.models.chat;

import java.util.ArrayList;

import com.google.inject.Singleton;

@Singleton
public class ChatItemList extends ArrayList<ChatItem> {
	static final String TAG = "ChatItemList";

//	public void saveInstanceState(Bundle instanceState) {
//		instanceState.putParcelableArrayList("mChatListEva", (ArrayList<ChatItem>) mChatListEva);
//	}
//	public void loadInstanceState(Bundle savedInstanceState) {
//		if (savedInstanceState != null) {
//			Log.d(TAG, "Loading ChatItem from savedInstanceState");
//			// Restore last state for checked position.
//			ArrayList<ChatItem> parcelableArrayList = savedInstanceState.getParcelableArrayList("mChatListEva");
//			if (parcelableArrayList != null)
//				mChatListEva = parcelableArrayList;
//		}
//		else {
//			mChatListEva = new ArrayList<ChatItem>();
//		}
//	}


}
