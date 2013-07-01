// Relevant example: http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
package com.evature.search.views.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.evature.search.R;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItem.ChatType;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.models.chat.DialogAnswerChatItem;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "ChatAdapter";
	private static final int VIEW_TYPE_COUNT = ChatType.values().length;
	
	// TODO: move to resource file
	private ChatItemList mChatList;
	LayoutInflater mInflater;

	int myChatInSessionBg, myChatInSessionText, myChatNoSessionBg, myChatNoSessionText;
	int evaChatInSessionBg, evaChatInSessionText, evaChatNoSessionBg, evaChatNoSessionText;
	
	public ChatAdapter(Activity activity, int resource, int textViewResourceId, ChatItemList chatList) {
		super(activity, resource, textViewResourceId, chatList.getItemList());
		mChatList = chatList;
		mInflater = LayoutInflater.from(activity);
		
		Resources resources = activity.getResources();
		myChatInSessionBg = resources.getColor(R.color.my_chat_in_session_bg);
		myChatInSessionText = resources.getColor(R.color.my_chat_in_session_text);
		myChatNoSessionBg = resources.getColor(R.color.my_chat_no_session_bg);
		myChatNoSessionText = resources.getColor(R.color.my_chat_no_session_text);
		evaChatInSessionBg = resources.getColor(R.color.eva_chat_in_session_bg);
		evaChatInSessionText = resources.getColor(R.color.eva_chat_in_session_text);
		evaChatNoSessionBg = resources.getColor(R.color.eva_chat_no_session_bg);
		evaChatNoSessionText = resources.getColor(R.color.eva_chat_no_session_text);
	}
	
	@Override
	public int getViewTypeCount(){
	  return VIEW_TYPE_COUNT;
	}
	
	@Override
	public ChatItem getItem(int position) {
		// todo: if some items are collapsed then count from start and skip them
		ChatItem chatItem = mChatList.get(position);
		return chatItem;
	};
	
	@Override public int getCount() {
		// todo: if some items are collapsed then count from start and skip them
		return mChatList.getItemList().size();
	};

	@Override
	public int getItemViewType(int position){
		ChatItem chatItem = getItem(position);
		return chatItem.getType().ordinal(); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatItem chatItem = getItem(position);
//		Log.d(TAG, "Creating view for row "+position+"  chat: "+chatItem.getChat());
		ChatType viewType = chatItem.getType(); // should be same logic as getItemViewType
		View row = convertView;
		if (row == null) {
			switch (viewType) {
			case Me:
				row = mInflater.inflate(R.layout.row_mychat, parent, false);
				break;
			case DialogQuestion:
			case Eva:
				row = mInflater.inflate(R.layout.row_eva_chat, parent, false);
				break;
			case DialogAnswer:
				row = mInflater.inflate(R.layout.row_eva_dialog, parent, false);
				break;
			}
		}
		
		TextView label = (TextView) row.findViewById(R.id.label);
		label.setText(chatItem.getChat());
		
		
		// some row types require more than label setting...
		switch (viewType) {
		case DialogAnswer:
			DialogAnswerChatItem dialogItem = (DialogAnswerChatItem)chatItem;
			TextView respInd = (TextView)row.findViewById(R.id.response_index);
			respInd.setText( Integer.toString(dialogItem.getIndex() + 1) );
			if (dialogItem.isChosen()) {
				label.setTypeface(null, Typeface.BOLD);
			}
			else {
				label.setTypeface(null, Typeface.NORMAL);
			}
			break;
		}
		
		if (viewType == ChatType.Me) {
			TextView icon = (TextView)row.findViewById(R.id.icon);
			if (chatItem.isInSession()) {
				row.setBackgroundColor(myChatInSessionBg);
				label.setTextColor(myChatInSessionText);
				icon.setTextColor(myChatInSessionText);
			}
			else {
				row.setBackgroundColor(myChatNoSessionBg);
				label.setTextColor(myChatNoSessionText);
				icon.setTextColor(myChatNoSessionText);
			}
		}
		else {
			if (chatItem.isInSession()) {
				row.setBackgroundColor(evaChatInSessionBg);
				label.setTextColor(evaChatInSessionText);
			}
			else {
				row.setBackgroundColor(evaChatNoSessionBg);
				label.setTextColor(evaChatNoSessionText);
			}
		}
//		if (chatItem.isActivated()) {
//			if (viewType == ChatType.Me) {
//				if (label.getVisibility() != View.GONE) {
//					label.setVisibility(View.GONE);
//					EditText editBox = (EditText)row.findViewById(R.id.editText);
//					editBox.setText(chatItem.getChat(), TextView.BufferType.EDITABLE);
//					editBox.setVisibility(View.VISIBLE);
//				}
//			}
//			else {
//				row.setBackgroundColor(SelectedBackgroundColor);
//				label.setTextColor(SelectedTextColor);
//			}
//		}
		row.setTag(chatItem);
		return row;
	}
}
