package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.evature.search.R;
import com.evature.search.models.chat.ChatItemList;

public class ExamplesFragment extends RoboFragment { // TODO: change to ListFragment ?
	static final String TAG = "ExamplesFragment";
	
	public interface ExampleClickedHandler {
		public void onClick(String example);
	}

	ExampleClickedHandler  clickHandler;
	private String[] mExamples;
	
	private ListView mExamplesListView;
	

	public void setExamples(String[] examples) {
		mExamples = examples;
	}
	
	public void setHandler(ExampleClickedHandler clickHandler) {
		this.clickHandler = clickHandler;		
	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView");
		
		try {
			ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_examples, null);
			mExamplesListView = ((ListView) root.findViewById(R.id.examples_list));
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1,  mExamples);
			mExamplesListView.setAdapter(adapter);
			mExamplesListView.setOnItemClickListener(new OnItemClickListener() {
				
				@Override
				public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
					String example = mExamples[position];
					clickHandler.onClick(example);
				}
			});
			return root;
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
