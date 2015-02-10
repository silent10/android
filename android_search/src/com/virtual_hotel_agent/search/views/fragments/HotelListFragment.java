package com.virtual_hotel_agent.search.views.fragments;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.internal.widget.AdapterViewCompat;
import android.support.v7.internal.widget.AdapterViewCompat.OnItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.evature.util.DLog;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelsListUpdated;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface;
import com.virtual_hotel_agent.search.controllers.web_services.ListContinuationDownloaderTask;
import com.virtual_hotel_agent.search.views.adapters.HotelListAdapter;
import com.virtual_hotel_agent.search.views.adapters.HotelListAdapter.OnHotelClickListener;

import de.greenrobot.event.EventBus;

public class HotelListFragment extends Fragment implements OnHotelClickListener, DownloaderTaskListenerInterface {


	private EventBus eventBus;
	ListContinuationDownloaderTask mContinuationLoader = null;
	private View mFooterView;
	boolean mClickEnabled = true;
	View mView;
	RecyclerView mHotelListView;
	private HotelListAdapter mAdapter;
	private final String TAG = "HotelListFragment";

	//private AnimationAdapter mAnimAdapter;
	private StaggeredGridLayoutManager mLayoutManager;

	@Override
	public void onDestroyView() {
		DLog.d(TAG, "onDestroyView");
		super.onDestroyView();
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
	    super.setUserVisibleHint(isVisibleToUser);

	    // Make sure that we are currently visible
	    if (this.isVisible()) {
	        // If we are becoming invisible, then...
	        if (!isVisibleToUser) {
	            DLog.i(TAG, "Not visible anymore.");
	        }
	        else {
	        	DLog.i(TAG, "Becoming visible. Starting audio.");
	        	((MainActivity)getActivity()).hotelsFragmentVisible();
	        }
	    }
	    else {
	    	DLog.w(TAG, "hint called while not visible?");
	    }
	}

	@Override
	public void onPause() {
		DLog.d(TAG, "onPause");

		if (mContinuationLoader != null) {
			mContinuationLoader.detach();
			mContinuationLoader.cancel(true);
			mContinuationLoader = null;
		}
		
		super.onPause();
	}

	@Override
	public void onResume() {
		DLog.d(TAG, "onResume");
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		DLog.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		DLog.d(TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		DLog.i(TAG, "onStop");
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
			DLog.w(TAG, "Fragment initialized twice");
			if (mView.getParent() != null)
				((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		eventBus.register(this);		
		mView = inflater.inflate(R.layout.fragment_hotel_list_portrait, container, false);
		mHotelListView = (RecyclerView) mView.findViewById(R.id.hotelListView);
		mFooterView = mView.findViewById(R.id.hotelListFooter);
		mHotelListView.setHasFixedSize(true);
		
		// use a linear layout manager
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mHotelListView.setLayoutManager(mLayoutManager);
		
		//mHotelListView.clearChoices();
//		if (VHAApplication.selectedHotel != null) {
//			mHotelListView.setSelection(VHAApplication.FOUND_HOTELS.indexOf(VHAApplication.selectedHotel));
////			mHotelListView.requestFocus();
//		}
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
		
		mFooterView.setVisibility(View.GONE);
		
		mEnabledPaging = false;

		if (VHAApplication.FOUND_HOTELS.size() > 0) {

			if (VHAApplication.cacheLocation != null && VHAApplication.moreResultsAvailable) {
				if (getActivity() != null) {
					LayoutInflater li = getActivity().getLayoutInflater();
					mHotelListView.setOnScrollListener(mListScroll);
					mEnabledPaging = true;
				}
			}
		}

		if (mAdapter == null) {
			mAdapter = new HotelListAdapter(this);
			mHotelListView.setAdapter(mAdapter);
		}
		else {
			mAdapter.notifyDataSetChanged();
		}
	}
	
	public void setTargetLocation(Location location) {
		if (mAdapter != null && location != null) {
			mAdapter.setTargetLocation(location);
			mAdapter.notifyDataSetChanged();
		}
	}

	public void onEvent( HotelItemClicked event) {
		
//		final int hotelIndex = event.hotelIndex;
//		if (mHotelListView.getCheckedItemPosition() != hotelIndex) {
//			mHotelListView.post(new Runnable() {
//				@Override
//				public void run() {
//					mHotelListView.setItemChecked(hotelIndex, true);
//				}
//			});
//		}
	}

	@Override
	public void onHotelClick(int position, View view) {
		DLog.d(TAG, "onHotelClick "+position);


		Tracker defaultTracker = GoogleAnalytics.getInstance(getActivity()).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("ui_action", "hotel_click", "hotel_list", (long) position)
				    .build()
				   );
		
//		mHotelListView.setItemChecked(position, true);
		//mHotelListView.setSelection(VHAApplication.FOUND_HOTELS.indexOf(VHAApplication.selectedHotel));
		//mHotelListView.requestFocus();
		if (position >= VHAApplication.FOUND_HOTELS.size()) {
			DLog.e(TAG, "position "+position+" is greater than "+VHAApplication.FOUND_HOTELS.size()+ " found hotels");
			return;
		}
		eventBus.post(new HotelItemClicked(position,
							VHAApplication.FOUND_HOTELS.get(position).hotelId,
							view.findViewById(R.id.hotelName),
							//view.findViewById(R.id.tripAdvisorStrip),
							view.findViewById(R.id.rating)));
		DLog.d(TAG, "running showHotelDetails()");

	}

	private boolean mEnabledPaging = false;

	private OnScrollListener mListScroll = new OnScrollListener() {

		private final int distanceFromLastPositionToLoad = 10;

		@Override
		public void	onScrolled(RecyclerView recyclerView, int dx, int dy) {
			int [] positions = mLayoutManager.findLastVisibleItemPositions(null);
			int maxPosition = positions[0];
			if (positions[1] > maxPosition) { 
				maxPosition = positions[1]; 
			}
			int totalItemCount = mAdapter.getItemCount();
			if ((maxPosition > 0) && (maxPosition >= totalItemCount - distanceFromLastPositionToLoad) 
					&& mContinuationLoader == null
					&& mEnabledPaging) {
				DLog.d(TAG, "-Last Scroll-");
				mFooterView.setVisibility(View.VISIBLE);
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

		mFooterView.setVisibility(View.GONE);
		
		if (!VHAApplication.moreResultsAvailable) {
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

		DLog.i(TAG, "Update progress "+progress);
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

	public void newHotelsList(Location targetLocation) {
		if (mAdapter == null) {
			DLog.e(TAG, "Unexpected adapter is null");
			return;
		}
		DLog.d(TAG, "New Hotel list updated to size "+(mAdapter.getItemCount()-1));
		//mAnimAdapter.reset();
		//mHotelListView.clearChoices();
		setAdapter();
		setTargetLocation(targetLocation);
		//mAdapter.notifyDataSetChanged();
	}


}
