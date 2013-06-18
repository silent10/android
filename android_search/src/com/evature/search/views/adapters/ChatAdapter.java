// Relevant example: http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
package com.evature.search.views.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.evature.search.R;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItemList;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "ChatAdapter";
	private ChatItemList mChatList;
	LayoutInflater mInflater;

	public ChatAdapter(Activity activity, int resource, int textViewResourceId, ChatItemList chatList) {
		super(activity, resource, textViewResourceId, chatList.getItemList());
		mChatList = chatList;
		mInflater = LayoutInflater.from(activity);
	}
	
	@Override
	public int getViewTypeCount(){
	  return ChatItem.CHAT_TYPES;
	}

	@Override
	public int getItemViewType(int position){
		ChatItem chatItem = mChatList.get(position);
		return chatItem.getType(); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatItem chatItem = mChatList.get(position);
//		Log.d(TAG, "Creating view for row "+position+"  chat: "+chatItem.getChat());
		int viewType = getItemViewType(position);
		View row = convertView;
		if (row == null) {
			switch (viewType) {
			case ChatItem.CHAT_ME:
				row = mInflater.inflate(R.layout.row_mychat, parent, false);
				break;
			case ChatItem.CHAT_EVA:
				row = mInflater.inflate(R.layout.row_eva_chat, parent, false);
				break;
			}
		}
		
		TextView label = (TextView) row.findViewById(R.id.label);
		label.setText(chatItem.getChat());
		
		// some row types require more than label
		switch (viewType) {
		}
		return row;
	}
}
