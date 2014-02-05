package com.virtual_hotel_agent.search.views.fragments;

import roboguice.fragment.RoboFragment;
import roboguice.util.Ln;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.virtual_hotel_agent.search.R;

public class ExamplesFragment extends RoboFragment { // TODO: change to ListFragment ?
	static final String TAG = "ExamplesFragment";
	
	public interface ExampleClickedHandler {
		public void onClick(String example);
	}

	ExampleClickedHandler  clickHandler;
	private String[] mExamples;
	
	private ListView mExamplesListView;
	

	public void setHandler(ExampleClickedHandler clickHandler) {
		this.clickHandler = clickHandler;		
	}

	// private ImageButton travel_search_button;	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Ln.d("onCreateView");
		
		try {
			mExamples = getResources().getStringArray(R.array.examples);
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
