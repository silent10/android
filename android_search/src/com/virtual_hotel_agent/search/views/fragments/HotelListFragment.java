package com.virtual_hotel_agent.search.views.fragments;

import org.json.JSONObject;

import roboguice.event.EventManager;
import roboguice.event.Observes;
import roboguice.fragment.RoboFragment;
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
import com.google.inject.Inject;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelsListUpdated;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface;
import com.virtual_hotel_agent.search.controllers.web_services.ListContinuationDownloaderTask;
import com.virtual_hotel_agent.search.models.expedia.ExpediaRequestParameters;
import com.virtual_hotel_agent.search.views.adapters.HotelListAdapter;

// From Arik's app

public class HotelListFragment extends RoboFragment implements OnItemClickListener, DownloaderTaskListenerInterface {

	@Inject protected EventManager eventManager;
	
	ListContinuationDownloaderTask mContinuationLoader = null;
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

		mView = inflater.inflate(R.layout.fragment_hotel_list_portrait, container, false);
		mHotelListView = (ListView) mView.findViewById(R.id.hotelListView);
		mHotelListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mHotelListView.clearChoices();
		ExpediaRequestParameters rp = MyApplication.getExpediaRequestParams();
		if (rp != null) {
			mHotelListView.setSelection(rp.getHotelId());
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
		
		AnimationAdapter animAdapter =  new ScaleInAnimationAdapter(new SwingBottomInAnimationAdapter(mAdapter));
		animAdapter.setAbsListView(mHotelListView);
		mHotelListView.setAdapter(animAdapter);
		
		mHotelListView.setOnItemClickListener(this);
	}

	public void onEventHotelItemClicked( @Observes HotelItemClicked event) {
		
		final int hotelIndex = event.hotelIndex;
		
		mHotelListView.post(new Runnable() {
			@Override
			public void run() {
				mHotelListView.setItemChecked(hotelIndex, true);
			}
		});		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {		
		Log.d(TAG, "onItemClick "+position);

		HotelListAdapter.ViewHolder holder = (HotelListAdapter.ViewHolder) view.getTag();

		if (holder == null) {
			MainActivity.LogError(TAG, "Got null holder");
			return;
		}

		Tracker defaultTracker = GoogleAnalytics.getInstance(getActivity()).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("ui_action", "hotel_click", "hotel_list", (long) holder.getHotelIndex())
				    .build()
				   );
		
		eventManager.fire(new HotelItemClicked(holder.getHotelIndex()));
		Log.d(TAG, "running showHotelDetails()");

	}

	private boolean mEnabledPaging = false;

	private OnScrollListener mListScroll = new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if ((visibleItemCount > 0) && (firstVisibleItem + visibleItemCount == totalItemCount) 
					&& mContinuationLoader == null
					&& mEnabledPaging) {
				Log.d(TAG, "-Last Scroll-");

				String nextQuery = MyApplication.getDb().getNextQuery();
				mContinuationLoader = new ListContinuationDownloaderTask(HotelListFragment.this, nextQuery,
						SettingsAPI.getCurrencyCode(getActivity()));
				mContinuationLoader.execute();
			}
		}
	};
	@Override
	public void endProgressDialog(int id, JSONObject result) {
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

		if (!MyApplication.getDb().mMoreResultsAvailable) {
			mHotelListView.removeFooterView(mFooterView);
			mEnabledPaging = false;
		}
		
		eventManager.fire(new HotelsListUpdated());
	}

	ProgressDialog mProgressDialog = null;

	@Override
	public void endProgressDialogWithError(int id, JSONObject result) {
		Toast.makeText(getActivity(), "Error getting hotel information, please try again", Toast.LENGTH_LONG).show();

		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}

//		if (mDownLoader != null && id == mDownLoader.getId()) {
//			mDownLoader = null;
//		}
		if (mContinuationLoader != null && id== mContinuationLoader.getId()) {
			mContinuationLoader = null;
			
			if (MyApplication.getDb() == null) {
				
			}
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

	public void listResultUpdated() {
		if (mAdapter == null) {
			MainActivity.LogError(TAG, "Unexpected adapter is null");
			return;
		}
		Log.d(TAG, "Hotel list updated");
		mHotelListView.setSelectionAfterHeaderView();
		mHotelListView.clearChoices();
		mAdapter.notifyDataSetChanged();
	}
}
