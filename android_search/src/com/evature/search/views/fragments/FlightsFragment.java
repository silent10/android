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
import android.widget.TextView;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.views.adapters.FlightListAdapter;

public class FlightsFragment extends RoboFragment implements OnClickListener, OnItemClickListener, OnKeyListener {

	// private LinearLayout mFooterView;
	View mView;
	ListView mFlightListView;
	private FlightListAdapter mAdapter;
	private static final String TAG = "FlightsFragment";

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    // Make sure that we are currently visible
	    if (this.isVisible()) {
	        // If we are becoming invisible, then...
	        if (!isVisibleToUser) {
	            Log.i(TAG, "Not visible anymore.");
	        }
	        else {
	        	Log.i(TAG, "Becoming visible. Starting audio.");
	        	((MainActivity)getActivity()).flightsFragmentVisible();
	        }
	    }
	    else {
	    	Log.w(TAG, "hint called while not visible?");
	    }
	}
	
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

		mView = inflater.inflate(R.layout.flight_list_portrait, container, false);
		TextView departure_date = (TextView) mView.findViewById(R.id.search_results_departure_date);
		departure_date.setText("Departure");
		TextView arrival_date = (TextView) mView.findViewById(R.id.search_results_arrival_date);
		arrival_date.setText("Arrival");

		mFlightListView = (ListView) mView.findViewById(R.id.flightListView);
		// EvaDatabase evaDatabase = MyApplication.getDb();
		// mAdapter = new FlightListAdapterTP(this, evaDatabase);
		// mFlightListView.setAdapter(mAdapter);
		// mFlightListView.setOnItemClickListener(this);
		setAdapter();
		return mView;
	}

	void setAdapter() {
		if (MyApplication.getJourneyDb() != null) {

			// if (MyApplication.getDb().mMoreResultsAvailable) {
			// LayoutInflater li = getActivity().getLayoutInflater();
			// mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
			// mFlightListView.addFooterView(mFooterView);
			// }

			mAdapter = new FlightListAdapter(this, MyApplication.getJourneyDb().mJourneys);
			mFlightListView.setAdapter(mAdapter);
			mFlightListView.setOnItemClickListener(this);
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
		Log.e(TAG, "Please implement showFlightDetails");
		// FlightListAdapterTP.ViewHolder holder = (FlightListAdapterTP.ViewHolder) v.getTag();
		//
		// if (holder == null) {
		// Log.e(TAG, "Got null holder");
		// return;
		// }
		// ((MainActivity) getActivity()).showFlightDetails(holder.index);
	}

	ProgressDialog mProgressDialog = null;

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		return false;
	}

	public FlightListAdapter getAdapter() {
		return mAdapter;
	}


	// public void updateDisplay() {
	// // Do whatever is needed to update the display
	// Log.d(TAG, "updateDisplay()");
	// mView = getActivity().getLayoutInflater().inflate(R.layout.flight_list_portrait, null);
	// TextView departure_date = (TextView) mView.findViewById(R.id.search_results_departure_date);
	// departure_date.setText("asdasd");
	// mView.postInvalidate();
	// mFlightListView = (ListView) mView.findViewById(R.id.flightListView);
	// try {
	// FlightListAdapterTP mAdapter = (FlightListAdapterTP) mFlightListView.getAdapter();
	// EvaDatabase evaDatabase = MyApplication.getDb();
	// if (mAdapter != null && evaDatabase != null) {
	// mAdapter.setData(evaDatabase);
	// mAdapter.notifyDataSetChanged();
	// } else {
	// mAdapter = new FlightListAdapterTP(this, evaDatabase);
	// mFlightListView.setAdapter(mAdapter);
	// mFlightListView.setOnItemClickListener(this);
	// setAdapter();
	// }
	// } catch (ClassCastException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// // ClassCastException:
	// // android.widget.HeaderViewListAdapter
	// // cannot be cast to
	// // com.evature.search.FlightListAdapterTP
	// mFlightListView.setOnItemClickListener(this);
	//
	// }
}
