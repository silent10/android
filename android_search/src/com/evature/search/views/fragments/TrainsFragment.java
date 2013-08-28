package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.views.adapters.TrainListAdapter;

public class TrainsFragment extends RoboFragment implements OnClickListener, OnItemClickListener, OnKeyListener {

	// private LinearLayout mFooterView;
	View mView;
	ListView mTrainListView;
	private TrainListAdapter mAdapter;
	private static final String TAG = "TrainsFragment";

	@Override
	public void onDestroy() {
		Log.d(TAG, "onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		Log.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i(TAG, "onStop");
		super.onStop();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.train_list_portrait, container, false);
		mTrainListView = (ListView) mView.findViewById(R.id.trainListView);
		// EvaDatabase evaDatabase = MyApplication.getDb();
		// mAdapter = new TrainListAdapter(this, evaDatabase);
		// mTrainListView.setAdapter(mAdapter);
		// mTrainListView.setOnItemClickListener(this);
		setAdapter();
		return mView;
	}

	void setAdapter() {
		if (MyApplication.getDb() != null) {
			// if (MyApplication.getDb().mMoreResultsAvailable) {
			// LayoutInflater li = getActivity().getLayoutInflater();
			// mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
			// mTrainListView.addFooterView(mFooterView);
			// }
			mAdapter = new TrainListAdapter(this);
			mTrainListView.setAdapter(mAdapter);
			mTrainListView.setOnItemClickListener(this);
		}
	}


	@Override
	public void onClick(View v) {

		Log.i(TAG, "onClick");

		onItemClick(null, v, 0, 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		Log.d(TAG, "onItemClick");
		Log.e(TAG, "Please implement showTrainDetails");
		// TrainListAdapter.ViewHolder holder = (TrainListAdapter.ViewHolder) v.getTag();
		//
		// if (holder == null) {
		// Log.e(TAG, "Got null holder");
		// return;
		// }
		// ((MainActivity) getActivity()).showTrainDetails(holder.index);
	}

	ProgressDialog mProgressDialog = null;

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	public TrainListAdapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(TrainListAdapter mAdapter) {
		this.mAdapter = mAdapter;
	}

	// public void updateDisplay() {
	// // Do whatever is needed to update the display
	// TrainListAdapter adapter = (TrainListAdapter) mTrainListView.getAdapter(); // crash -
	// // ClassCastException:
	// // android.widget.HeaderViewListAdapter
	// // cannot be cast to
	// // com.evature.search.TrainListAdapter
	// EvaDatabase evaDatabase = MyApplication.getDb();
	// adapter.setData(evaDatabase);
	// adapter.notifyDataSetChanged();
	// mTrainListView.setOnItemClickListener(this);
	//
	// }
}
