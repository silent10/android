package com.evature.search.views.fragments;

import java.util.ArrayList;
import java.util.List;

import com.evature.search.R;
import com.evature.search.R.id;
import com.evature.search.R.layout;
import com.evature.search.R.menu;
import com.evature.search.models.ChatItem;
import com.evature.search.views.adapters.ChatAdapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class ChatFragment extends Fragment {

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainmenu, menu);
	}

	protected List<ChatItem> mChatListEva = new ArrayList<ChatItem>();
	private ListView mChatListView;
	static final String TAG = "ChatFragment";

	public static Fragment newInstance() {
		ChatFragment f = new ChatFragment();
		return f;
	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		if (savedInstanceState != null) {
			Log.d(TAG, "onCreateView savedInstanceState != null");
			// Restore last state for checked position.
			mChatListEva = savedInstanceState.getParcelableArrayList("mChatListEva");
		}
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat, null);
		// MainActivity mainActivity = (MainActivity) getActivity();
		setChatListView((ListView) root.findViewById(R.id.chat_list));
		// Connect the data of the chat history to the view:
		getChatListView().setAdapter(new ChatAdapter(this, R.layout.row, R.id.label, mChatListEva));
		// travel_search_button = (ImageButton) root.findViewById(R.id.search_button);

		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelableArrayList("mChatListEva", (ArrayList<ChatItem>) mChatListEva);
	}

	public ListView getChatListView() {
		return mChatListView;
	}

	public void setChatListView(ListView mChatListView) {
		this.mChatListView = mChatListView;
	}

}
