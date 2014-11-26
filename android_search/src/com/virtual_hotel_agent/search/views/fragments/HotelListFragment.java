package com.virtual_hotel_agent.search.views.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelsListUpdated;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface;
import com.virtual_hotel_agent.search.controllers.web_services.ListContinuationDownloaderTask;
import com.virtual_hotel_agent.search.views.adapters.HotelListAdapter;

import de.greenrobot.event.EventBus;

public class HotelListFragment extends Fragment implements OnItemClickListener, DownloaderTaskListenerInterface {


	private EventBus eventBus;
	ListContinuationDownloaderTask mContinuationLoader = null;
	private LinearLayout mFooterView;
	boolean mClickEnabled = true;
	View mView;
	ListView mHotelListView;
	private HotelListAdapter mAdapter;
	private final String TAG = "HotelListFragment";

	private AnimationAdapter mAnimAdapter;

	@Override
	public void onDestroyView() {
		Log.d(TAG, "onDestroyView");
		super.onDestroyView();
	}
	
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
	        	((MainActivity)getActivity()).hotelsFragmentVisible();
	        }
	    }
	    else {
	    	Log.w(TAG, "hint called while not visible?");
	    }
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");

		if (mContinuationLoader != null) {
			mContinuationLoader.detach();
			mContinuationLoader.cancel(true);
			mContinuationLoader = null;
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
		super.onCreate(savedInstanceState);
		
		eventBus = EventBus.getDefault();
		Context context = getActivity();
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "Hotels Screen")
				    .build()
				);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mView != null) {
			Log.w(TAG, "Fragment initialized twice");
			if (mView.getParent() != null)
				((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		eventBus.register(this);		
		mView = inflater.inflate(R.layout.fragment_hotel_list_portrait, container, false);
		mHotelListView = (ListView) mView.findViewById(R.id.hotelListView);
		mHotelListView.clearChoices();
		if (VHAApplication.selectedHotel != null) {
			mHotelListView.setSelection(VHAApplication.FOUND_HOTELS.indexOf(VHAApplication.selectedHotel));
//			mHotelListView.requestFocus();
		}
		if (mContinuationLoader != null) {
			mContinuationLoader.detach();
			mContinuationLoader.cancel(true);
			mContinuationLoader = null;
		}
		
		setAdapter();
		// mCheckQueryLength.sendEmptyMessageDelayed(0, 500);

		return mView;
	}

	private void setAdapter() {
		
		if (mEnabledPaging && mFooterView != null)
			mHotelListView.removeFooterView(mFooterView);

		mEnabledPaging = false;

		if (VHAApplication.FOUND_HOTELS.size() > 0) {

			if (VHAApplication.cacheLocation != null && VHAApplication.moreResultsAvailable) {
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
		
		mAnimAdapter =  new ScaleInAnimationAdapter(new SwingBottomInAnimationAdapter(mAdapter));
		mAnimAdapter.setAbsListView(mHotelListView);
		mHotelListView.setAdapter(mAnimAdapter);
		
		mHotelListView.setOnItemClickListener(this);
	}

	public void onEvent( HotelItemClicked event) {
		
		final int hotelIndex = event.hotelIndex;
		if (mHotelListView.getCheckedItemPosition() != hotelIndex) {
			mHotelListView.post(new Runnable() {
				@Override
				public void run() {
					mHotelListView.setItemChecked(hotelIndex, true);
				}
			});
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {		
		Log.d(TAG, "onItemClick "+position);


		Tracker defaultTracker = GoogleAnalytics.getInstance(getActivity()).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("ui_action", "hotel_click", "hotel_list", (long) position)
				    .build()
				   );
		
//		mHotelListView.setItemChecked(position, true);
		mHotelListView.setSelection(VHAApplication.FOUND_HOTELS.indexOf(VHAApplication.selectedHotel));
		//mHotelListView.requestFocus();
		eventBus.post(new HotelItemClicked(position));
		Log.d(TAG, "running showHotelDetails()");

	}

	private boolean mEnabledPaging = false;

	private OnScrollListener mListScroll = new OnScrollListener() {

		private final int distanceFromLastPositionToLoad = 5;
		
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if ((visibleItemCount > 0) && (firstVisibleItem + visibleItemCount >= totalItemCount - distanceFromLastPositionToLoad) 
					&& mContinuationLoader == null
					&& mEnabledPaging) {
				Log.d(TAG, "-Last Scroll-");

				//String nextQuery = MyApplication.getDb().getNextQuery();
				mContinuationLoader = new ListContinuationDownloaderTask(HotelListFragment.this, 
						SettingsAPI.getCurrencyCode(getActivity()));
				mContinuationLoader.execute();
			}
		}
	};
	@Override
	public void endProgressDialog(int id, Object result) {
//		if (mDownLoader != null && id == mDownLoader.getId()) {
//			mDownLoader = null;
//		}
		if (mContinuationLoader != null && id== mContinuationLoader.getId()) {
			mContinuationLoader = null;
		}

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

		if (!VHAApplication.moreResultsAvailable) {
			mHotelListView.removeFooterView(mFooterView);
			mEnabledPaging = false;
		}
		
		eventBus.post(new HotelsListUpdated());
	}

	ProgressDialog mProgressDialog = null;

	@Override
	public void endProgressDialogWithError(int id, Object result) {
		Toast.makeText(getActivity(), "Error getting hotels information, please try again", Toast.LENGTH_LONG).show();

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

//		if (mDownLoader != null && id == mDownLoader.getId()) {
//			mDownLoader = null;
//		}
		if (mContinuationLoader != null && id== mContinuationLoader.getId()) {
			mContinuationLoader = null;
			
			// TODO: check error type
			// error may be because too much time has passed - so cache will not work
			((MainActivity) getActivity()).clearExpediaCache();
		}

		
	}

	@Override
	public void startProgressDialog(int id) {
		if (id == R.string.HOTEL) {
//			if (mDownLoader == null) {
//				Log.w(TAG, "expected hotel downloader to run");
//				return;
//			}
			mProgressDialog = ProgressDialog.show(getActivity(), "Getting Hotel Information",
					"Contacting search server", true, false);
		}
	}

	@Override
	public void updateProgress(int id, DownloaderStatus progress) {

		Log.i(TAG, "Update progress "+progress);
		if (mContinuationLoader != null && id == mContinuationLoader.getId()) {
			switch (progress) {
			case MadeSomeProgress:
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
				break;
			
			}
		}
	}

	public void newHotelsList() {
		if (mAdapter == null) {
			VHAApplication.logError(TAG, "Unexpected adapter is null");
			return;
		}
		Log.d(TAG, "New Hotel list updated to size "+mAdapter.getCount());
		mAnimAdapter.reset();
		mHotelListView.clearChoices();
		mHotelListView.setScrollY(0);
		setAdapter(); // for some reason starting a new adapter is the only way to scroll to top
		//mAdapter.notifyDataSetChanged();
	}

}
