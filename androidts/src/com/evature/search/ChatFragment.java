package com.evature.search;

import java.util.ArrayList;
import java.util.List;

import com.evaapis.SpeechRecognition;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioButton;

public class ChatFragment extends Fragment {

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.mainmenu, menu);
	}

	protected List<ChatItem> mChatListEva = new ArrayList<ChatItem>();
	protected ListView mChatListView;
	static final String TAG = "ChatFragment";

	public static Fragment newInstance() {
		ChatFragment f = new ChatFragment();
		return f;
	}

	// private ImageButton travel_search_button;

	RadioButton mRadioButtonEva;
	RadioButton mRadioButtonGoogle;
	
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
		mChatListView = (ListView) root.findViewById(R.id.chat_list);
		// Connect the data of the chat history to the view:
		mChatListView.setAdapter(new ChatAdapter(this, R.layout.row, R.id.label, mChatListEva));
		// travel_search_button = (ImageButton) root.findViewById(R.id.search_button);

		mRadioButtonGoogle =(RadioButton)root.findViewById(R.id.radioButtonGoogle);
		
		mRadioButtonGoogle.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				MainActivity activity= (MainActivity)getActivity();
				if(activity.getCurrentSpeechMethod()==SpeechRecognition.SPEECH_RECOGNITION_EVA)
				{
					activity.setCurrentSpeechMethod(SpeechRecognition.SPEECH_RECOGNITION_GOOGLE);
					mRadioButtonGoogle.setChecked(true);
					mRadioButtonEva.setChecked(false);
				}
				
			}
		});
		
		mRadioButtonEva =(RadioButton)root.findViewById(R.id.radioButtonEva);
		
		mRadioButtonEva.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				MainActivity activity= (MainActivity)getActivity();
				if(activity.getCurrentSpeechMethod()==SpeechRecognition.SPEECH_RECOGNITION_GOOGLE)
				{
					activity.setCurrentSpeechMethod(SpeechRecognition.SPEECH_RECOGNITION_EVA);
					mRadioButtonGoogle.setChecked(false);
					mRadioButtonEva.setChecked(true);
				}
				
			}
		});
		
		return root;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putParcelableArrayList("mChatListEva", (ArrayList<ChatItem>) mChatListEva);
	}

	// @Override
	// public void onActivityCreated(Bundle savedInstanceState) {
	// Log.d(TAG, "onActivityCreated");
	// super.onActivityCreated(savedInstanceState);
	// if (savedInstanceState != null) {
	// Log.d(TAG, "onActivityCreated savedInstanceState != null");
	// // Restore last state for checked position.
	// mChatListEva = savedInstanceState.getParcelableArrayList("mChatListEva");
	// ListView chatListView = (ListView) getView().findViewById(R.id.chat_list);
	// ((ChatAdapter) chatListView.getAdapter()).notifyDataSetChanged();
	// // mChatListView.setAdapter(new ChatAdapter(mainActivity, R.layout.row, R.id.label, mChatListEva));
	// }
	// }
}
