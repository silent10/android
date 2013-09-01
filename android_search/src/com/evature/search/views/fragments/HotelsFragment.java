package com.evature.search.views.fragments;

import roboguice.event.EventManager;
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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.controllers.events.HotelsListUpdated;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaListContinuationDownloaderTask;
import com.evature.search.controllers.web_services.HotelListDownloaderTask;
import com.evature.search.views.adapters.HotelListAdapter;
import com.google.inject.Inject;

// From Arik's app

public class HotelsFragment extends RoboFragment implements OnClickListener, OnItemClickListener, OnKeyListener,
		EvaDownloaderTaskInterface {

	@Inject protected EventManager eventManager;
	
	HotelListDownloaderTask mDownLoader = null;
	EvaListContinuationDownloaderTask mContinuationLoader = null;
	private LinearLayout mFooterView;
	boolean mClickEnabled = true;
	View mView;
	ListView mHotelListView;
	private HotelListAdapter mAdapter;
	private final String TAG = "HotelListFragment";

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");

		if (mContinuationLoader != null) {
			mContinuationLoader.detach();
			mContinuationLoader.cancel(true);
			mContinuationLoader = null;
		}
		
		if (mDownLoader != null) {
			mDownLoader.detach();
			mDownLoader.cancel(true);
			mDownLoader = null;
		}
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

		if (mDownLoader != null) {
			mProgressDialog = ProgressDialog.show(getActivity(), "Getting Hotel Information",
					"Contacting search server", true, false);

			mDownLoader.attach(this);
		}

		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.hotel_list_portrait, container, false);
		mHotelListView = (ListView) mView.findViewById(R.id.hotelListView);

		setAdapter();
		// mCheckQueryLength.sendEmptyMessageDelayed(0, 500);

		return mView;
	}

	void setAdapter() {
		
		if (mEnabledPaging && mFooterView != null)
			mHotelListView.removeFooterView(mFooterView);

		mEnabledPaging = false;
		mPaging = false;

		if (MyApplication.getDb() != null) {

			if (MyApplication.getDb().mMoreResultsAvailable) {
				if (getActivity() != null) {
					LayoutInflater li = getActivity().getLayoutInflater();
					mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
					mHotelListView.addFooterView(mFooterView);
					mHotelListView.setOnScrollListener(mListScroll);
					mEnabledPaging = true;
				}
			}
		}

		mAdapter = new HotelListAdapter(this);
		mHotelListView.setAdapter(mAdapter);
		mHotelListView.setOnItemClickListener(this);
	}

	@Override
	public void onClick(View v) {

		Log.i(TAG, "Stam");

		onItemClick(null, v, 0, 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		Log.d(TAG, "onItemClick");

		HotelListAdapter.ViewHolder holder = (HotelListAdapter.ViewHolder) v.getTag();

		if (holder == null) {
			Log.e(TAG, "Got null holder");
			return;
		}

		((MainActivity) getActivity()).showHotelDetails(holder.getHotelIndex());
		Log.d(TAG, "running showHotelDetails()");

	}

	private boolean mPaging = false;
	private boolean mEnabledPaging = false;

	private OnScrollListener mListScroll = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if ((visibleItemCount > 0) && (firstVisibleItem + visibleItemCount == totalItemCount) && !mPaging
					&& mEnabledPaging) {
				mPaging = true;
				Log.d(TAG, "-Last Scroll-");

				String nextQuery = MyApplication.getDb().getNextQuery();
				mContinuationLoader = new EvaListContinuationDownloaderTask(HotelsFragment.this, nextQuery,
						EvaSettingsAPI.getCurrencyCode(getActivity()));
				mContinuationLoader.execute();
			}
		}
	};

	@Override
	public void endProgressDialog(int id, String result) {
		mDownLoader = null;

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	ProgressDialog mProgressDialog = null;

	@Override
	public void endProgressDialogWithError(int id, String result) {
		Toast.makeText(getActivity(), "Error getting hotel information, please try again", Toast.LENGTH_LONG).show();

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

		mDownLoader = null;

	}

	@Override
	public void startProgressDialog(int id) {
		if (id == R.string.HOTEL) {
			if (mDownLoader == null) {
				Log.w(TAG, "expected hotel downloader to run");
				return;
			}
			mProgressDialog = ProgressDialog.show(getActivity(), "Getting Hotel Information",
					"Contacting search server", true, false);
		}
	}

	@Override
	public void updateProgress(int id, DownloaderStatus progress) {

		Log.i(TAG, "Update progress");

		if (id == R.string.HOTELS) {
			if (mContinuationLoader == null) {
				Log.w(TAG, "expected continuous downloader to run");
			}
			if (getAdapter() != null) {
				getAdapter().notifyDataSetChanged();
			} else {
				return;
			}
	
			if (!MyApplication.getDb().mMoreResultsAvailable) {
				mHotelListView.removeFooterView(mFooterView);
				mEnabledPaging = false;
			}
			
			eventManager.fire(new HotelsListUpdated());
		}
	}

	public void finishPaging() {
		mPaging = false;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public HotelListAdapter getAdapter() {
		return mAdapter;
	}

}
