package com.virtual_hotel_agent.search.views;

import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
//import android.view.PagerAdapter;
//import android.view.ViewPager;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evaapis.EvaException;
import com.evaapis.android.EvaSpeechComponent;
import com.evaapis.android.EvaSpeechComponent.SpeechRecognitionResultListener;
import com.evaapis.android.SoundLevelView;
import com.evaapis.android.SpeechAudioStreamer;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.views.fragments.BookingFragement;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelDetailFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelsMapFragment;
import com.virtual_hotel_agent.search.views.fragments.ReservationDisplayFragment;
import com.virtual_hotel_agent.search.views.fragments.ReviewsFragment;
import com.virtual_hotel_agent.search.views.fragments.RoomsSelectFragement;

/****
 *  User interface parts of the MainActivity 
 */
public class MainView {

	private static final String TAG = "MainView";
	private View mStatusPanel;
	private TextView mStatusText;
	private ProgressBar mProgressBar;
	private SoundLevelView mSoundView;
	private ImageButton mSearchButton;
	public ImageButton startNewSessionButton;
//	private ImageButton mTextButton;
	private View mBottomBar;
	private final int search_button_padding = 24;

	private WeakReference<Handler> mUpdateLevel;
//	private ViewPager mViewPager;
	private TabsPagerAdapter mTabsAdapter;
	
	private String mChatTabName;
//	private String mExamplesTabName;
	private String mHotelsTabName;
	private String mHotelTabName;
	private String mRoomsTabName;
	private String mBookingTabName;
	private String mMapTabName;
	private String mReservationsTabName;
	private String mReviewsTabName;
	private List<String> mTabTitles;
	
	public MainView(final MainActivity mainActivity, List<String> tabTitles) {
		mStatusPanel = mainActivity.findViewById(R.id.status_panel);
		mStatusText = (TextView)mainActivity.findViewById(R.id.text_listeningStatus);
		mProgressBar = (ProgressBar)mainActivity.findViewById(R.id.progressBar1);
		mSoundView = (SoundLevelView)mainActivity.findViewById(R.id.surfaceView_sound_wave);
		mSearchButton = (ImageButton) mainActivity.findViewById(R.id.search_button);
		mBottomBar = mainActivity.findViewById(R.id.bottom_bar);
		startNewSessionButton = (ImageButton) mainActivity.findViewById(R.id.restart_button);
//		mViewPager = (ViewPager) mainActivity.findViewById(R.id.viewpager);
		
		mChatTabName = mainActivity.getString(R.string.CHAT);
//		mExamplesTabName = mainActivity.getString(R.string.EXAMPLES);
		//mDebugTabName = mainActivity.getString(R.string.DEBUG);
		mHotelsTabName = mainActivity.getString(R.string.HOTELS);
		mHotelTabName = mainActivity.getString(R.string.HOTEL);
		mRoomsTabName = mainActivity.getString(R.string.ROOMS);
		mBookingTabName = mainActivity.getString(R.string.BOOKING);
		mReservationsTabName = mainActivity.getString(R.string.RESERVATIONS);
		mMapTabName = mainActivity.getString(R.string.MAP);
		mReviewsTabName = mainActivity.getString(R.string.REVIEWS);
		
		mTabTitles = tabTitles;
		
		mChatFragment = new ChatFragment();
		mainActivity.getFragmentManager().beginTransaction()
        		.add(R.id.fragment_container, getChatFragment())
        		.commit();
		
		// setup the tab switching
//		mTabsAdapter = new TabsPagerAdapter(mainActivity.getFragmentManager());
//		mViewPager.setAdapter(mTabsAdapter);
//		mTabs.setViewPager(mViewPager);
//		mViewPager.setOffscreenPageLimit(5);
		
		mSoundView.setColor(0xffdd8877);
		mSoundView.setAlign(Gravity.RIGHT);

//		mTabs.setOnPageChangeListener(new OnPageChangeListener() {
//			private boolean lastShown = true;
//			private boolean hidButtons = false;
//			
//			@Override
//			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			}
//	
//			@Override
//			public void onPageSelected(int position) {
//				mainActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(position > 0);
//				if (position > 2) {
//					if (!hidButtons) {
//						// save last shown on first hiding of buttons
//						lastShown = areMainButtonsShown();
//					}
//					toggleMainButtons(false);
//					hidButtons = true;
//				}
//				else {
//					if (lastShown && hidButtons) {
//						toggleMainButtons(true);
//						hidButtons = false;
//						lastShown = true;
//					}
//				}
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int arg0) {
//				
//			}
//		});
//		mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}
	
	public void activateSearchButton() {
		Log.d(TAG, "activate search button");
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.startTransition(100);
		    }
		});
	}
	
	public void flashSearchButton(final int times) {
		if (times <= 0) {
			return;
		}
		if (times == 1) {
			Log.d(TAG, "flash search button");
		}
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.startTransition(250);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
				      // reverse the transition after it completes
				    	mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
						mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				    	TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				    	drawable.reverseTransition(250);
				    	
				    	mSearchButton.postDelayed(new Runnable() {
						    @Override
						    public void run() {
						    	flashSearchButton(times-1);
						    }
				    	}, 260);
				    }
				}, 260);
		    }
		});
	}
	
	public void disableSearchButton() {
		Log.d(TAG, "disable search button");
		mSearchButton.post(new Runnable() {
			@Override
			public void run() {
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.reverseTransition(50);
				mSearchButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						mSearchButton.setBackgroundResource(R.drawable.transition_button_dectivate);
						mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
						TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
						drawable.startTransition(50);
					}
				}, 60);
			}
		});
	}
	
	public void deactivateSearchButton() {
		Log.d(TAG, "deactivate search button");
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.reverseTransition(100);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
				    	mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				    	mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				    	TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				    	drawable.resetTransition();
				    }
				}, 110);
		    }
		});
	}
	
	
	public void flashBadSearchButton(final int times) {
		if (times <= 0) {
			return;
		}
		if (times == 1) {
			Log.d(TAG, "flash bad search button");
		}
				
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
				mSearchButton.setBackgroundResource(R.drawable.transition_button_bad);
				mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.startTransition(100);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
						mSearchButton.setBackgroundResource(R.drawable.transition_button_bad);
						mSearchButton.setPadding(search_button_padding, search_button_padding, search_button_padding, search_button_padding);
						TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
						drawable.reverseTransition(150);
						// repeat
						mSearchButton.postDelayed(new Runnable() { 
							public void run() {		flashBadSearchButton(times-1); }
						}, 110);
				    }
				}, 110);
		    }
		});
	}
	

	public void showStatus(String text) {
		mStatusText.setText(text);
		mProgressBar.setVisibility(View.VISIBLE);
		mStatusPanel.setVisibility(View.VISIBLE);
	}
	public void hideStatus() {
		mStatusText.setText("");
		mProgressBar.setVisibility(View.GONE);
		mStatusPanel.setVisibility(View.GONE);
	}
	public void hideSpeechWave() {
		if (mUpdateLevel != null) {
			Handler handler = mUpdateLevel.get();
			if (handler != null)
				handler.removeMessages(0);
		}
		mSoundView.setVisibility(View.GONE);
	}
	
	static class SearchHandler extends Handler {
		private boolean processing = false;
		private EvaSpeechComponent speechSearch;
		private MainView view;
		
		public SearchHandler(EvaSpeechComponent speechSearch, MainView view) {
			this.speechSearch = speechSearch;
			this.view = view;
		}
		
		@Override
		public void handleMessage(Message msg) {
			SpeechAudioStreamer  speechAudioStreamer = speechSearch.getSpeechAudioStreamer();
			
			if (speechAudioStreamer.wasNoise) {
				if (speechAudioStreamer.getIsRecording() == false) {
					if (!processing) {
						processing = true;
						view.disableSearchButton();
						view.showStatus("Processing...");
					}
				}
				else {
					view.mSoundView.setSoundData(
							speechAudioStreamer.getSoundLevelBuffer(), 
							speechAudioStreamer.getBufferIndex(),
							speechAudioStreamer.getPeakLevel(),
							speechAudioStreamer.getMinSoundLevel()
					);
					if (view.mSoundView.getVisibility() != View.VISIBLE)
						view.mSoundView.setVisibility(View.VISIBLE);
					view.mSoundView.invalidate();
				}
			}
			
			sendEmptyMessageDelayed(0, 200);
			super.handleMessage(msg);
		}
	};

	
	private SpeechRecognitionResultListener mSpeechSearchListener = new SpeechRecognitionResultListener() {
		
		private void finishSpeech() {
			hideSpeechWave();
			hideStatus();
		}
		
		@Override
		public void speechResultError(String message, Object cookie) {
			finishSpeech();
			VHAApplication.EVA.speechResultError(message, cookie);
		}

		@Override
		public void speechResultOK(String evaJson, Bundle debugData, Object cookie) {
			finishSpeech();
			VHAApplication.EVA.speechResultOK(evaJson, debugData, cookie);
		}
	};
	
	public void startSpeechSearch(final EvaSpeechComponent speechSearch, Object cookie, boolean editLastUtterance) {
		showStatus("Listening...");
		
		activateSearchButton();
		//view.setBackgroundResource(R.drawable.custom_button_active);
		mUpdateLevel = new WeakReference<Handler>(new SearchHandler(speechSearch, this));
		
		try {
			Handler handler = mUpdateLevel.get();
			if (handler != null) {
				speechSearch.start(mSpeechSearchListener, cookie, editLastUtterance);
				handler.sendEmptyMessageDelayed(0, 50);
			}
			else {
				throw new EvaException("updateVolume Level is null");
			}
		}
		catch (EvaException e) {
			Toast.makeText(VHAApplication.getAppContext(), "Failed to start recorder, please try again later and contact the developers if the problem persists", Toast.LENGTH_LONG).show();
			VHAApplication.logError(TAG, "Exception starting recorder", e);
		}
	}

	
	private boolean mainButtonsShown = true;
	
	public boolean areMainButtonsShown() {
		return mainButtonsShown;
	}
	
	public void toggleMainButtons(boolean showMainButtons) {
		mainButtonsShown = showMainButtons;
		mBottomBar.setVisibility(showMainButtons ? View.VISIBLE : View.GONE);
//		mViewPager.invalidate();
//		mTabs.invalidate();
	}
	
	//  Tabs handling

	public String getChatTabName()    {  return mChatTabName; 	}
	public String getHotelsListTabName()  {  return mHotelsTabName; }
	public String getHotelTabName()   {  return mHotelTabName; }
	public String getRoomsTabName()   {  return mRoomsTabName; }
	public String getBookingTabName() {  return mBookingTabName; }
	public String getMapTabName()     {  return mMapTabName; }
	public String getReservationsTabName() {  return mReservationsTabName; }
	public String getReviewsTabName() {  return mReviewsTabName; }

	public int getChatTabIndex()    {  return mTabTitles.indexOf(mChatTabName); 	}
	public int getHotelsListTabIndex()  {  return mTabTitles.indexOf(mHotelsTabName); }
	public int getHotelTabIndex()   {  return mTabTitles.indexOf(mHotelTabName); }
	public int getRoomsTabIndex()   {  return mTabTitles.indexOf(mRoomsTabName); }
	public int getBookingTabIndex() {  return mTabTitles.indexOf(mBookingTabName); }
	public int getMapTabIndex()     {  return mTabTitles.indexOf(mMapTabName); }
	public int getReservationsTabIndex() {  return mTabTitles.indexOf(mReservationsTabName); }
	public int getReviewsTabIndex() {  return mTabTitles.indexOf(mReviewsTabName); }

	
	private HotelsMapFragment mMapFragment;
	private HotelListFragment mHotelsListFragment;
	private HotelDetailFragment mHotelDetailFragment;
	private RoomsSelectFragement mRoomSelectFragment;
	private BookingFragement mBookingFragment;
	private ReservationDisplayFragment mReservationFragment;
	private ReviewsFragment mReviewsFragment;
	private ChatFragment mChatFragment;
	
	
	public ChatFragment 		getChatFragment()    {  return mChatFragment; 	}
	public HotelListFragment 	getHotelsListFragment()  {  return mHotelsListFragment; }
	public HotelDetailFragment 	getHotelFragment()   {  return mHotelDetailFragment; }
	public RoomsSelectFragement getRoomsFragment()   {  return mRoomSelectFragment; }
	public BookingFragement 	getBookingFragment() {  return mBookingFragment; }
	public HotelsMapFragment 	getMapFragment()     {  return mMapFragment; }
	public ReservationDisplayFragment getReservationsFragment() {  return mReservationFragment; }
	public ReviewsFragment 		getReviewsFragment() {  return mReviewsFragment; }

	

	public class TabsPagerAdapter  {} /*implements ViewPager.OnPageChangeListener  {
		
		//private final ViewPager mViewPager;
		private final String TAG = TabsPagerAdapter.class.getSimpleName();
		HotelsMapFragment mMapFragment;
		HotelListFragment mHotelsListFragment;
		HotelDetailFragment mHotelDetailFragment;
		RoomsSelectFragement mRoomSelectFragment;
		BookingFragement mBookingFragment;
		ReservationDisplayFragment mReservationFragment;
		ReviewsFragment mReviewsFragment;
		ChatFragment mChatFragment;
		
		public TabsPagerAdapter(FragmentManager fm) {
			Log.i(TAG, "CTOR");
			// optimization - create before needed
			mChatFragment = new ChatFragment(); 
			mMapFragment = new HotelsMapFragment();
			mHotelsListFragment = new HotelListFragment();
			mHotelDetailFragment = new HotelDetailFragment();
			mRoomSelectFragment = new RoomsSelectFragement();
			mBookingFragment = new BookingFragement();
			mReviewsFragment = null;
			mReservationFragment = null;
		}
		
		@Override public void destroyItem(android.view.ViewGroup container, int position, Object object) {
			if (position >= getCount()) {
				Log.d(TAG, "Destryoing tab at position "+position);
		        FragmentManager manager = ((Fragment) object).getFragmentManager();
		        FragmentTransaction trans = manager.beginTransaction();
		        trans.remove((Fragment) object);
		        trans.commit();
		    }
			else {
				Log.d(TAG, "Ignoring destroyItem at position "+position);
			}
		};
		
	    @Override
	    public int getItemPosition(Object object){
	        return POSITION_NONE;
	    }
				

		public Fragment getItem(int position) {// Asks for the main fragment
			Log.d(TAG, "getItem " + String.valueOf(position));
			int size = mTabTitles.size();
			if (position >= size) {
				VHAApplication.logError(TAG, "No fragment made for Position "+position);
				return null;
			}
			String tabTitle = mTabTitles.get(position);
			if (tabTitle.equals(mChatTabName)) { // Main Chat window
				Log.d(TAG, "Chat Fragment");
				if (mChatFragment == null) {
					mChatFragment = new ChatFragment();
				}
				return mChatFragment;
			}
			else if (tabTitle.equals(mHotelsTabName)) { // Hotel list window
				Log.i(TAG, "Hotels Fragment");
				if (mHotelsListFragment == null) {
					mHotelsListFragment = new HotelListFragment();
				}
				return mHotelsListFragment;
			}
			else if (tabTitle.equals(mMapTabName)) { // Hotel list window
				Log.i(TAG, "HotelsMap Fragment");
				if (mMapFragment == null) {
					mMapFragment = new HotelsMapFragment();
				}
				return mMapFragment;
			}
			
//			else if (mTabTitles.get(position).equals(getString(R.string.FLIGHTS))) { // flights list
//				Log.i(TAG, "Flights Fragment");
//				return new FlightsFragment();
//			}
			else if (tabTitle.equals(mHotelTabName)) { // Single hotel
				Log.i(TAG, "starting hotel Fragment");
				if (mHotelDetailFragment == null) {
					mHotelDetailFragment = new HotelDetailFragment();
				}
				return mHotelDetailFragment;
			}
			else if (tabTitle.equals(mRoomsTabName)) {
				Log.i(TAG, "starting Rooms Fragment");
				if (mRoomSelectFragment == null) {
					mRoomSelectFragment = new RoomsSelectFragement();
				}
				return mRoomSelectFragment;
			}
			else if (tabTitle.equals(mBookingTabName)) {
				Log.i(TAG, "starting booking fragment");
				if (mBookingFragment == null) {
					mBookingFragment = new BookingFragement();
				}
				return mBookingFragment;
			}
			else if (tabTitle.equals(mReservationsTabName)) {
				Log.i(TAG, "starting reservation fragment");
				if (mReservationFragment == null) {
					mReservationFragment = new ReservationDisplayFragment();
				}
				return mReservationFragment;
			}
			else if (tabTitle.equals(mReviewsTabName)) {
				Log.i(TAG, "Starting reviews fragment");
				if (mReviewsFragment == null) {
					mReviewsFragment = new ReviewsFragment();
				}
				return mReviewsFragment;
			}
//			if (mTabTitles.get(position).equals(getString(R.string.TRAINS))) { // trains list window
//				Log.i(TAG, "Trains Fragment");
//				return new TrainsFragment();
//			}

			VHAApplication.logError(TAG, "No fragment made for Position "+position+(position< size ? " titled "+tabTitle : ""));
			return null;
		}

		@Override
		public int getCount() {
			return mTabTitles.size();
		}
		
		@Override
        public CharSequence getPageTitle(int position) {
            return mTabTitles.get(position % mTabTitles.size());
        }
	 	
		@Override
		public void notifyDataSetChanged() {
//			mTabs.notifyDataSetChanged();
			super.notifyDataSetChanged();
		}
		
//		int lastShown = -1;

//		// Internal helper function
//		public void showTab(int position) {
//			Log.d(TAG, "showTab "+position);
////			lastShown = position;
//			mViewPager.setCurrentItem(position, true);
////			mTabs.onPageSelected(position);
////			this.notifyDataSetChanged();
//		}
//		
//		public void showTab(String name) {
//			int index = mTabTitles.indexOf(name);
//			if (index == -1) {
//				addTab(name);
//			}
//			else {
//				showTab(index);
//			}
//		}

		public void addTab(String name) { // Dynamic tabs add to end
			Log.d(TAG, "addTab "+name);
//			mTabs.setAdapter(null);
			// ??? mViewPager.setAdapter(null);
			mTabTitles.add(name);
			notifyDataSetChanged();
		}
		
		public void addTab(String name, int position) { // Dynamic tabs add to certain position
			Log.d(TAG, "addTab "+name);
//			mTabs.setAdapter(null);
			// ??? mViewPager.setAdapter(null);
			mTabTitles.add(position, name);
			notifyDataSetChanged();
		}
		

		
		public void removeTab(String tabName) {
			int ind = mTabTitles.indexOf(tabName);
			if (ind != -1)
				removeTab(ind);
		}
		
		public void removeTab(int tabIndex)
		{
			Log.d(TAG, "removeTab "+tabIndex);
//			mTabs.setAdapter(null);
			// ??? mViewPager.setAdapter(null);
			mTabTitles.remove(tabIndex);
			notifyDataSetChanged();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return false;
		}

	}
*/

	public int getCurrentPage() {
	//	return mTabs.getCurrentPage();
//		return mViewPager.getCurrentItem();
		return 0;
	}

	public void setCurrentItem(int tabInd) {
//		mViewPager.setCurrentItem(tabInd, true);
//		mTabs.setCurrentItem(tabInd);
	}

	public void removeTabs() {
		final String [] tabsToRemove = { mHotelsTabName, mHotelTabName, mMapTabName, mRoomsTabName, 
				mReviewsTabName, mBookingTabName, mReservationsTabName };
		for (String tab : tabsToRemove) {
			int index = mTabTitles.indexOf(tab);
			if (index != -1)
				mTabTitles.remove(index);
		}
		
//		mTabsAdapter.notifyDataSetChanged();
	}

	public void addTab(String tabName) {
//		mTabsAdapter.addTab(tabName);
	}

	public void removeTab(String tabName) {
//		mTabsAdapter.removeTab(tabName);
	}

	public void fadeOutView(boolean tabs, boolean pager, boolean buttons) {
		AlphaAnimation anim = new AlphaAnimation(1f, 0.1f);
		anim.setDuration(500);
		anim.setRepeatCount(0);
		anim.setFillAfter(true);
//		if (tabs)
//			mTabs.startAnimation(anim);
//		if (pager)
//			mViewPager.startAnimation(anim);
		if (buttons)
			mBottomBar.startAnimation(anim);
	}

	public void fadeInView(boolean tabs, boolean pager, boolean buttons) {
		AlphaAnimation anim = new AlphaAnimation(0.1f, 1f);
		anim.setFillAfter(true);
		anim.setRepeatCount(0);
		anim.setDuration(200);
//		if (tabs)
//			mTabs.startAnimation(anim);
//		if (pager)
//			mViewPager.startAnimation(anim);
		if (buttons)
			mBottomBar.startAnimation(anim);
	}

}
