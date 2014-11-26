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
import android.support.modified.v13.app.FragmentPagerAdapter;
import android.support.modified.v4.view.ViewPager;
import android.support.modified.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
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
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

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

//	private ImageButton mTextButton;
	private View mBottomBar;
	private final int search_button_padding = 24;

	private WeakReference<Handler> mUpdateLevel;
	private ViewPager mViewPager;
	private MyPagerAdapter mPagerAdapter;
	
	private String mChatTabName;
	private String mHotelsTabName;

	private List<String> mTabTitles;
	
	public MainView(final MainActivity mainActivity, List<String> tabTitles) {
		mStatusPanel = mainActivity.findViewById(R.id.status_panel);
		mStatusText = (TextView)mainActivity.findViewById(R.id.text_listeningStatus);
		mProgressBar = (ProgressBar)mainActivity.findViewById(R.id.progressBar1);
		mSoundView = (SoundLevelView)mainActivity.findViewById(R.id.surfaceView_sound_wave);
		mSearchButton = (ImageButton) mainActivity.findViewById(R.id.search_button);
		mBottomBar = mainActivity.findViewById(R.id.bottom_bar);
		mViewPager = (ViewPager) mainActivity.findViewById(R.id.viewpager);
		
		mChatTabName = mainActivity.getString(R.string.CHAT);
//		mExamplesTabName = mainActivity.getString(R.string.EXAMPLES);
		//mDebugTabName = mainActivity.getString(R.string.DEBUG);
		mHotelsTabName = mainActivity.getString(R.string.HOTELS);
		
		mTabTitles = tabTitles;


		Toolbar toolbar = (Toolbar) mainActivity.findViewById(R.id.toolbar);
		toolbar.setTitle(mainActivity.getString(R.string.app_name));
		mainActivity.setSupportActionBar(toolbar);
		
		
		final ActionBar supportActionBar = mainActivity.getSupportActionBar();
		supportActionBar.setDisplayHomeAsUpEnabled(false);
		supportActionBar.setHomeButtonEnabled(true);

		
		final String[] drawerItems = {
				//mainActivity.getString(R.string.tutorial),
				mainActivity.getString(R.string.faq),
				mainActivity.getString(R.string.settings),
				mainActivity.getString(R.string.report_a_bug),
				mainActivity.getString(R.string.about)
		};
        ListView drawerList = (ListView) mainActivity.findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        drawerList.setAdapter(new ArrayAdapter<String>(mainActivity,
                R.layout.drawer_list_item, drawerItems));
        // Set the list's click listener
//        drawerList.setOnItemClickListener(new DrawerItemClickListener());

        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

    		@Override
    		public void onItemClick(android.widget.AdapterView<?> parent,
    				View view, int position, long id) {
    			mainActivity.selectDrawerItem(position, drawerItems[position]);
    		}
        });
		
		// setup the tab switching
		mPagerAdapter = new MyPagerAdapter(mainActivity.getFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
//		mTabs.setViewPager(mViewPager);
//		mViewPager.setOffscreenPageLimit(5);
		
		
		mSoundView.setColor(0xffdd8877);
		mSoundView.setAlign(Gravity.RIGHT);

		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
	
			@Override
			public void onPageSelected(int position) {
				supportActionBar.setDisplayHomeAsUpEnabled(position > 0);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
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
				// TODO: disable button?
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

	
	public int getChatTabIndex() {
		return 0;
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

	private HotelListFragment mHotelsListFragment;
	private ChatFragment mChatFragment;
	
	
	public ChatFragment 		getChatFragment()    {  return mChatFragment; 	}
	public HotelListFragment 	getHotelsListFragment()  {  return mHotelsListFragment; }

	

	public class MyPagerAdapter extends FragmentPagerAdapter /*implements ViewPager.OnPageChangeListener */ {
		
		private final String TAG = "MyPagerAdapter";
		
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			Log.i(TAG, "CTOR");
			// optimization - create before needed
			mChatFragment = new ChatFragment(); 
//			mHotelsListFragment = new HotelListFragment();
		}
//		
		@Override 
		public void destroyItem(android.view.ViewGroup container, int position, Object object) {
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
		
//	    @Override
	    public int getItemPosition(Object object){
	    	if (object == mChatFragment) {
	    		return POSITION_UNCHANGED;
	    	}
	        return POSITION_NONE;
	    }

	    @Override
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
	 	
//		int lastShown = -1;

	}

	public int getCurrentPage() {
		return mViewPager.getCurrentItem();
	}

	public void removeTabs() {
		final String [] tabsToRemove = { mHotelsTabName };
		for (String tab : tabsToRemove) {
			int index = mTabTitles.indexOf(tab);
			if (index != -1)
				mTabTitles.remove(index);
		}
		
		mPagerAdapter.notifyDataSetChanged();
	}

	public void showTab(int position) {
		Log.d(TAG, "showTab "+position);
//		lastShown = position;
		mViewPager.setCurrentItem(position, true);
	}
	
	public void showTab(String name) {
		int index = mTabTitles.indexOf(name);
		if (index == -1) {
			addTab(name);
		}
		else {
			showTab(index);
		}
	}

	public void addTab(String name) { // Dynamic tabs add to end
		Log.d(TAG, "addTab "+name);
		mTabTitles.add(name);
		mPagerAdapter.notifyDataSetChanged();
	}
	
	public void addTab(String name, int position) { // Dynamic tabs add to certain position
		Log.d(TAG, "addTab "+name);
		mTabTitles.add(position, name);
		mPagerAdapter.notifyDataSetChanged();
	}
	

	
	public void removeTab(String tabName) {
		int ind = mTabTitles.indexOf(tabName);
		if (ind != -1)
			removeTab(ind);
	}
	
	public void removeTab(int tabIndex)
	{
		Log.d(TAG, "removeTab "+tabIndex);
		mTabTitles.remove(tabIndex);
		mPagerAdapter.notifyDataSetChanged();
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
		if (pager)
			mViewPager.startAnimation(anim);
		if (buttons)
			mBottomBar.startAnimation(anim);
	}



}
