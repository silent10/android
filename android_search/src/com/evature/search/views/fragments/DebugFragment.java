package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.evature.search.R;

public class DebugFragment extends RoboFragment {
	static final String TAG = "DebugFragment";
	private static final int MAX_LENGTH = 2500;
	private TextView debugText = null;
	private String debugTextEva = "No reply yet.";
	private String debugTextVayant = "No reply yet.";
	private String debugTextExpedia = "No reply yet.";
	private RadioGroup radioButtons;
	
	
	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		View view = inflater.inflate(R.layout.fragment_eva_debug, container, false);
		radioButtons = (RadioGroup) view.findViewById(R.id.radioGroup_debug);
		debugText = (TextView)view.findViewById(R.id.eva_debug_text);
		if (savedInstanceState != null) {
			debugTextEva = savedInstanceState.getString("debugTextEva");
			debugTextVayant = savedInstanceState.getString("debugTextVayant");
			debugTextExpedia = savedInstanceState.getString("debugTextExpedia");
			int buttonId = savedInstanceState.getInt("debugTab");
			if (buttonId != 0)
				radioButtons.check(savedInstanceState.getInt("debugTab"));
		}
		
		//updateChecked();  removed for performance
		
		radioButtons.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				DebugFragment.this.updateChecked();
			}
		});
		
		return view;
	}
	
	private void updateChecked() {
		switch(radioButtons.getCheckedRadioButtonId()) {
		case R.id.radio_dbg_eva:
			debugText.setText(debugTextEva == null ? "No reply yet." : debugTextEva);
			break;
		case R.id.radio_dbg_vayant:
			debugText.setText(debugTextVayant == null ? "No reply yet." : debugTextVayant);
			break;
		case R.id.radio_dbg_expedia:
			debugText.setText(debugTextExpedia == null ? "No reply yet." : debugTextExpedia);
			break;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle instanceState) {
		try {
			super.onSaveInstanceState(instanceState);
			instanceState.putString("debugTextEva", debugTextEva);
			instanceState.putString("debugTextVayant", debugTextVayant);
			instanceState.putString("debugTextExpedia", debugTextExpedia);
			instanceState.putInt("debugTab", radioButtons.getCheckedRadioButtonId());
		}
		catch(Exception e) {
			Log.e(TAG, "Exception while saving instance state in DebugFragment", e);
		}
	}

	
	
	public void setDebugText(String text) {
		if (text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH);
		}
		if (debugText != null) {
			debugText.setText(text);
			debugTextEva = text;
			radioButtons.check(R.id.radio_dbg_eva);
		}
		else
			debugTextEva = text;
	}
	
	public void setVayantDebugText(String text) {
		if (text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH);
		}
		if (debugText != null) {
			debugText.setText(text);
			debugTextVayant = text;
			radioButtons.check(R.id.radio_dbg_vayant);
		}
		else
			debugTextVayant = text;
	}

	public void setExpediaDebugText(String text) {
		if (text.length() > MAX_LENGTH) {
			text = text.substring(0, MAX_LENGTH);
		}
		if (debugText != null) {
			debugText.setText(text);
			debugTextExpedia = text;
			radioButtons.check(R.id.radio_dbg_expedia);
		}
		else
			debugTextExpedia = text;
	}
}
