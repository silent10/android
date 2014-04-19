/*
 * Copyright (c) 2012 Evature.com
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to 
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:  
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.  
 *  
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package com.virtual_hotel_agent.search.controllers.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.request.CommonParameters;
import com.evaapis.android.EvaComponent;
import com.evaapis.android.EvaSearchReplyListener;
import com.evaapis.android.EvaSpeechComponent;
import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.EvaWarning;
import com.evaapis.crossplatform.flow.FlowElement;
import com.evaapis.crossplatform.flow.FlowElement.TypeEnum;
import com.evaapis.crossplatform.flow.QuestionElement;
import com.evature.util.Log;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.viewpagerindicator.TitlePageIndicator;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.events.BookingCompletedEvent;
import com.virtual_hotel_agent.search.controllers.events.ChatItemModified;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelSelected;
import com.virtual_hotel_agent.search.controllers.events.HotelsListUpdated;
import com.virtual_hotel_agent.search.controllers.events.RatingClickedEvent;
import com.virtual_hotel_agent.search.controllers.events.RoomSelectedEvent;
import com.virtual_hotel_agent.search.controllers.events.ToggleMainButtonsEvent;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListener;
import com.virtual_hotel_agent.search.controllers.web_services.HotelDownloaderTask;
import com.virtual_hotel_agent.search.controllers.web_services.HotelListDownloaderTask;
import com.virtual_hotel_agent.search.controllers.web_services.RoomsUpdaterTask;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.views.MainView;
import com.virtual_hotel_agent.search.views.fragments.BookingFragement;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;
import com.virtual_hotel_agent.search.views.fragments.ChildAgeDialogFragment;
//import com.virtual_hotel_agent.search.views.fragments.ExamplesFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelDetailFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelsMapFragment;
import com.virtual_hotel_agent.search.views.fragments.ReservationDisplayFragment;
import com.virtual_hotel_agent.search.views.fragments.ReviewsFragment;
//import com.virtual_hotel_agent.search.views.fragments.ExamplesFragment.ExampleClickedHandler;
import com.virtual_hotel_agent.search.views.fragments.RoomsSelectFragement;

public class MainActivity extends ActionBarActivity implements 
													EvaSearchReplyListener,
													OnSharedPreferenceChangeListener {

	private static final String ITEMS_IN_SESSION = "items_in_session";


	private static final String TAG = MainActivity.class.getSimpleName();
	// private static String mExternalIpAddress = null;
	
	private List<String> mTabTitles;
	
	private ViewPager mViewPager; 
	private TitlePageIndicator mTabs;
	//SearchVayantTask mSearchVayantTask;
	//SearchTravelportTask mSearchTravelportTask;
	TabsPagerAdapter mTabsAdapter;
		
	private boolean mIsNetworkingOk = false;

	private String mChatTabName;
//	private String mExamplesTabName;
	private String mHotelsTabName;
	private String mHotelTabName;
	private String mRoomsTabName;
	private String mBookingTabName;
	private String mMapTabName;
	private String mReservationsTabName;
	private String mReviewsTabName;

	static private HotelListDownloaderTask mSearchExpediaTask = null;
	static private RoomsUpdaterTask mRoomUpdater = null;
	static private HotelDownloaderTask mHotelDownloader = null;
	
	private ChatItem storeVoiceResultInChatItem = null;

	private EvaSpeechComponent speechSearch = null;
	
	MainView mainView;


	@Override
	public void onDestroy() {
		VHAApplication.EVA.onDestroy();
		super.onDestroy();
	}
	
	
	
	
// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		VHAApplication.EVA.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	// This examples assumes the use of Google Analytics campaign
	// "utm" parameters, like "utm_source"
	private static final String CAMPAIGN_SOURCE_PARAM = "utm_source";


	// cookies are returned onEvaReply are those passed to the search query 
	private static final Object DELETED_UTTERANCE_COOKIE = new Object();
	private static final Object VOICE_COOKIE = new Object();
	private static final Object TEXT_TYPED_COOKIE = new Object();
	 /*
	   * Given a URI, returns a map of campaign data that can be sent with
	   * any GA hit.
	   *
	   * @param uri A hierarchical URI that may or may not have campaign data
	   *     stored in query parameters.
	   *
	   * @return A map that may contain campaign or referrer
	   *     that may be sent with any Google Analytics hit.
	   */
	Map<String, String> getReferrerMapFromUri(Uri uri) {
		MapBuilder paramMap = new MapBuilder();

		// If no URI, return an empty Map.
		if (uri == null) {
			return paramMap.build();
		}

		// Source is the only required campaign field. No need to continue if
		// not
		// present.
		if (uri.getQueryParameter(CAMPAIGN_SOURCE_PARAM) != null) {

			// MapBuilder.setCampaignParamsFromUrl parses Google Analytics
			// campaign
			// ("UTM") parameters from a string URL into a Map that can be set
			// on
			// the Tracker.
			paramMap.setCampaignParamsFromUrl(uri.toString());

			// If no source parameter, set authority to source and medium to
			// "referral".
		} else if (uri.getAuthority() != null) {

			paramMap.set(Fields.CAMPAIGN_MEDIUM, "referral");
			paramMap.set(Fields.CAMPAIGN_SOURCE, uri.getAuthority());

		}

		return paramMap.build();
	}
	

	@Override
	public void onStart() {
		super.onStart();
		try {
			Tracker t1 = GoogleAnalytics.getInstance(this).getTracker("UA-47284954-1");
	
			Intent intent = this.getIntent();
		    Uri uri = intent.getData();
		    if (uri != null) {
		    	MapBuilder.createAppView().setAll(getReferrerMapFromUri(uri));
		    }
		    EasyTracker.getInstance(this).activityStart(this);
		}
		catch(Exception e) {
			VHAApplication.logError(TAG, "Exception setting google analytics", e);
		}
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	
	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		VHAApplication.EVA.onResume();
		super.onResume();
//		setDebugData(DebugTextType.None, null);
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		cancelBackgroundThreads();
		VHAApplication.EVA.onPause();
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Called when the activity is first created.
		Log.d(TAG, "onCreate()");
		
		SettingsAPI.getLocale(this);
		VHAApplication.EVA = new EvaComponent(this, this);
		EvaComponent eva = VHAApplication.EVA;
		eva.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		
		setVolumeControlStream(AudioManager.STREAM_MUSIC); // TODO: move to EvaComponent?
		speechSearch = new EvaSpeechComponent(eva);
		setContentView(R.layout.new_main);
		
//		Handler mHandlerTripAdvisorDownloaded = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				Bitmap tripAdvisorBmp = (Bitmap) msg.obj;
//				super.handleMessage(msg);
//			}
//		};
//		ImageDownloader imageDownloader = new ImageDownloader(null, mHandlerTripAdvisorDownloaded );
		
		eva.registerPreferenceListener();
		eva.setScope("h");
		
		PackageInfo pInfo;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			eva.setAppVersion("vha_"+pInfo.versionCode);
		} catch (NameNotFoundException e) {
			Log.w(TAG, "Failed to get app version");
			eva.setAppVersion("vha_unknown");
		}
		
		mViewPager = (ViewPager) findViewById(R.id.viewpager); 
		mTabs = (TitlePageIndicator) findViewById(R.id.indicator);
		
		mainView = new MainView(this);
		
		eva.setApiKey(SettingsAPI.getEvaKey(this));
		eva.setSiteCode(SettingsAPI.getEvaSiteCode(this));
		
		mChatTabName = getString(R.string.CHAT);
//		mExamplesTabName = getString(R.string.EXAMPLES);
		//mDebugTabName = getString(R.string.DEBUG);
		mHotelsTabName = getString(R.string.HOTELS);
		mHotelTabName = getString(R.string.HOTEL);
		mRoomsTabName = getString(R.string.ROOMS);
		mBookingTabName = getString(R.string.BOOKING);
		mReservationsTabName = getString(R.string.RESERVATIONS);
		mMapTabName = getString(R.string.MAP);
		mReviewsTabName = getString(R.string.REVIEWS);

		CommonParameters.currencyCode = SettingsAPI.getCurrencyCode(this);
		
//		if (savedInstanceState != null  && MyApplication.FOUND_HOTELS.size() > 0) { // Restore state
//			// Same code as onRestoreInstanceState() ?
//			Log.d(TAG, "restoring saved instance state");
//			mTabTitles = savedInstanceState.getStringArrayList("mTabTitles");
//		} else {
//			Log.d(TAG, "no saved instance state");
			mTabTitles = new ArrayList<String>(Arrays.asList(/*mExamplesTabName,*/ mChatTabName));
//		}
		
		mTabsAdapter = new TabsPagerAdapter(getSupportFragmentManager());
		
		clearChatList();

		mViewPager.setAdapter(mTabsAdapter);
		mTabs.setViewPager(mViewPager);
		mViewPager.setOffscreenPageLimit(5);

		mTabs.setOnPageChangeListener(new OnPageChangeListener() {
			private boolean lastShown = true;
			private boolean hidButtons = false;
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
	
			@Override
			public void onPageSelected(int position) {
				getActionBar().setDisplayHomeAsUpEnabled(position > 0);
				if (position > 2) {
					if (!hidButtons) {
						// save last shown on first hiding of buttons
						lastShown = mainView.areMainButtonsShown();
					}
					mainView.toggleMainButtons(false);
					hidButtons = true;
				}
				else {
					if (lastShown && hidButtons) {
						mainView.toggleMainButtons(true);
						hidButtons = false;
						lastShown = true;
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
	
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			Log.d(TAG, "Progress: We are connected to the network");
			mIsNetworkingOk = true;
			// fetch data
			// new GetExternalIpAddress().execute();
		}
		if (!mIsNetworkingOk) {
			fatal_error(R.string.network_error);
		}

//		setDebugData(DebugTextType.None, null);

		mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));

		// patch for debug - bypass the speech recognition:
		// Intent data = new Intent();
		// Bundle a_bundle = new Bundle();
		// ArrayList<String> sentences = new ArrayList<String>();
		// sentences.add("3 star hotel in rome");
		// a_bundle.putStringArrayList(RecognizerIntent.EXTRA_RESULTS, sentences);
		// data.putExtras(a_bundle);
		// onActivityResult(VOICE_RECOGNITION_REQUEST_CODE, RESULT_OK, data);
		
		AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		Log.i(TAG, "Current volume :"+volume+" out of "+maxVolume);
		
		// stop the flash-screen being in memory
		//findViewById(R.id.the_main_layout).setBackgroundResource(R.drawable.hotel_background);
	}
		
	@Override
	public void onBackPressed() {
	   Log.d(TAG, "onBackPressed Called");
	   int chatInd = mTabTitles.indexOf(mChatTabName);
	   if (mTabs.getCurrentPage() == chatInd) {
		   boolean handled = getChatFragment().handleBackPressed();
		   if (!handled) {
			   super.onBackPressed();
		   }
	   }
	   else {
		   mTabs.setCurrentItem(chatInd);
	   }
	}
	

	public class TabsPagerAdapter  extends FragmentPagerAdapter /*implements ViewPager.OnPageChangeListener */ {
		
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

		public TabsPagerAdapter( FragmentManager fm) {
			super(fm);
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
		

	    @Override
	    public int getItemPosition(Object object){
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
	 
//		@Override
//		public void onPageScrollStateChanged(int arg0) {
//		}

//		@Override
//		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//			if (positionOffset > 0) {
//				lastShown = position+1;
//			}
//			else {
//				lastShown = position;
//			}
//		}
//
//		@Override
//		public void onPageSelected(int position) {
//			lastShown = position;
//		}
//		
		@Override
		public void notifyDataSetChanged() {
			mTabs.notifyDataSetChanged();
			super.notifyDataSetChanged();
		}
		
//		int lastShown = -1;

		// Internal helper function
		public void showTab(int position) {
			Log.d(TAG, "showTab "+position);
//			lastShown = position;
			mViewPager.setCurrentItem(position, true);
//			mTabs.onPageSelected(position);
//			this.notifyDataSetChanged();
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

	}
	
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		//menu.getItem(2).setVisible(eva.isDebug());
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return  super.onCreateOptionsMenu(menu);
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // user pressed the menu button
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
			return true;
		case R.id.settings: // Did the user select "settings"?
			intent = new Intent();
			// Then set the activity class that needs to be launched/started.
			intent.setClass(this, MyPreferences.class);
//			Bundle a_bundle = new Bundle(); // Lets send some data to the preferences activity
		//	a_bundle.putStringArrayList("mLanguages", (ArrayList<String>) mSpeechRecognition.getmGoogleLanguages());
//			intent.putExtras(a_bundle);
			startActivity(intent);
			return true;
		case R.id.faq:
			String faqUrl = "http://www.travelnow.com/templates/352395/faq";
			Uri uri = Uri.parse(Html.fromHtml(faqUrl).toString());
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(uri);
			Log.i(TAG, "Setting Browser to url:  "+uri);
			startActivity(i);
			return true;
//		case R.id.help:
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(getString(R.string.app_name));
//			final TextView helpMessage = new TextView(this);
//			String helpText = this.getText(R.string.help_text).toString();
//			helpMessage.setText(helpText);
//			helpMessage.setMovementMethod(LinkMovementMethod.getInstance());
//			helpMessage.setPadding(10, 10, 10, 10);
//			builder.setView(helpMessage);
//			builder.setPositiveButton(getString(R.string.ok_button), null);
//			builder.setCancelable(false); // Can you just press back and dismiss it?
//			builder.create().show();
//			return true;
//			
		case R.id.bug_report:
			// Then set the activity class that needs to be launched/started.
			intent = new Intent(this, BugReportDialog.class);
			startActivity(intent);
			return true;
//		case R.id.about: // Did the user select "About us"?
//			// Links in alertDialog:
//			// http://stackoverflow.com/questions/1997328/android-clickable-hyperlinks-in-alertdialog
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(getString(R.string.app_name));
//			final TextView message = new TextView(this);
//			String text = this.getText(R.string.lots_of_text).toString();
//			
//			try {
//				int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
//				text += "\n\nVersion: "+version;
//				if (!MyApplication.AcraInitialized) {
//					text += "\n\n  --->  ACRA not initalized!";
//				}
//			} catch (NameNotFoundException e) {
//				Log.w(TAG, "Name not found getting version", e);
//			}
//			
//			final SpannableString s = new SpannableString(text);
//			Linkify.addLinks(s, Linkify.WEB_URLS);
//			message.setText(s);
//			message.setMovementMethod(LinkMovementMethod.getInstance());
//			message.setPadding(10, 10, 10, 10);
//			builder.setView(message);
//			builder.setPositiveButton(getString(R.string.ok_button), null);
//			builder.setCancelable(false); // Can you just press back and dismiss it?
//			builder.create().show();
//			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is killed and restarted.
		try {
			super.onSaveInstanceState(savedInstanceState);
		}
		catch (IllegalStateException e) {
			// this sometimes happens when "fragment not in fragment manager" - not sure why
			VHAApplication.logError(TAG, "Illegal state while saving instance state in main activity", e);
		}

		// savedInstanceState.putBoolean("mTtsWasConfigured", mSpeechToTextWasConfigured);
		// savedInstanceState.putString("mExternalIpAddress", mExternalIpAddress);
		savedInstanceState.putStringArrayList("mTabTitles", (ArrayList<String>) mTabTitles);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		// restore state:
		// mExternalIpAddress = savedInstanceState.getString("mExternalIpAddress");
		// mSpeechToTextWasConfigured = savedInstanceState.getBoolean("mTtsWasConfigured");
	}
	
	private void clearChatList() {
		ChatFragment chatFragment = getChatFragment();
		if (chatFragment != null && chatFragment.isReady()) {
			chatFragment.clearChat();
		}
	}

	
	private void addChatItem(ChatItem item) {
		Log.d(TAG, "Adding chat item  type = "+item.getType()+ "  '"+item.getChat()+"'");
		ChatFragment chatFragment = getChatFragment();
		if (chatFragment != null && chatFragment.isReady()) {
			chatFragment.addChatItem(item);
		}
		else {
			mChatListModel.add(item);
		}
		//mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}
	
	private ChatFragment getChatFragment() {
//		int index = mTabTitles.indexOf(mChatTabName);
//				Log.i(TAG, "Chat tab at index "+index);
//		if (index == -1) {
//			mTabsAdapter.addTab(mChatTabName);
//			index = mTabTitles.size() - 1;
//		}
		if (mTabsAdapter == null) {
			Log.w(TAG, "chat mTabsAdapter == null!?");
			return null;
		}

		ChatFragment fragment = mTabsAdapter.mChatFragment;//instantiateItem(mViewPager, index); 		// http://stackoverflow.com/a/8886019/78234
		if (fragment == null) { // could be null if not instantiated yet
			Log.w(TAG, "chat fragment == null!?");
		}
		return fragment;
	}
	

	private String handleChat(EvaApiReply apiReply) {
		if (!apiReply.isFlightSearch() && !apiReply.isHotelSearch() && (apiReply.chat != null)) {
			if (apiReply.chat.hello != null && apiReply.chat.hello) {
				return "Why, Hello there!";
			}
			if (apiReply.chat.who != null && apiReply.chat.who) {
				return "I'm your virtual hotel agent";
			}
			if (apiReply.chat.meaningOfLife != null && apiReply.chat.meaningOfLife) {
				return "Staying in awesome hotels, of course!";
			}
		}
		return null;
	}


//	public void setVayantReply(JSONObject response) {
//		setDebugData(DebugTextType.VayantDebug, response);
//	}
	

	
	private void fatal_error(final int string_id) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.fatal_error));
		builder.setMessage(string_id);
		builder.setPositiveButton(getString(R.string.ok_button), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// if this button is clicked, close current activity (exit the app).
				// Test1Activity.this.finish();
			}
		});
		builder.setCancelable(false); // Can you just press back and dismiss it?
		builder.create().show();
	}
	
	/***
	 * Start a voice recognition - and place the results in the chatItem (or add a new one if null)
	 */
	private void voiceRecognitionSearch(ChatItem chatItem, boolean editLastUtterance) {
		// simplest method:  default 
		// MainActivity.this.eva.searchWithVoice("voice");
		
		if ("google_local".equals(VHAApplication.EVA.getVrService())) {
			VHAApplication.EVA.searchWithLocalVoiceRecognition(4);
			return;
		}
		
		if (speechSearch.isInSpeechRecognition() == true) {
			speechSearch.stop();
			return;
		}
		
		storeVoiceResultInChatItem = chatItem;
		
		VHAApplication.EVA.speak("");
		Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("speech_search", "speech_search_start", "", editLastUtterance ? 1l: 0l)
				    .build()
				   );
		
		
		mainView.startSpeechSearch(speechSearch, VOICE_COOKIE, editLastUtterance);
	}
	
	// search button click handler ("On Click property" of the button in the xml)
	// http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.restart_button:
			startNewSession();
			break;

		case R.id.search_button:
			voiceRecognitionSearch(null, false);
			break;
			
		case R.id.add_utterance_button:
			ChatFragment chatFragment = getChatFragment();
			if (chatFragment != null)
				chatFragment.addUtterance();
			mTabsAdapter.showTab(mChatTabName);
			break;
		}
	}

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if ("vha_preference_currency".equals(key)) {
			
			int currencyIndex = Integer.parseInt(sharedPreferences.getString("vha_preference_currency", "-1"));
			String[] entries = getResources().getStringArray(R.array.entries_currency_preference);
	
			String currencyCode;
			if (currencyIndex < entries.length)
				currencyCode = entries[currencyIndex];
			else {
				VHAApplication.logError(TAG, "currencyIndex = "+currencyIndex+"  but entries.size = "+entries.length);
				currencyCode = entries[0];
			}
			CommonParameters.currencyCode = currencyCode;
		}

		
//		if (EvaComponent.DEBUG_PREF_KEY.equals(key)) {
//			ActivityCompat.invalidateOptionsMenu(this);
//		}
//		else if (SettingsAPI.EVA_KEY.equals(key)) {
//			VHAApplication.EVA.setApiKey(SettingsAPI.getEvaKey(this));
//		}
//		else if (SettingsAPI.EVA_SITE_CODE.equals(key)) {
//			VHAApplication.EVA.setSiteCode(SettingsAPI.getEvaSiteCode(this));
//		}
	}
	
	@Override
	public void onEvaError(String message, boolean isServerError, Object cookie) {
		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		mainView.flashBadSearchButton(2);
		Tracker defaultTracker = GoogleAnalytics.getInstance(VHAApplication.getAppContext()).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("speech_search", "speech_search_end_bad", message, 0l)
				    .build()
				   );

	}

	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {

		if (VHAApplication.AcraInitialized) {
			ErrorReporter bugReporter = ACRA.getErrorReporter();
			String itemsStr = bugReporter.getCustomData(ITEMS_IN_SESSION);
			int items;
			if (itemsStr == null) {
				items = 0;
			}
			else {
				items = Integer.parseInt(itemsStr);
			}
			items++;
			bugReporter.putCustomData(ITEMS_IN_SESSION, ""+items);
			if (reply.sessionId != null && reply.sessionId.equals("") == false && reply.sessionId.equals("1") == false) {
				bugReporter.putCustomData("eva_session_"+items, reply.sessionId );
			}
			else if (reply.JSONReply != null) {
				bugReporter.putCustomData("eva_session_"+items, reply.JSONReply.toString());
			}
		}
		if (VOICE_COOKIE == cookie) {
			if (reply.errorMessage != null) {
				mainView.flashBadSearchButton(2);
				Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
				if (defaultTracker != null) 
					defaultTracker.send(MapBuilder
						    .createEvent("speech_search", "speech_search_end_bad", reply.errorMessage, 0l)
						    .build()
						   );
			}
			else {
				mainView.deactivateSearchButton();
				Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
				if (defaultTracker != null) 
					defaultTracker.send(MapBuilder
						    .createEvent("speech_search", "speech_search_end_ok", reply.processedText, 0l)
						    .build()
						   );

			}
			SpannableString chat = null;
//			if (reply.originalInputText != null) {
//				chat = new SpannableString(reply.originalInputText);
//			}
//			else 
			if (reply.processedText != null) {
				// reply of voice -  add a "Me" chat item for the input text
				chat = new SpannableString(reply.processedText);
				if (reply.evaWarnings.size() > 0) {
					int col = getResources().getColor(R.color.my_chat_no_session_text);
					for (EvaWarning warning: reply.evaWarnings) {
						if (warning.position == -1) {
							continue;
						}
						chat.setSpan( new ForegroundColorSpan(col), warning.position, warning.position+warning.text.length(), 0);
						//chat.setSpan( new StyleSpan(Typeface.ITALIC), warning.position, warning.position+warning.text.length(), 0);
					}
				}
			}
			if (chat != null) {
				if (storeVoiceResultInChatItem != null) {
					// this voice recognition replaces the last utterance
					getChatFragment().voiceResponseToChatItem(storeVoiceResultInChatItem, chat);
					storeVoiceResultInChatItem = null;
				}
				else {
					addChatItem(new ChatItem(chat));
				}
			}
		}
		
		if (cookie == DELETED_UTTERANCE_COOKIE) {
			if (reply.errorMessage != null) {
				mainView.flashBadSearchButton(2);
				Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
				if (defaultTracker != null) 
					defaultTracker.send(MapBuilder
						    .createEvent("text_search", "text_search_end_bad", reply.errorMessage, 0l)
						    .build()
						   );
				
				// TODO: there was an error - restore the removed items
			}
			else {
				// this is a response of a "delete last utterance" request -
				// if the reply is the same as the previous 
				if (mChatListModel.size() > 0) {
					ChatItem lastChatItem = mChatListModel.get(mChatListModel.size()-1);
					EvaApiReply oldReply = lastChatItem.getEvaReply();
					if (oldReply != null) {
						ArrayList<ChatItem> itemsToRemove = new ArrayList<ChatItem>();
						for (ChatItem itemInList : mChatListModel) {
							if (itemInList.getEvaReply() == oldReply) {
								itemsToRemove.add(itemInList);
							}
						}
						mChatListModel.removeAll(itemsToRemove);
					}
				}
			}
		}
		
		if (cookie == TEXT_TYPED_COOKIE) {
			if (reply.errorMessage != null) {
				mainView.flashBadSearchButton(2);
				Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
				if (defaultTracker != null) 
					defaultTracker.send(MapBuilder
						    .createEvent("text_search", "text_search_end_bad", reply.errorMessage, 0l)
						    .build()
						   );
				
				// TODO: there was an error - restore the pre-modified text
			}
		}
		
//		if (reply.JSONReply != null) {
//			setDebugData(DebugTextType.EvaDebug, reply.JSONReply);
//		}
//		
		if (reply.flow != null ) {
			handleFlow(reply);
		}
//		else {
//			handleNonFlow(reply);  // old code
//		}		
	}

	/****
	 * Display chat items for each flow element - 
	 * execute the first question element or, if no question element, execute the first flow element
	 * @param reply
	 */
	private void handleFlow(EvaApiReply reply) {
		boolean first = true;
		boolean hasQuestion = false;
		for (FlowElement flow : reply.flow.Elements) {
			if (flow.Type == TypeEnum.Question) {
				hasQuestion = true;
				break;
			}
		}
		
		for (FlowElement flow : reply.flow.Elements) {
			
			ChatItem chatItem = null;
			if (flow.Type == TypeEnum.Question) {
				QuestionElement question = (QuestionElement) flow;
				DialogQuestionChatItem  questionChatItem = new DialogQuestionChatItem(flow.getSayIt(), reply, flow);
				chatItem = questionChatItem;
				addChatItem(questionChatItem);
				
				if (question.choices != null && question.choices.length > 0) {
					for (int index=0; index < question.choices.length; index++) {
						addChatItem(new DialogAnswerChatItem(questionChatItem, index, question.choices[index]));
					}
				}
				if (first) {
					// execute first question
					executeFlowElement(reply, flow, chatItem, true);
					first = false;
				}
			}
			else {
				chatItem = new ChatItem(flow.getSayIt(), reply, flow, ChatType.VirtualAgent);
				addChatItem(chatItem);
			}
			
			if (!hasQuestion) {
				// if no questions exist
				if ( first) {
					// automatically execute first element and switch to result
					executeFlowElement(reply, flow, chatItem, true);
					
					first = false;
				}
				else {
					executeFlowElement(reply, flow, chatItem, false);
				}
			}
			
		}
	}
	
	private void removeTabs() {
		final String [] tabsToRemove = { mHotelsTabName, mHotelTabName, mMapTabName, mRoomsTabName, 
				mReviewsTabName, mBookingTabName, mReservationsTabName };
		for (String tab : tabsToRemove) {
			int index = mTabTitles.indexOf(tab);
			if (index != -1)
				mTabTitles.remove(index);
		}
		
		mTabsAdapter.notifyDataSetChanged();
	}


	static ChatItem currentHotelSearch = null;
//	static ChatItem currentFlightSearch = null;
	static ChatItem lastHotelCompleted = null;
//	static ChatItem lastFlightCompleted = null;
	int retries = 0;

	/***
	 * Finished activation of chat-item  (eg. hotel search, flight search, etc...) 
	 */
	class ChatItemDownloaderListener extends DownloaderTaskListener {
		
		ChatItem currentItem;
		boolean switchToResult;
		
		ChatItemDownloaderListener(ChatItem chatItem, boolean switchToResult) {
			currentItem = chatItem;
			this.switchToResult = switchToResult;
		}
		
		
		@Override
		public void endProgressDialog(int id, Object result) {
			Log.i(TAG, "End search for "+currentItem.getChat());
			mainView.hideStatus();
			//currentItem.setSearchResults(result);
			currentItem.setStatus(Status.HasResults);
			retries = 0;
			
			String tabName = getString(id); // Yeah, I'm using the string ID for distinguishing between downloader tasks
			// tabName is HOTELS, FLIGHTS, etc.. depending on chatItem downloader id
			
			if (id == R.string.HOTELS && (VHAApplication.FOUND_HOTELS.size() == 0)) {
				removeTabs();
				Toast.makeText(MainActivity.this, R.string.no_hotels, Toast.LENGTH_LONG ).show();
			}
			else {
				int index = mTabTitles.indexOf(tabName);
				if (index == -1) {
					mTabsAdapter.addTab(tabName);
					index = mTabTitles.size()-1;
				} 

				if (id == R.string.HOTELS) {
					int mapIndex = mTabTitles.indexOf(mMapTabName);
					if (mapIndex == -1) {
						mTabsAdapter.addTab(mMapTabName);
						mapIndex = mTabTitles.size()-1;
					}
					HotelsMapFragment mapFragment = mTabsAdapter.mMapFragment;// instantiateItem(mViewPager, mapIndex);
					mapFragment.onHotelsListUpdated();
					
					mTabsAdapter.removeTab(mHotelTabName);
					mTabsAdapter.removeTab(mRoomsTabName);
					mTabsAdapter.removeTab(mReviewsTabName);
					mTabsAdapter.removeTab(mBookingTabName);
					
					HotelListFragment fragment = mTabsAdapter.mHotelsListFragment; //instantiateItem(mViewPager, index);
					if (fragment != null) {
						fragment.newHotelsList();
					}
					else {
						VHAApplication.logError(TAG, "Unexpected hotel list fragment is null");
					}
				}
				
				if (this.switchToResult) {
					mTabs.setCurrentItem(index);
				}
			}
			
			// this is ugly hack - there can be only one hotel chat item with results, and only one flight search with results
			if (currentItem.getFlowElement().Type == TypeEnum.Hotel) {
				if (lastHotelCompleted != null && lastHotelCompleted != currentItem) {
					lastHotelCompleted.setStatus(Status.ToSearch);
				}
				lastHotelCompleted = currentItem;
			}
//			if (currentItem.getFlowElement().Type == TypeEnum.Flight) {
//				if (lastFlightCompleted != null && lastFlightCompleted != currentItem) {
//					lastFlightCompleted.setStatus(Status.ToSearch);
//				}
//				lastFlightCompleted = currentItem;
//			}
			// if chat items change when search is done - update chat list 
			//invalidateChatFragment();
		}

		@Override
		public void startProgressDialog(int id) {
			Log.i(TAG, "Start search for "+currentItem.getChat());
			currentItem.setStatus(Status.InSearch);
			//invalidateChatFragment();
		}

		@Override
		public void endProgressDialogWithError(int id, Object result) {
			Log.w(TAG, "End search with ERROR for "+currentItem.getChat());
			mainView.hideStatus();
			currentItem.setStatus(Status.ToSearch);
			if (currentItem.getFlowElement().Type == TypeEnum.Hotel) {
				if (retries < 3) {
					retries++;
					Log.w(TAG, "retrying... "+retries); 
					executeFlowElement(currentItem.getEvaReply(), currentItem.getFlowElement(), currentItem, false);
				}
			}
			// invalidateChatFragment();
		}
	}

	public void clearExpediaCache() {
		if (lastHotelCompleted != null) {
			
			lastHotelCompleted.setStatus(Status.ToSearch);
			
			// ??? mViewPager.setAdapter(null);
			//mTabs.setAdapter(null);
			
			removeTabs();
			
			//mSwipeyAdapter.stuffChanged(mTabTitles.indexOf(mChatTabName));
			
			VHAApplication.clearSearch();
		}
	}
	
	private void searchHotels(final ChatItem _chatItem,
			final EvaApiReply _reply,
			final boolean _switchToResult) {
		
		// reached hotel search - no need showing tips & examples next time
		SettingsAPI.setShowIntroTips(this, false);
		
		mSearchExpediaTask = new HotelListDownloaderTask();
		mSearchExpediaTask.initialize(new ChatItemDownloaderListener(_chatItem, _switchToResult), 
				 _reply); // TODO: change to be based on flow element, 
//		if (currentHotelSearch.getStatus() == Status.HasResults) {
//			// this chat item was already activated and has results - bypass the cloud service and fake results
//			mSearchExpediaTask.setCachedResults(_chatItem.getSearchResult());
//		}
//		else {
			mainView.showStatus("Searching for hotels...");
			mSearchExpediaTask.execute();
//		}
	}
	
	class FlashHandler extends Handler {
		MainView mainView;
		public FlashHandler(MainView mainView) {
			this.mainView = mainView;
		}
		@Override
		public void handleMessage(Message msg) {
			mainView.flashSearchButton(3);
			super.handleMessage(msg);
		}
	}
	private static FlashHandler mFlashButton = null;
	
	private void executeFlowElement(EvaApiReply reply, FlowElement flow, ChatItem chatItem, boolean switchToResult) {
//		chatItem.setActivated(true);
		final EvaComponent eva = VHAApplication.EVA;
		
		if (switchToResult && chatItem.sayitActivated == false) { 
			String sayIt = flow.getSayIt();
			if (sayIt != null && !"".equals(sayIt) ) {
				eva.speak(sayIt);
				chatItem.sayitActivated = true;
			}
		}
		
		switch (flow.Type) {
		case Hotel:
			currentHotelSearch = chatItem;
			final EvaApiReply _reply = reply;
			final boolean _switchToResult = switchToResult;
			// if children travelers specified but EAN does not include the ages - ask for ages  
			if (reply.travelers != null && reply.travelers.allChildren() > 0
				&& (reply.ean.containsKey("room1") == false || reply.ean.get("room1").matches("\\d"))
					) {
				Log.d(TAG,"Guests dialog");
				chatItem.sayitActivated = false;
				final ChatItem _chatItem = chatItem; 
				final FlowElement _flow = flow;
				eva.speak("Please enter the age of the children");

				ChildAgeDialogFragment dialog = new ChildAgeDialogFragment();
				dialog.setTravelers(reply.travelers);
				
				dialog.setListener(new ChildAgeDialogFragment.ChildAgeDialogListener() {
					
					@Override
					public void onDialogPositiveClick(ChildAgeDialogFragment dialog) {
						String sayIt = _flow.getSayIt();
						if (sayIt != null && !"".equals(sayIt) ) {
							eva.speak(sayIt);
							_chatItem.sayitActivated = true;
						}
						searchHotels(_chatItem, _reply, _switchToResult);
					}
					
					@Override
					public void onDialogNegativeClick(ChildAgeDialogFragment dialog) {
						Toast.makeText(MainActivity.this, R.string.children_ages_required, Toast.LENGTH_LONG).show();
					}
				});
		        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
				
			}
			else {
				 Log.d(TAG, "Running Hotel Search!");
				 if (reply.travelers != null) {
					 // no children
					 VHAApplication.numberOfAdults = reply.travelers.allAdults();
					 VHAApplication.childAges = null;
				 }
				 else {
					 // no travelers info - default 2 adults
					 VHAApplication.numberOfAdults = 2;
					 VHAApplication.childAges = null;
				 }
				 
				 searchHotels(chatItem, _reply, _switchToResult);
			}

			break;
//		case Flight:
//			Log.d(TAG, "Running Vayant Search!");
//			currentFlightSearch = chatItem;
//			mSearchVayantTask = new SearchVayantTask(this, reply, flow);
//			mSearchVayantTask.attach(new DownloaderListener(chatItem, switchToResult));
////			if (chatItem.getStatus() == Status.HasResults) {
////				// this chat item was already activated and has results - bypass the cloud service and fake results
////				mSearchVayantTask.setCachedResults(chatItem.getSearchResult());
////			}
////			else {
//				mSearchVayantTask.execute();
////			}
//			break;
		case Question:
			// flash the microphone button
			Log.d(TAG, "Question asked");
			// give some delay to the flashing - to happen while question is asked
			if (mFlashButton == null) {
				 mFlashButton = new FlashHandler(mainView);
			}
			mFlashButton.sendEmptyMessageDelayed(1, 2000);
			break;
		
		}
	}


	@Inject private ChatItemList mChatListModel;

	/**
	 * Listener to Room-information request complete
	 */
	private class RoomTaskListener extends DownloaderTaskListener {
		private boolean mSwitchToTab = false;

		@Override
		public void endProgressDialog(int id, Object result) { // we got the hotel rooms reply successfully
			Log.d(TAG, "endProgressDialog() for hotel rooms for hotel "+mRoomUpdater.hotelId);
			mainView.hideStatus();
			
			String tabName = mRoomsTabName; 
			int index = mTabTitles.indexOf(tabName);
			if (index == -1) {
				mTabsAdapter.addTab(tabName);
				index = mTabTitles.size()-1;
			}
			RoomsSelectFragement fragment = mTabsAdapter.mRoomSelectFragment; //instantiateItem(mViewPager, index);
			if (fragment != null) // could be null if not instantiated yet
			{
				fragment.changeHotelId(mRoomUpdater.hotelId);
				if (mSwitchToTab) {
					mTabs.setCurrentItem(index);
					mSwitchToTab  = false;
				}
			}
			mRoomUpdater = null;
		}
		
		@Override
		public void endProgressDialogWithError(int id, Object result) {
//			setDebugData(DebugTextType.ExpediaDebug, result);
			mainView.hideStatus();
			if (mRoomUpdater.eanWsError != null) {
				EanWsError err = mRoomUpdater.eanWsError;
				if ("SOLD_OUT".equals(err.category)) {
					int index = mTabTitles.indexOf(mHotelTabName);
					if (index != -1) {
						HotelDetailFragment fragment = mTabsAdapter.mHotelDetailFragment; //instantiateItem(mViewPager, index);
						if (fragment != null) // could be null if not instantiated yet
						{
							fragment.hotelSoldOut();
						}
					}
					if (err.presentationMessage.equals("") == false)
						Toast.makeText(MainActivity.this, err.presentationMessage, Toast.LENGTH_LONG).show();
				}
			}
			mRoomUpdater = null;
		}

		public void switchToTab() {
			mSwitchToTab  = true;
		}
	};
	
	private RoomTaskListener mRoomUpdaterListener = new RoomTaskListener(); 
	
	/****
	 * Listening to end of hotel-details request
	 */
	private DownloaderTaskListener mHotelDownloadListener = new DownloaderTaskListener() {
		
		@Override
		public void endProgressDialog(int id, Object result) { // we got the hotel details reply successfully
			mainView.hideStatus();
//			setDebugData(DebugTextType.ExpediaDebug, result);
			
			long hotelId = mHotelDownloader.getHotelId();
			VHAApplication.selectedHotel = VHAApplication.HOTEL_ID_MAP.get(hotelId);
			Log.d(TAG, "endProgressDialog() Hotel # " + hotelId+ " - "+VHAApplication.selectedHotel.name);

			onEventHotelsListUpdated(null);

			// add hotel tab again
			String tabName = mHotelTabName; 
			int index = mTabTitles.indexOf(tabName);
			if (index == -1) {
				mTabsAdapter.addTab(tabName);
				index = mTabTitles.size() - 1;
			}
	
			HotelDetailFragment fragment = mTabsAdapter.mHotelDetailFragment; //instantiateItem(mViewPager, index);
			if (fragment != null) // could be null if not instantiated yet
			{
				fragment.changeHotelId(hotelId);
			}

			index = mTabTitles.indexOf(tabName);
			mTabs.setCurrentItem(index);

			index = mTabTitles.indexOf(mReviewsTabName);
			if (index == -1) {
				mTabsAdapter.addTab(mReviewsTabName);
			}
			ReviewsFragment reviews = mTabsAdapter.mReviewsFragment;
			reviews.hotelChanged(hotelId);

			
			startRoomSearch(hotelId);
			mHotelDownloader = null;
		}

		@Override
		public void endProgressDialogWithError(int id, Object result) {
//			setDebugData(DebugTextType.ExpediaDebug, result);
			mainView.hideStatus();
			if (mHotelDownloader.eanWsError != null) {
				EanWsError err = mHotelDownloader.eanWsError;
				if (err.recoverable && err.presentationMessage.equals("") == false) {
					Toast.makeText(MainActivity.this, err.presentationMessage, Toast.LENGTH_LONG).show();
				}
			}
			mHotelDownloader = null;
		}
		
	};
	
	/****
	 * Start new session from menu item 
	 */
	private void startNewSession() {
//		if (isNewSession() == false) {
			mTabsAdapter.showTab(mChatTabName);
			VHAApplication.EVA.resetSession();
			ChatItem myChat = new ChatItem("Start new search");
			addChatItem(myChat);
			String greeting = "Starting a new search. How may I help you?";
			
			int pos = greeting.length();
			String seeExamples = "\nClick here to see some examples.";
			SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
			int col = getResources().getColor(R.color.vha_chat_no_session_text);
			sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
			sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
			ChatItem chat = new ChatItem(sgreet,null, null, ChatType.VirtualAgentWelcome);
			addChatItem(chat);
			VHAApplication.EVA.speak(greeting);
			
			mainView.flashSearchButton(3);

//		}
	}
	
	private void cancelBackgroundThreads() {
		if (mSearchExpediaTask != null) {
			mSearchExpediaTask.cancel(true);
			mSearchExpediaTask = null;
		}
		if (mHotelDownloader != null) {
			mHotelDownloader.cancel(true);
			mHotelDownloader = null;
		}
		if (mRoomUpdater != null) {
			mRoomUpdater.cancel(true);
			mRoomUpdater = null;
		}
		VHAApplication.EVA.stopSearch();
		if (speechSearch != null) {
			speechSearch.cancel();
		}
		mainView.hideSpeechWave();
	}
	
	
	/****
	 * handler for  "new session was started" event 
	 * this is either in response to menu click or to a server side initiated new session
	 */
	@Override
	public void newSessionStarted(boolean selfTriggered) {
		
		cancelBackgroundThreads();
		if (VHAApplication.AcraInitialized) {
			ErrorReporter bugReporter = ACRA.getErrorReporter();
			String itemsStr = bugReporter.getCustomData(ITEMS_IN_SESSION);
			int items;
			if (itemsStr == null) {
				items = 0;
			}
			else {
				items = Integer.parseInt(itemsStr);
			}
			bugReporter.putCustomData(ITEMS_IN_SESSION, "0");
	
			for (int i=0; i<items; i++) {
				bugReporter.removeCustomData("eva_session_"+i);
			}
		}
		
		
		mainView.hideStatus();
		//S3DrawableBackgroundLoader.getInstance().Reset();
		
//		for (ChatItem chatItem : mChatListModel.getItemList()) {
//			chatItem.setInSession(false);
//		}
		clearChatList();
//		lastFlightCompleted = null;
		lastHotelCompleted = null;

		mTabsAdapter.showTab(mChatTabName);
		// ??? mViewPager.setAdapter(null);
		//mTabs.setAdapter(null);
		
		removeTabs();

		VHAApplication.clearSearch();
	}
	


	// note "onEvent" template is needed for progruard to not break roboguice
	// this event happens after a next page of hotel list results is downloaded
	public void onEventHotelsListUpdated( /*@Observes*/ HotelsListUpdated event) {
		int mapTabIndex = mTabTitles.indexOf(mMapTabName);
		if (mapTabIndex != -1) {
			HotelsMapFragment frag = (HotelsMapFragment)  mTabsAdapter.mMapFragment; //instantiateItem(mViewPager, mapTabIndex);
			if (frag != null) {
				frag.onHotelsListUpdated();
			}
		}
	}
	
	public void onEventRatingClicked( /*@Observes */RatingClickedEvent event) {
		int reviewsIndex = mTabTitles.indexOf(mReviewsTabName);
		if (reviewsIndex != -1)
			mTabs.setCurrentItem(reviewsIndex);
	}
	
	public void onEventBookingCompleted( /*@Observes */BookingCompletedEvent event) {
		int reservationTabIndex = mTabTitles.indexOf(mReservationsTabName);
		if (reservationTabIndex == -1) {
			mTabsAdapter.addTab(mReservationsTabName);
			reservationTabIndex = mTabTitles.size() - 1;
		}
		
		ReservationDisplayFragment frag = (ReservationDisplayFragment)  mTabsAdapter.mReservationFragment; //instantiateItem(mViewPager, reservationTabIndex);
		if (frag != null) {
			frag.showLatestReservation();
		}
		mTabs.setCurrentItem(reservationTabIndex);
	}
	
	
	public void onEventRoomSelected( /*@Observes*/ RoomSelectedEvent event) {

		Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(event.hotelId);
		VHAApplication.selectedRoom = event.room;
		
		int bookingIndex = mTabTitles.indexOf(mBookingTabName);
		if (bookingIndex == -1) {
			mTabsAdapter.addTab(mBookingTabName);
			bookingIndex = mTabTitles.size() - 1;
		}
		BookingFragement frag = (BookingFragement)  mTabsAdapter.mBookingFragment; //instantiateItem(mViewPager, bookingIndex);
		if (frag != null) {
			frag.changeHotelRoom(hotel, event.room);
		}
		mTabs.setCurrentItem(bookingIndex);
	}
	
	public void onEventHotelItemClicked( /*@Observes*/ HotelItemClicked event) {
		Log.d(TAG, "onHotelItemClicked("+event.hotelIndex+")");
		if (VHAApplication.FOUND_HOTELS.size() <= event.hotelIndex) {
			VHAApplication.logError(TAG, "clicked index "+event.hotelIndex+ " but size is "+VHAApplication.FOUND_HOTELS.size());
			return;
		}

		if (mHotelDownloader != null) {
			if (false == mHotelDownloader.cancel(true)) {
				Log.d(TAG, "false == mHotelDownloader.cancel(true)");
				mHotelDownloader = null;
			}
		}
		
		Hotel hotel = VHAApplication.FOUND_HOTELS.get(event.hotelIndex);
		HotelInformation info = VHAApplication.EXTENDED_INFOS.get(hotel.hotelId);
		mHotelDownloader = new HotelDownloaderTask(mHotelDownloadListener, hotel.hotelId);
		if (info != null) {
			Log.d(TAG, "Loaded info for hotel "+hotel.name+" from cache");
			// restored from cache - fake downloader progress
			mHotelDownloadListener.endProgressDialog(R.string.HOTEL, null);
		}
		else {
			mainView.showStatus("Getting Hotel info...");
			Log.d(TAG, "Getting info for hotel "+hotel.name);
			//this.endProgressDialog(R.string.HOTEL, "fake response");
			mHotelDownloader.execute();
		}
	}
	
	public void onEventChatItemModified( /*@Observes*/ ChatItemModified event) {
		if (event.startRecord) {
			if (event.chatItem == null) {
				VHAApplication.logError(TAG, "Unexpected chatItem=null startRecord");
				return;
			}
			voiceRecognitionSearch(event.chatItem, event.editLastUtterance);
		}
		else {
			String searchText;
			Object cookie = null;
			if (event.chatItem == null) {
				// removed last item
				searchText = "";
				cookie = DELETED_UTTERANCE_COOKIE;
			}
			else {
				searchText = event.chatItem.getChat().toString();
				cookie = TEXT_TYPED_COOKIE;
			}
			VHAApplication.EVA.searchWithText(searchText, cookie, event.editLastUtterance);
		}
	}
	
	public void onEventHotelSelected( /*@Observes*/ HotelSelected event) {
		Log.d(TAG, "onHotelSelected("+event.hotelId+")");

		int index = mTabTitles.indexOf(mRoomsTabName);
		if (index == -1) {
			// no rooms tab - will be soon - so mark as switch to it
			if (mRoomUpdater == null) {
				startRoomSearch(event.hotelId);
			}
			mRoomUpdaterListener.switchToTab();
		}
		else {
			// room fragment is available - this is in sync with the selected hotel
			mTabs.setCurrentItem(index);
		}
	}
	
	public void onEventToggleMainButtons( /*@Observes*/ ToggleMainButtonsEvent event) {
		mainView.toggleMainButtons(event.showMainButtons);
	}
		
	private void startRoomSearch(long hotelId) {
		if (mRoomUpdater != null) {
			if (false == mRoomUpdater.cancel(true)) {
				Log.d(TAG, "false == mRoomUpdater.cancel(true)");
				mRoomUpdater = null;
				// return;
			}
		}
		
		mTabsAdapter.removeTab(mRoomsTabName);
		mTabsAdapter.removeTab(mBookingTabName);
		mTabsAdapter.removeTab(mReservationsTabName);
		mRoomUpdater = new RoomsUpdaterTask(hotelId);
		mRoomUpdater.attach(mRoomUpdaterListener);
		List<HotelRoom> rooms = VHAApplication.HOTEL_ROOMS.get(hotelId);
		if (rooms != null) {
			// restored from cache - fake downloader progress
			mRoomUpdaterListener.endProgressDialog(-1, null);
		}
		else {
			mainView.showStatus("Getting Rooms info for hotel");
			mRoomUpdater.execute();
		}
	}



	public void hotelsFragmentVisible() {
		// ugly hack to trigger sayit...
		if (currentHotelSearch != null && currentHotelSearch.sayitActivated == false) {
			String sayIt = currentHotelSearch.getFlowElement().getSayIt();
			if (sayIt != null && !"".equals(sayIt) ) {
				VHAApplication.EVA.speak(sayIt);
				currentHotelSearch.sayitActivated = true;
			}
		}
	}
//
//	public void flightsFragmentVisible() {
//		if (currentFlightSearch != null && currentFlightSearch.sayitActivated == false) {
//			String sayIt = currentFlightSearch.getFlowElement().getSayIt();
//			if (sayIt != null && !"".equals(sayIt) ) {
//				VHAApplication.EVA.speak(sayIt);
//				currentFlightSearch.sayitActivated = true;
//			}
//		}
//	}


	
}
// TODO: I took the microphone icon from: http://www.iconarchive.com/show/atrous-icons-by-iconleak/microphone-icon.html
// Need to add attribution in the about text.
// TODO: very cool: http://code.google.com/p/google-gson/
// Gson is a Java library that can be used to convert Java Objects into their JSON representation. It can also be used
// to convert a JSON string to an equivalent Java object. Gson can work with arbitrary Java objects including
// pre-existing objects that you do not have source-code of.
/*
 * 
 * Example of google speech recognition:
 * http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/VoiceRecognition.html
 * Connecting to the network: http://developer.android.com/training/basics/network-ops/connecting.html
 */

// Translate: Bing primary key = "uMLqpa+YkdRvJHukpbt06yQNa+ozPiGwrKSwnvjBYh4="

// I think a crash is caused if I start a speech recognition activity and I rotate the screen.
// The result is delivered to a dead activity and I get 