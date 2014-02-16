// Relevant example: http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
package com.virtual_hotel_agent.search.views.adapters;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import com.evature.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evaapis.crossplatform.flow.FlowElement;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "ChatAdapter";
	private static final int VIEW_TYPE_COUNT = ChatType.values().length;
	
	// TODO: move to resource file
	private ChatItemList mChatList;
	LayoutInflater mInflater;

	int myChatInSessionBg, myChatInSessionText, myChatNoSessionBg, myChatNoSessionText;
	int vhaChatInSessionBg, vhaChatInSessionText, vhaChatNoSessionBg, vhaChatNoSessionText;
	
	public ChatAdapter(Activity activity, int resource, int textViewResourceId, ChatItemList chatList) {
		super(activity, resource, textViewResourceId, chatList);
		mChatList = chatList;
		mInflater = LayoutInflater.from(activity);
		
		Resources resources = activity.getResources();
		myChatInSessionBg = resources.getColor(R.color.my_chat_in_session_bg);
		myChatInSessionText = resources.getColor(R.color.my_chat_in_session_text);
		myChatNoSessionBg = resources.getColor(R.color.my_chat_no_session_bg);
		myChatNoSessionText = resources.getColor(R.color.my_chat_no_session_text);
		vhaChatInSessionBg = resources.getColor(R.color.eva_chat_in_session_bg);
		vhaChatInSessionText = resources.getColor(R.color.eva_chat_in_session_text);
		vhaChatNoSessionBg = resources.getColor(R.color.eva_chat_no_session_bg);
		vhaChatNoSessionText = resources.getColor(R.color.eva_chat_no_session_text);
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
		if (mChatList == null) {
			Log.e(TAG, "null chatList");
			return 0;
		}
		return mChatList.size();
	};

	@Override
	public int getItemViewType(int position){
		ChatItem chatItem = getItem(position);
		return chatItem.getType().ordinal(); 
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ChatItem chatItem = getItem(position);
		ChatType viewType = chatItem.getType(); // should be same logic as getItemViewType
		View row = convertView;
		if (row == null) {
//			Ln.d("Creating view for row "+position+" type: "+viewType+"  chat: "+chatItem.getChat());
			switch (viewType) {
			case Me:
				row = mInflater.inflate(R.layout.row_mychat, parent, false);
				break;
			case DialogQuestion:
			case VirtualAgent:
				row = mInflater.inflate(R.layout.row_eva_chat, parent, false);
				break;
			case VirtualAgentContinued:
			case DialogAnswer:
				row = mInflater.inflate(R.layout.row_eva_dialog, parent, false);
				break;
			}
		}
		else {
//			Ln.d("reusing view for row "+position+" type: "+viewType+"  chat: "+chatItem.getChat());
		}
		
		TextView label = (TextView) row.findViewById(R.id.label);
		label.setText(chatItem.getChat());
		
		
		// some row types require more than label setting...
		switch (viewType) {
		case VirtualAgentContinued:
			TextView _respInd = (TextView)row.findViewById(R.id.response_index);
			_respInd.setText( "" );
			label.setTextColor(vhaChatInSessionText);
			label.setTypeface(null, Typeface.NORMAL);
			break;
			
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
			
			
		case Me:
//			TextView icon = (TextView)row.findViewById(R.id.icon);
			if (chatItem.isInSession()) {
				row.setBackgroundColor(myChatInSessionBg);
				label.setTextColor(myChatInSessionText);
//				icon.setTextColor(myChatInSessionText);
			}
			else {
				row.setBackgroundColor(myChatNoSessionBg);
				label.setTextColor(myChatNoSessionText);
//				icon.setTextColor(myChatNoSessionText);
			}
			break;
			
			
		default:
			ImageView topImg = (ImageView) row.findViewById(R.id.top_icon);
			ProgressBar progress = (ProgressBar) row.findViewById(R.id.progressBar_search);
			progress.setVisibility(View.GONE);
			topImg.setVisibility(View.GONE);
			FlowElement flow = chatItem.getFlowElement();
			ImageView img = (ImageView) row.findViewById(R.id.icon);
			if (flow == null) {
				img.setImageResource(R.drawable.vha_head);
			}
			else {
				switch (flow.Type) {
				case Hotel:
					img.setImageResource(R.drawable.hotel_small);
					break;
//				case Flight:
//					img.setImageResource(R.drawable.airplane_small);
//					break;
				case Question:
					img.setImageResource(R.drawable.vha_head);
				}
			}
			
			switch(chatItem.getStatus()) {
			case HasResults:
				topImg.setImageResource(R.drawable.see_results);
				topImg.setVisibility(View.VISIBLE);
				break;
			case InSearch:
				progress.setVisibility(View.VISIBLE);
				break;
			case ToSearch:
				topImg.setImageResource(R.drawable.search);
				topImg.setVisibility(View.VISIBLE);
				break;				
			}
			
			if (chatItem.isInSession()) {
				row.setBackgroundColor(vhaChatInSessionBg);
				label.setTextColor(vhaChatInSessionText);
			}
			else {
				row.setBackgroundColor(vhaChatNoSessionBg);
				label.setTextColor(vhaChatNoSessionText);
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
