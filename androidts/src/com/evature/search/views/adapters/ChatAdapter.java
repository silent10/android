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
import com.evature.search.models.ChatItem;
import com.evature.search.models.ChatItemList;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "ChatAdapter";
	private ChatItemList mChatList;
	LayoutInflater mInflater;

	public ChatAdapter(Activity activity, int resource, int textViewResourceId, ChatItemList chatList) {
		super(activity, resource, textViewResourceId, chatList.getItemList());
		mChatList = chatList;
		mInflater = LayoutInflater.from(activity);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ChatItem chatItem = mChatList.get(position);
		Log.d(TAG, "Creating view for row "+position+"  chat: "+chatItem.getChat());
		View row = convertView;
		row = mInflater.inflate(R.layout.row, parent, false);
		TextView label = (TextView) row.findViewById(R.id.label);
		label.setText(chatItem.getChat());
		ImageView icon = (ImageView) row.findViewById(R.id.icon);
		if (chatItem.isEva()) {
			icon.setImageResource(R.drawable.eva_icon);
		} else {
			icon.setImageResource(R.drawable.chat_icon);
		}
		return (row);
	}
}
