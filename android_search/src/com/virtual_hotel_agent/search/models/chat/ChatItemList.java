package com.virtual_hotel_agent.search.models.chat;

import java.util.ArrayList;

import android.os.Bundle;

public class ChatItemList extends ArrayList<ChatItem> {
	static final String TAG = "ChatItemList";
	
	private ChatItemList() {
	}
	
	private static class ChatItemListHolder {
		public static final ChatItemList instance = new ChatItemList();
	}

	public static ChatItemList getInstance() {
		return ChatItemListHolder.instance;
	}


	public void saveInstanceState(Bundle instanceState) {
//		Bundle replyCache = new Bundle();
//		for (ChatItem ci: this) {
//			if (ci.getEvaReply() != null) {
//				if (replyCache.containsKey(ci.getEvaReply().transactionId) == false) {
//					replyCache.putString(ci.getEvaReply().transactionId, ci.getEvaReply().JSONReply.toString());
//				}
//			}
//		}
//		instanceState.putBundle("reply_cache", replyCache);
//		instanceState.putS("mChatListEva", this);
//		//instanceState.put
//		ArrayList<String> transactionIds = new ArrayList<String>();
//		for (ChatItem ci: this) {
//			transactionIds.add(ci.getEvaReply() == null ? "" : ci.getEvaReply().transactionId);
//		}
//		instanceState.putStringArrayList("chat_items_transactions", transactionIds);
		instanceState.putSerializable("chat-item-list", this);
	}
	
	public void loadInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
//			Log.d(TAG, "Loading ChatItems from savedInstanceState");
//			
//			// Restore last state for checked position.
			ArrayList<ChatItem> list = (ArrayList<ChatItem>) savedInstanceState.getSerializable("chat-item-list");
			clear();
			if (list != null) {
				for (ChatItem ci : list) {
					add(ci);
				}
			}
			
			// connect this fragment to the MainView -
		}
	}


}
