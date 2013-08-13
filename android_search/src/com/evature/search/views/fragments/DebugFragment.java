package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.evature.search.R;

public class DebugFragment extends RoboFragment {
	static final String TAG = "DebugFragment";
	private TextView debugText = null;
	String debugTextStr = "";
	
	
	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		View view = inflater.inflate(R.layout.fragment_eva_debug, container, false);
		debugText = (TextView)view.findViewById(R.id.eva_debug_text);
		if (savedInstanceState != null) {
			debugTextStr = savedInstanceState.getString("debugText", "");
		}
		debugText.setText(debugTextStr);
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		try {
			super.onSaveInstanceState(instanceState);
			instanceState.putString("debugText", debugTextStr);
		}
		catch(Exception e) {
			Log.e(TAG, "Exception while saving instance state in DebugFragment", e);
		}
	}

	
	public void setDebugText(String text) {
		if (debugText != null) {
			debugText.setText(text);
			debugTextStr = text;
		}
		else
			debugTextStr = text;
	}

}
