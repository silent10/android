// Relevant example: http://windrealm.org/tutorials/android/listview-with-checkboxes-without-listactivity.php
package com.virtual_hotel_agent.search.views.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.evature.util.DLog;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;

public class ChatAdapter extends ArrayAdapter<ChatItem> {

	private static final String TAG = "ChatAdapter";
	private static final int VIEW_TYPE_COUNT = ChatType.values().length+1;
	
	private final ChatItemList mChatList;
	private final ChatFragment chatFragment;
	private final LayoutInflater mInflater;

	int  myChatTextColor;
	int  vhaChatTextColor;
	
	public ChatAdapter(final ChatFragment chatFragment, final ChatItemList chatList) {
		super(chatList);
		mChatList = chatList;
		this.chatFragment = chatFragment;
		Activity activity = chatFragment.getActivity();
		mInflater = LayoutInflater.from(activity);
		
		Resources resources = activity.getResources();
		myChatTextColor = resources.getColor(R.color.my_chat_text);
		vhaChatTextColor = resources.getColor(R.color.vha_chat_text);
	}
	
	@Override
	public int getViewTypeCount(){
	  return VIEW_TYPE_COUNT;
	}
	
	@Override
	public long getItemId(final int position) {
		return position;
	}
	@Override
	public boolean hasStableIds() {
		return true;
	}

	
	@Override
	public ChatItem getItem(int position) {
		// todo: if some items are collapsed then count from start and skip them
		if (position >= mChatList.size()) {
			DLog.e(TAG, "Accessing chat item "+position+" but size is "+mChatList.size());
			return null;
		}
		ChatItem chatItem = mChatList.get(position);
		return chatItem;
	};
	
	@Override public int getCount() {
		// todo: if some items are collapsed then count from start and skip them
		if (mChatList == null) {
			DLog.e(TAG, "null chatList");
			return 0;
		}
		return mChatList.size()+1;
	};

	@Override
	public int getItemViewType(int position){
		if (position >= mChatList.size()) {
			return VIEW_TYPE_COUNT-1;
		}
		ChatItem chatItem = getItem(position);
		return chatItem.getType().ordinal(); 
	}
	
	private View fillerView(View view, ViewGroup parent) {
		if (view == null) {
			view = mInflater.inflate(R.layout.row_filler, parent, false);
			view.setClickable(false);
			view.setEnabled(false);
		}
		return view;
	}
	
	
	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (position >= mChatList.size()) {
			return fillerView(convertView, parent);
		}
		ChatItem chatItem = getItem(position);
		ChatType viewType = chatItem.getType(); // should be same logic as getItemViewType
		View row = convertView;
		if (row == null) {
//			Ln.d("Creating view for row "+position+" type: "+viewType+"  chat: "+chatItem.getChat());
			switch (viewType) {
			case Me:
				row = mInflater.inflate(R.layout.row_user_chat, parent, false);
				UserRowHolder userHolder = new UserRowHolder();
				userHolder.editText = (EditText) row.findViewById(R.id.editText);
				userHolder.editText.setOnEditorActionListener(chatFragment.editorActionHandler);
				userHolder.chatbubble = row.findViewById(R.id.chat_bubble);
				userHolder.label = (TextView) row.findViewById(R.id.label);
				userHolder.inEdit = row.findViewById(R.id.edit_chat_item);
				row.setTag(R.id.chat_row_holder, userHolder);
				break;
			case DialogQuestion:
			case VirtualAgentError:
			case VirtualAgent:
			case VirtualAgentWelcome:
			case VirtualAgentContinued:
				row = mInflater.inflate(R.layout.row_vha_chat, parent, false);
				VhaRowHolder evaHolder = new VhaRowHolder();
				//evaHolder.cruisesFoundIcon = (ImageView) row.findViewById(R.id.cruises_found_icon);
				evaHolder.searchingProgress = (ProgressBar) row.findViewById(R.id.progressBar_search);
				evaHolder.subLabel = (TextView) row.findViewById(R.id.sub_label);
				evaHolder.label = (TextView) row.findViewById(R.id.label);
				evaHolder.chatbubble = row.findViewById(R.id.chat_bubble);
				row.setTag(R.id.chat_row_holder, evaHolder);	
				break;
			case DialogAnswer:
				row = mInflater.inflate(R.layout.row_vha_dialog, parent, false);
				break;
			}
		}
		else {
//			Ln.d("reusing view for row "+position+" type: "+viewType+"  chat: "+chatItem.getChat());
		}
		
		RowHolder holder = (RowHolder) row.getTag(R.id.chat_row_holder);
		if (holder == null) {
			holder = new RowHolder();
			holder.label = (TextView) row.findViewById(R.id.label);
			if (holder.label == null) {
				// rare bug that is hard to reproduce - the row is a recycled filler view but isn't at the last position
				DLog.e(TAG, "No label in Row #1 ?");
				if (convertView != null) {
					// try again without view recycling
					return getView(position, null, parent);
				}
			}
			row.setTag(R.id.chat_row_holder, holder);
		}
		TextView label = holder.label;
		if (label == null) {
			DLog.e(TAG, "No label in Row?");
			row.setTag(chatItem);
			return row;
		}
		else {
			label.setText(chatItem.getChat());
		}
		
		
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
			
			
		case Me:
//			TextView icon = (TextView)row.findViewById(R.id.icon);
//			if (chatItem.isInSession()) {
//				label.setTextColor(myChatInSessionText);
////				icon.setTextColor(myChatInSessionText);
//			}
//			else {
				label.setTextColor(myChatTextColor);
//				icon.setTextColor(myChatNoSessionText);
//			}
			
			UserRowHolder meHolder = (UserRowHolder) row.getTag(R.id.chat_row_holder); 
			
			if (chatItem.getStatus() == Status.InEdit) {
				meHolder.editText.setText(chatItem.getChat().toString());
				label.setVisibility(View.GONE);
				meHolder.inEdit.setVisibility(View.VISIBLE);
			}
			else {
				label.setVisibility(View.VISIBLE);
				meHolder.inEdit.setVisibility(View.GONE);
			}
			break;

//		case VirtualAgentContinued:
//			label.setTextColor(vhaChatTextColor);
//			label.setTypeface(null, Typeface.NORMAL);
//			break;

		default:
			VhaRowHolder evaHolder = (VhaRowHolder) holder;
			if (chatItem.getSubLabel() == null) {
				evaHolder.subLabel.setVisibility(View.GONE);
				evaHolder.searchingProgress.setVisibility(View.GONE);
				//evaHolder.cruisesFoundIcon.setVisibility(View.GONE);
			}
			else {
				evaHolder.subLabel.setText(chatItem.getSubLabel());
				evaHolder.subLabel.setVisibility(View.VISIBLE);
				evaHolder.searchingProgress.setVisibility(chatItem.getStatus() == Status.InSearch ? View.VISIBLE : View.GONE);
				//evaHolder.cruisesFoundIcon.setVisibility(chatItem.getStatus() == Status.HAS_RESULTS ? View.VISIBLE :View.GONE);
			}

//			View right_pane = row.findViewById(R.id.right_pane);
//			ImageView topImg = (ImageView) row.findViewById(R.id.top_icon);
//			ProgressBar progress = (ProgressBar) row.findViewById(R.id.progressBar_search);
//			progress.setVisibility(View.GONE);
//			topImg.setVisibility(View.GONE);
//			FlowElement flow = chatItem.getFlowElement();
//			ImageView img = (ImageView) row.findViewById(R.id.icon);
			/*if (flow == null) {
				img.setImageResource(R.drawable.hotel72);
			}
			else {
				switch (flow.Type) {
				case Hotel:
					img.setImageResource(R.drawable.hotel_small_flag);
					//right_pane.setVisibility(View.VISIBLE);
					break;
//				case Flight:
//					img.setImageResource(R.drawable.airplane_small);
//					break;
				case Question:
					img.setImageResource(R.drawable.hotel72);
					right_pane.setVisibility(View.GONE);
					break;
				default:
					right_pane.setVisibility(View.GONE);
				}
			}*/
			/*
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
			}*/
			
//			if (chatItem.isInSession()) {
				label.setTextColor(vhaChatTextColor);
//			}
//			else {
//				label.setTextColor(vhaChatNoSessionText);
//			}
		}
		row.setTag(chatItem);
		return row;
	}
	
	private static class RowHolder {
		TextView label;
		View chatbubble;
	}
	
	public static class UserRowHolder extends RowHolder {
		public View inEdit;
		public EditText editText;
	}
	
	private static class VhaRowHolder extends RowHolder {
		// public ImageView hotelsFoundIcon;
		public ProgressBar searchingProgress;
		public TextView subLabel;
	}

}
