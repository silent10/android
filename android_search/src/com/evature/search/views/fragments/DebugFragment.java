package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
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
			Log.i(TAG, "Loading from savedInstanceState: "+debugTextStr);
		}
		debugText.setText(debugTextStr);
		return view;
	}
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		Log.d(TAG, "onSaveInstanceState saving "+debugTextStr);
		super.onSaveInstanceState(instanceState);
		instanceState.putString("debugText", debugTextStr);
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
