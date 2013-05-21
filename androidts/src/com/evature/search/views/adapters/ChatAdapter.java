// Relevant example: http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
package com.evature.search.views.adapters;

import java.util.List;

import com.evature.search.R;
import com.evature.search.R.drawable;
import com.evature.search.R.id;
import com.evature.search.R.layout;
import com.evature.search.models.ChatItem;
import com.evature.search.views.fragments.ChatFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private List<ChatItem> mChatList;
	LayoutInflater mInflater;

	public ChatAdapter(ChatFragment chatFragment, int resource, int textViewResourceId, List<ChatItem> objects) {
		super(chatFragment.getActivity(), resource, textViewResourceId, objects);
		mChatList = objects;
		mInflater = LayoutInflater.from(chatFragment.getActivity());
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ChatItem chatItem = mChatList.get(position);
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
