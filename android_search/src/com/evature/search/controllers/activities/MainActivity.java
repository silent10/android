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

package com.evature.search.controllers.activities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.json.JSONException;

import roboguice.event.Observes;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.evaapis.EvaApiReply;
import com.evaapis.EvaBaseActivity;
import com.evaapis.EvaDialog.DialogElement;
import com.evaapis.EvaWarning;
import com.evaapis.events.NewSessionStarted;
import com.evaapis.flow.FlowElement;
import com.evaapis.flow.FlowElement.TypeEnum;
import com.evaapis.flow.QuestionElement;
import com.evature.components.MyViewPager;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.events.ChatItemClicked;
import com.evature.search.controllers.events.HotelsListUpdated;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaHotelDownloaderTask;
import com.evature.search.controllers.web_services.HotelListDownloaderTask;
import com.evature.search.controllers.web_services.SearchTravelportTask;
import com.evature.search.controllers.web_services.SearchVayantTask;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItem.ChatType;
import com.evature.search.models.chat.ChatItem.Status;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.models.chat.DialogAnswerChatItem;
import com.evature.search.models.chat.DialogQuestionChatItem;
import com.evature.search.views.SwipeyTabs;
import com.evature.search.views.adapters.SwipeyTabsAdapter;
import com.evature.search.views.adapters.TrainListAdapter;
import com.evature.search.views.fragments.ChatFragment;
import com.evature.search.views.fragments.ChatFragment.DialogClickHandler;
import com.evature.search.views.fragments.DebugFragment;
import com.evature.search.views.fragments.ExamplesFragment;
import com.evature.search.views.fragments.ExamplesFragment.ExampleClickedHandler;
import com.evature.search.views.fragments.FlightsFragment;
import com.evature.search.views.fragments.HotelFragment;
import com.evature.search.views.fragments.HotelsFragment;
import com.evature.search.views.fragments.HotelsMapFragment;
import com.evature.search.views.fragments.TrainsFragment;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MainActivity extends EvaBaseActivity implements 
													TextToSpeech.OnInitListener, 
													EvaDownloaderTaskInterface {

	private static final String ITEMS_IN_SESSION = "items_in_session";


	private static final String TAG = MainActivity.class.getSimpleName();
	// private static String mExternalIpAddress = null;
	
	
	private static boolean mSpeechToTextWasConfigured = false;
	private List<String> mTabTitles;
	@Inject Injector injector;
	
	@InjectView(R.id.viewpager) private MyViewPager mViewPager; // see http://blog.peterkuterna.net/2011/09/viewpager-meets-swipey-tabs.html
	@InjectView(R.id.swipeytabs) private SwipeyTabs mTabs; // The main swipey tabs element and the main view pager element:
	SearchVayantTask mSearchVayantTask;
	SearchTravelportTask mSearchTravelportTask;
	SwipeyTabsPagerAdapter mSwipeyAdapter;
	HotelListDownloaderTask mSearchExpediaTask;
		
	private boolean mIsNetworkingOk = false;

	private String mChatTabName;

	private String mExamplesTabName;
	private String mDebugTabName;
	private String mHotelsTabName;

	private String mHotelTabName;

	static EvaHotelDownloaderTask mHotelDownloader = null;
	

	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		setDebugData(DebugTextType.None, "");
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		if (mHotelDownloader != null) {
			mHotelDownloader.cancel(true);
			mHotelDownloader = null;
		}
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Called when the activity is first created.
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_main);
		
		mChatTabName = getString(R.string.CHAT);
		mExamplesTabName = getString(R.string.EXAMPLES);
		mDebugTabName = getString(R.string.DEBUG);
		mHotelsTabName = getString(R.string.HOTELS);
		mHotelTabName = getString(R.string.HOTEL);

		
		if (savedInstanceState != null) { // Restore state
			// Same code as onRestoreInstanceState() ?
			Log.d(TAG, "restoring saved instance state");
			mTabTitles = savedInstanceState.getStringArrayList("mTabTitles");
		} else {
			Log.d(TAG, "no saved instance state");
			mTabTitles = new ArrayList<String>(Arrays.asList(mExamplesTabName, mChatTabName));
		}
		
		
		

//		getActionBar().setDisplayHomeAsUpEnabled(true);
		mSwipeyAdapter = new SwipeyTabsPagerAdapter(this, getSupportFragmentManager(), mViewPager, mTabs);
		mViewPager.setAdapter(mSwipeyAdapter);
		mTabs.setAdapter(mSwipeyAdapter);
		mViewPager.setOnPageChangeListener(mTabs); // To sync the tabs with the viewpager
		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (of the second argument) is called after initialization completes.
	
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

		mViewPager.setCurrentItem(mTabTitles.indexOf(mChatTabName));

		// patch for debug - bypass the speech recognition:
		// Intent data = new Intent();
		// Bundle a_bundle = new Bundle();
		// ArrayList<String> sentences = new ArrayList<String>();
		// sentences.add("3 star hotel in rome");
		// a_bundle.putStringArrayList(RecognizerIntent.EXTRA_RESULTS, sentences);
		// data.putExtras(a_bundle);
		// onActivityResult(VOICE_RECOGNITION_REQUEST_CODE, RESULT_OK, data);

	}

	// Using FragmentStatePagerAdapter to overcome bug: http://code.google.com/p/android/issues/detail?id=19001
	// This is an uglier approach I did not use: http://stackoverflow.com/a/7287121/78234
	public class SwipeyTabsPagerAdapter extends FragmentStatePagerAdapter implements SwipeyTabsAdapter,
			ViewPager.OnPageChangeListener {
		// Nicer example: http://developer.android.com/reference/android/support/v4/view/ViewPager.html
		private final Context mContext;
		private final ViewPager mViewPager;
		private final String TAG = SwipeyTabsPagerAdapter.class.getSimpleName();

		public SwipeyTabsPagerAdapter(Context context, FragmentManager fm, ViewPager pager, SwipeyTabs tabs) {
			super(fm);
			Log.i(TAG, "CTOR");
			mViewPager = pager;
			mViewPager.setAdapter(this);
			mViewPager.setOnPageChangeListener(this);
			this.mContext = context;
		}

		@Override
		public Fragment getItem(int position) {// Asks for the main fragment
			Log.i(TAG, "getItem " + String.valueOf(position));
			int size = mTabTitles.size();
			if (position >= size) {
				Log.e(TAG, "No fragment made for Position "+position);
				return null;
			}
			if (mTabTitles.get(position).equals(mDebugTabName)) { // debug window
				Log.d(TAG, "Debug Fragment");
				DebugFragment fragment = injector.getInstance(DebugFragment.class);
				return fragment;
			}
			
			if (mTabTitles.get(position).equals(mExamplesTabName)) { // Examples window
				Log.d(TAG, "Example Fragment");
				ExamplesFragment fragment = injector.getInstance(ExamplesFragment.class);
				fragment.setHandler(new ExampleClickedHandler() {
					@Override
					public void onClick(String example) {
						Log.d(TAG, "Running example: "+example);
						MainActivity.this.addChatItem(new ChatItem(example));
						mViewPager.setCurrentItem(mTabTitles.indexOf(mChatTabName), true);
						MainActivity.this.searchWithText(example);
					}});
				return fragment; 
			}
			if (mTabTitles.get(position).equals(mChatTabName)) { // Main Chat window
				Log.i(TAG, "Chat Fragment");
				ChatFragment chatFragment = injector.getInstance(ChatFragment.class);
				chatFragment.setDialogHandler(new DialogClickHandler() {
					
					@Override
					public void onClick(SpannableString dialogResponse, int responseIndex) {
						MainActivity.this.addChatItem(new ChatItem(dialogResponse));
						MainActivity.this.replyToDialog(responseIndex);
						//MainActivity.this.searchWithText(dialogResponse);
					}
				});
				return chatFragment;
			}
			if (mTabTitles.get(position).equals(mHotelsTabName)) { // Hotel list window
				Log.i(TAG, "Hotels Fragment");
				return injector.getInstance(HotelsFragment.class);
			}
			
			if (mTabTitles.get(position).equals(getString(R.string.HOTELS_MAP))) { // Hotel list window
				Log.i(TAG, "HotelsMap Fragment");
				Fragment fragment= injector.getInstance(HotelsMapFragment.class);
				return fragment;
			}
			
			if (mTabTitles.get(position).equals(getString(R.string.FLIGHTS))) { // flights list
				Log.i(TAG, "Flights Fragment");
				return injector.getInstance(FlightsFragment.class);
			}
			
			if (mTabTitles.get(position).equals(mHotelTabName)) { // Single hotel
				int hotelIndex = MyApplication.getDb().getHotelId();
				Log.i(TAG, "starting hotel Fragment for hotel # " + hotelIndex);
				return HotelFragment.newInstance(hotelIndex);
			}
			if (mTabTitles.get(position).equals(getString(R.string.TRAINS))) { // trains list window
				Log.i(TAG, "Trains Fragment");
				return injector.getInstance(TrainsFragment.class);
			}

			Log.e(TAG, "No fragment made for Position "+position+(position< size ? " titled "+mTabTitles.get(position) : ""));
			return null;
		}

		@Override
		public int getCount() {
			int count = mTabTitles.size();
			// Log.i(TAG, "getCount() " + String.valueOf(count));
			return count;
		}

		public TextView getTab(final int position, SwipeyTabs root) { // asks for just the tab part
			Log.i(TAG, "getTab() " + String.valueOf(position));
			TextView view = (TextView) LayoutInflater.from(mContext)
					.inflate(R.layout.swipey_tab_indicator, root, false);
			view.setText(mTabTitles.get(position));
			view.setOnClickListener(new OnClickListener() { // You can swipe AND click on a specific tab
				public void onClick(View v) {
					mViewPager.setCurrentItem(position, true);
				}
			});
			return view;
		}
		
//		@Override
//		public Object instantiateItem(ViewGroup container, int position) {
//			Object item = super.instantiateItem(container, position);
//			this.finishUpdate(container);
//			return item;
//		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageSelected(int arg0) {
		}

		@Override
		public void notifyDataSetChanged() {
			Log.i(TAG, "notifyDataSetChanged()");
			mTabs.setAdapter(this);
			super.notifyDataSetChanged();
		}

		// Internal helper function
		public void stuffChanged(int position) {
			Log.d(TAG, "stuffChanged "+position);
			mTabs.setAdapter(mSwipeyAdapter);
			mViewPager.setAdapter(mSwipeyAdapter); // I crashed here once ?! java.lang.IllegalStateException: Fragment
													// ChatFragment{41ac6dd0} is not currently in the FragmentManager
			this.notifyDataSetChanged();
			mTabs.onPageSelected(position);
			mViewPager.setCurrentItem(position, true);
		}

		public void addTab(String name) { // Dynamic tabs add to end
			Log.d(TAG, "addTab "+name);
			int position = mViewPager.getCurrentItem();
			mTabs.setAdapter(null);
			mTabTitles.add(name);
			stuffChanged(position);
		}
		
		public void addTab(String name, int position) { // Dynamic tabs add to certain position
			Log.d(TAG, "addTab "+name);
			mTabs.setAdapter(null);
			mTabTitles.add(position, name);
			stuffChanged(position);
		}
		

		public void removeTab() { // Dynamic tabs remove from end
			Log.d(TAG, "removeTab");
			int position = mViewPager.getCurrentItem();
			int size = mTabTitles.size();
			if (size > 0) { // fast clicking on remove gets us here...
				if (position == size - 1) { // We are at the last tab
					position = position - 1; // Move to the NEW last tab
				}
				mTabs.setAdapter(null);
				mTabTitles.remove(size - 1);
				stuffChanged(position);
			}
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
			
			if(tabIndex!=0)
			{
				stuffChanged(tabIndex-1);
			}
			else
			{
				stuffChanged(tabIndex);
			}
		}

	}
	
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
		menu.getItem(2).setVisible(mDebug);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // user pressed the menu button
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			mViewPager.setCurrentItem(1);
			return true;
		case R.id.new_session:
			startNewSession();
			return true;
		case R.id.settings: // Did the user select "settings"?
			intent = new Intent();
			// Then set the activity class that needs to be launched/started.
			intent.setClass(this, MyPreferences.class);
			Bundle a_bundle = new Bundle(); // Lets send some data to the preferences activity
		//	a_bundle.putStringArrayList("mLanguages", (ArrayList<String>) mSpeechRecognition.getmGoogleLanguages());
			intent.putExtras(a_bundle);
			startActivity(intent);
			return true;
		case R.id.bug_report:
			// Then set the activity class that needs to be launched/started.
			intent = new Intent(this, BugReportDialog.class);
			startActivity(intent);
			return true;
		case R.id.about: // Did the user select "About us"?
			// Links in alertDialog:
			// http://stackoverflow.com/questions/1997328/android-clickable-hyperlinks-in-alertdialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.app_name));
			final TextView message = new TextView(this);
			final SpannableString s = new SpannableString(this.getText(R.string.lots_of_text));
			Linkify.addLinks(s, Linkify.WEB_URLS);
			message.setText(s);
			message.setMovementMethod(LinkMovementMethod.getInstance());
			message.setPadding(10, 10, 10, 10);
			builder.setView(message);
			builder.setPositiveButton(getString(R.string.ok_button), null);
			builder.setCancelable(false); // Can you just press back and dismiss it?
			builder.create().show();
			return true;
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
			Log.e(TAG, "Illegal state while saving instance state in main activity", e);
		}

		savedInstanceState.putBoolean("mTtsWasConfigured", mSpeechToTextWasConfigured);
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
		mSpeechToTextWasConfigured = savedInstanceState.getBoolean("mTtsWasConfigured");
	}
	
	private void addChatItem(ChatItem item) {
		Log.d(TAG, "Adding chat item  type = "+item.getType()+ "  '"+item.getChat()+"'");
		mChatListModel.add(item);
		invalidateChatFragment();
	}
	
	private void invalidateChatFragment() {
		int index = mTabTitles.indexOf(mChatTabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(mChatTabName);
			index = mTabTitles.size() - 1;
		}
		Log.i(TAG, "Chat tab at index "+index);
		// http://stackoverflow.com/a/8886019/78234
		ChatFragment fragment = (ChatFragment) mSwipeyAdapter.instantiateItem(mViewPager, index);
		if (fragment != null) // could be null if not instantiated yet
		{
			fragment.invalidate();
//			if (fragment.getView() != null) {
//				fragment.addChatItem(item);
//			} else {
//				Log.e(TAG, "chat fragment.getView() == null!?!");
//			}
		} 
		else {
			Log.w(TAG, "chat fragment == null!?");
		}

	}
	

	private String handleChat(EvaApiReply apiReply) {
		if (!apiReply.isFlightSearch() && !apiReply.isHotelSearch() && (apiReply.chat != null)) {
			if (apiReply.chat.hello != null && apiReply.chat.hello) {
				return "Why, Hello there!";
			}
			if (apiReply.chat.who != null && apiReply.chat.who) {
				return "I'm Eva, your travel search assistant";
			}
			if (apiReply.chat.meaningOfLife != null && apiReply.chat.meaningOfLife) {
				return "Disrupting travel search, of course!";
			}
		}
		return null;
	}


	public void setVayantReply(String response) {
		setDebugData(DebugTextType.VayantDebug, response);
		showTab(R.string.FLIGHTS);
	}
	
	private int showTab(int tabNameId) {
		String tabName = getString(tabNameId);
		int index = mTabTitles.indexOf(tabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(tabName);
			index = mTabTitles.size() - 1;
		}
		mSwipeyAdapter.stuffChanged(index);
		mViewPager.setCurrentItem(index, true);
		return index;
	}

	public void setTravelportReply(boolean train) {
		// get the fragment: http://stackoverflow.com/a/7393477/78234
		int string_id = train ? R.string.TRAINS : R.string.FLIGHTS;
		int index = showTab(string_id);
		String tag = "android:switcher:" + R.id.viewpager + ":" + index; // wtf...
		if (train) {
			TrainsFragment fragment = (TrainsFragment) getSupportFragmentManager().findFragmentByTag(tag);
			if (fragment != null) // could be null if not instantiated yet
			{
				TrainListAdapter adapter = fragment.getAdapter();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					Log.d(TAG, "TrainListAdapter notifyDataSetChanged()");
				}
				// fragment.updateDisplay();
			} else {
				Log.e(TAG, "Trains fragment == null!?!");
			}
		} else {
			FlightsFragment fragment = (FlightsFragment) getSupportFragmentManager().findFragmentByTag(tag);
			if (fragment != null) // could be null if not instantiated yet
			{
				// fragment.updateDisplay();
//				FlightListAdapterTP adapter = fragment.getAdapter();
//				if (adapter != null) {
//					adapter.notifyDataSetChanged();
//					Log.d(TAG, "Flights fragment adapter notifyDataSetChanged()");
//				} else {
//					Log.e(TAG, "Flights fragment adapter == null!?!");
//				}
			} else {
				Log.e(TAG, "Flights fragment == null!?!");
			}
		}
	}

	
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

	@Override
	public void endProgressDialog(int id, String result) { // we got the hotel search reply successfully
		Log.d(TAG, "endProgressDialog() for id " + id);

		setDebugData(DebugTextType.ExpediaDebug, result);
		
		if (id == R.string.HOTEL && mHotelDownloader != null) {
			int hotelIndex = mHotelDownloader.getHotelIndex();
			Log.d(TAG, "endProgressDialog() Hotel # " + hotelIndex);
			MyApplication.getDb().setHotelId(hotelIndex);
		}

		String tabName = getString(id); // Yeah, I'm using the string ID for distinguishing between downloader tasks
		
		int index = mTabTitles.indexOf(tabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(tabName);
			index = mTabTitles.size() - 1;
			
			if(tabName.equals("HOTELS"))
			{
				mSwipeyAdapter.addTab("MAP");
			}
		} else if (id == R.string.HOTEL) {
			// remove hotel tab and add it again
			mSwipeyAdapter.removeTab();
			mSwipeyAdapter.addTab(tabName);
			// HotelFragment fragment = (HotelFragment) adapter.instantiateItem(mViewPager, index);
			// fragment.mAdapter.notifyDataSetChanged();
			// I need to invalidate the entire view somehow!!!
			mSwipeyAdapter.stuffChanged(index);
		}
		if (id == R.string.HOTELS) {
			HotelsFragment fragment = (HotelsFragment) mSwipeyAdapter.instantiateItem(mViewPager, index);
			if (fragment != null && fragment.getAdapter() != null) {
				fragment.getAdapter().notifyDataSetChanged();
			}
			else {
				Log.w(TAG, "Unexpected hotel fragment or fragment adaptor are null");
			}
			HotelsMapFragment mapFragment = (HotelsMapFragment) mSwipeyAdapter.instantiateItem(mViewPager, index+1);
			
			mSwipeyAdapter.removeTab(mHotelTabName);
		}

		mViewPager.setCurrentItem(index, true);

		// mAdapter = new HotelListAdapter(mHotelsFragment, MyApplication.getDb());

		// if (mEnabledPaging && mFooterView != null)
		// mHotelListView.removeFooterView(mFooterView);
		//
		// mEnabledPaging = false;
		// mPaging = false;
		//
		// if (EvaSearchApplication.getDb().mMoreResultsAvailable) {
		// LayoutInflater li = getActivity().getLayoutInflater();
		// hideKeyboard();
		// mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
		// mHotelListView.addFooterView(mFooterView);
		// mHotelListView.setOnScrollListener(mListScroll);
		// mEnabledPaging = true;
		// }

		// mHotelListView.setAdapter(mAdapter);
	}

	@Override
	public void startProgressDialog(int id) {
	}

	@Override
	public void endProgressDialogWithError(int id, String result) {
		setDebugData(DebugTextType.ExpediaDebug, result);
	}

	@Override
	public void updateProgress(int id, DownloaderStatus mProgress) {
	}

	// search button click handler ("On Click property" of the button in the xml)
	// http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.search_button:
//		    MainActivity.this.searchWithText("3 star Hotels in NYC on July 5th to July 16th");
			MainActivity.this.searchWithVoice();
			break;
		}
	}




	public void showHotelDetails(int hotelIndex) {
		Log.d(TAG, "showHotelDetails()");
		if (MyApplication.getDb() == null) {
			Log.w(TAG, "MyApplication.getDb() == null");
			return;
		}

		if (mHotelDownloader != null) {
			if (false == mHotelDownloader.cancel(true)) {
				Log.d(TAG, "false == mHotelDownloader.cancel(true)");
				// return;
			}
		}

		mHotelDownloader = new EvaHotelDownloaderTask(this, hotelIndex);
		this.endProgressDialog(R.string.HOTEL, "fake response");
		mHotelDownloader.execute();

	}

	int mDebugTab = -1;
	private String lastEvaReply;
	private String lastVayantReply;
	private String lastExpediaReply;
	enum DebugTextType {
		None,
		EvaDebug,
		VayantDebug,
		ExpediaDebug
	}
	
	private void setDebugData(DebugTextType debugType, String txt) {
		switch (debugType) {
		case EvaDebug:
			lastEvaReply = txt;
			break;
		case VayantDebug:
			lastVayantReply = txt;
			break;
		case ExpediaDebug:
			lastExpediaReply = txt;
			break;
		}

		
		if (false == mDebug) {
			if (mDebugTab != -1) {
//				DebugFragment fragment = (DebugFragment) mSwipeyAdapter
//						.instantiateItem(mViewPager, mDebugTab);
//				if (fragment != null) {
//					FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//					ft.hide(fragment);
//					ft.commit();
//				}
				mSwipeyAdapter.removeTab(mDebugTab);
				mDebugTab = -1;
			}
			return;
		}
		
		if (mDebugTab == -1) {
			mDebugTab = mTabTitles.indexOf(mDebugTabName);
			if (mDebugTab == -1) {
				try {
					mSwipeyAdapter.addTab(mDebugTabName, 1);
				} catch (IllegalStateException e) {
					Log.e(TAG, "Exception of IllegalStateException while adding debug tab",e);
				}
				mDebugTab = 1; // mTabTitles.size() - 1;
			}
		}
		DebugFragment fragment = (DebugFragment) mSwipeyAdapter
				.instantiateItem(mViewPager, mDebugTab);
		if (fragment == null) {
			Log.e(TAG, "No debug fragment");
			return;
		}
		switch (debugType) {
		case EvaDebug:
			fragment.setDebugText(lastEvaReply);
			break;
		case VayantDebug:
			fragment.setVayantDebugText(lastVayantReply);
			break;
		case ExpediaDebug:
			fragment.setExpediaDebugText(lastExpediaReply);
			break;
		}
		
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);
		if (DEBUG_PREF_KEY.equals(key)) {
			ActivityCompat.invalidateOptionsMenu(this);
		}
	}
	

	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {

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
		bugReporter.putCustomData("eva_session_"+items, reply.JSONReply.toString());
		
		if ("voice".equals(cookie)) {
			SpannableString chat = null;
			if (reply.originalInputText != null) {
				chat = new SpannableString(reply.originalInputText);
			}
			else if (reply.processedText != null) {
				// reply of voice -  add a "Me" chat item for the input text
				chat = new SpannableString(reply.processedText);
				if (reply.evaWarnings.size() > 0) {
					int col = getResources().getColor(R.color.my_chat_no_session_text);
					for (EvaWarning warning: reply.evaWarnings) {
						if (warning.position == -1) {
							continue;
						}
						chat.setSpan( new ForegroundColorSpan(col), warning.position, warning.position+warning.text.length(), 0);
						chat.setSpan( new StyleSpan(Typeface.ITALIC), warning.position, warning.position+warning.text.length(), 0);
					}
				}
			}
			if (chat != null)
				addChatItem(new ChatItem(chat));
		}
		
		super.onEvaReply(reply, cookie);
		
		try {
			setDebugData(DebugTextType.EvaDebug, reply.JSONReply.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		if (reply.flow != null ) {
			handleFlow(reply);
		}
//		else {
//			handleNonFlow(reply);  // old code
//		}		
	}

	
/*
	private void handleNonFlow(EvaApiReply reply) {
		String sayIt = handleChat(reply);
		if (sayIt != null) {
			addChatItem(new ChatItem(sayIt, reply, null, ChatType.Eva));
		}
		else if (reply.dialog != null) {
			sayIt = reply.dialog.sayIt;
			speak(sayIt);
			DialogQuestionChatItem chatItem = new DialogQuestionChatItem(sayIt, reply, null);
			addChatItem(chatItem);
			
			if (reply.dialog.elements != null && reply.dialog.elements.length > 0) {
				DialogElement dialogElement = reply.dialog.elements[0];
				if ("Question".equals(dialogElement.Type) && "Multiple Choice".equals(dialogElement.SubType)) {
					for (int index=0; index < dialogElement.Choices.length; index++) {
						addChatItem(new DialogAnswerChatItem(chatItem, index, dialogElement.Choices[index]));
					}
				}
			}

		}
		else {
			sayIt = reply.sayIt;
			if (sayIt != null && !"".equals(sayIt.trim())) {
				// say_it = "Searching for a " + say_it; Need to create an international version of this...
				addChatItem(new ChatItem(sayIt, reply,  null, ChatType.Eva));
				speak(sayIt);
			}
			
			if (reply.ean != null && !"".equals(reply.ean)) {  //isHotelSearch()) {
				Log.d(TAG, "Running Hotel Search!");
				mSearchExpediaTask = injector.getInstance(HotelListDownloaderTask.class);
				mSearchExpediaTask.initialize(this, reply, "$");
				mSearchExpediaTask.execute();
			}
			if (reply.isFlightSearch()) {
				Log.d(TAG, "Running Vayant Search!");
				mSearchVayantTask = new SearchVayantTask(this, reply, null);
				mSearchVayantTask.execute();
			}
//			if (reply.isTrainSearch()) {
//				Log.d(TAG, "Running Travelport Search!");
//				mSearchTravelportTask = new SearchTravelportTask(this, reply);
//				mSearchTravelportTask.execute();
//			}
		}
	}*/

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
				DialogQuestionChatItem  questionChatItem = new DialogQuestionChatItem(flow.SayIt, reply, flow);
				chatItem = questionChatItem;
				addChatItem(questionChatItem);
				
				if (question.choices != null && question.choices.length > 0) {
					for (int index=0; index < question.choices.length; index++) {
						addChatItem(new DialogAnswerChatItem(questionChatItem, index, question.choices[index]));
					}
				}
				if (first) {
					// execute first question
					executeFlowElement(reply, flow, chatItem);
					first = false;
				}
			}
			else {
				chatItem = new ChatItem(flow.SayIt, reply, flow, ChatType.Eva);
				addChatItem(chatItem);
			}
			
			if (!hasQuestion && first) {
				// automatically execute first element - if no questions exist
				executeFlowElement(reply, flow, chatItem);
				
				first = false;
			}
			
		}
	}


	static ChatItem lastHotelCompleted = null;
	static ChatItem lastFlightCompleted = null;

	class DownloaderListener implements EvaDownloaderTaskInterface {
		
		ChatItem currentItem;
		
		DownloaderListener(ChatItem chatItem) {
			currentItem = chatItem;
		}
		@Override
		public void endProgressDialog(int id, String result) {
			Log.i(TAG, "End search for "+currentItem.getChat());
			currentItem.setStatus(Status.HasResults);
			currentItem.setSearchResults(result);
			// this is ugly hack - there can be only one hotel chat item with results, and only one flight search with results
			if (currentItem.getFlowElement().Type == TypeEnum.Hotel) {
				if (lastHotelCompleted != null && lastHotelCompleted != currentItem) {
					lastHotelCompleted.setStatus(Status.ToSearch);
				}
				lastHotelCompleted = currentItem;
			}
			if (currentItem.getFlowElement().Type == TypeEnum.Flight) {
				if (lastFlightCompleted != null && lastFlightCompleted != currentItem) {
					lastFlightCompleted.setStatus(Status.ToSearch);
				}
				lastFlightCompleted = currentItem;
			}
			invalidateChatFragment();
		}

		@Override
		public void startProgressDialog(int id) {
			Log.i(TAG, "Start search for "+currentItem.getChat());
			currentItem.setStatus(Status.InSearch);
			invalidateChatFragment();
		}

		@Override
		public void endProgressDialogWithError(int id, String result) {
			Log.i(TAG, "End search with ERROR for "+currentItem.getChat());
			currentItem.setStatus(Status.ToSearch);
			invalidateChatFragment();
		}

		@Override
		public void updateProgress(int id, DownloaderStatus mProgress) {
		}
	}

	private void executeFlowElement(EvaApiReply reply, FlowElement flow, ChatItem chatItem) {
//		chatItem.setActivated(true);
		if (flow.SayIt != null && !"".equals(flow.SayIt) ) {
			speak(flow.SayIt);
		}
		
		switch (flow.Type) {
		case Hotel:
			Log.d(TAG, "Running Hotel Search!");
			mSearchExpediaTask = injector.getInstance(HotelListDownloaderTask.class);
			mSearchExpediaTask.initialize(this, reply, "$"); // TODO: change to be based on flow element, // TODO: change to use currency
			mSearchExpediaTask.attach(new DownloaderListener(chatItem));
			if (chatItem.getStatus() == Status.HasResults) {
				// this chat item was already activated and has results - bypass the cloud service and fake results
				mSearchExpediaTask.setCachedResults(chatItem.getSearchResult());
			}
			else {
				mSearchExpediaTask.execute();
			}
			break;
		case Flight:
			Log.d(TAG, "Running Vayant Search!");
			mSearchVayantTask = new SearchVayantTask(this, reply, flow);
			mSearchVayantTask.attach(new DownloaderListener(chatItem));
			if (chatItem.getStatus() == Status.HasResults) {
				// this chat item was already activated and has results - bypass the cloud service and fake results
				mSearchVayantTask.setCachedResults(chatItem.getSearchResult());
			}
			else {
				mSearchVayantTask.execute();
			}
			break;
		case Question:
			break;
		
		}
	}

	/****
	 * Start new session from menu item 
	 */
	private void startNewSession() {
//		if (isNewSession() == false) {
			showTab(R.string.CHAT);
			addChatItem(new ChatItem("Start new search"));
			resetSession();
			String sessionText = "Starting a new search. How may I help you?";
			addChatItem(new ChatItem(sessionText, null, null, ChatType.Eva));
			speak(sessionText);

//		}
	}
	
	@Inject private ChatItemList mChatListModel;
	
	/****
	 * handler for  "new session was started" event 
	 * @param event
	 */
	protected void newSessionStart(@Observes NewSessionStarted event) {
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
		
//		for (ChatItem chatItem : mChatListModel.getItemList()) {
//			chatItem.setInSession(false);
//		}
		if (mChatListModel.size() > 0) {
			ChatItem lastItem = mChatListModel.get(mChatListModel.size()-1);
			mChatListModel.clear();
			if (lastItem.getType() == ChatType.Me) {
				mChatListModel.add(lastItem);
			}
		}		
	}
	
	public void onHotelsListUpdated( @Observes HotelsListUpdated event) {
		int mapTabIndex = mTabTitles.indexOf("MAP");
		if (mapTabIndex != -1) {
			HotelsMapFragment frag = (HotelsMapFragment)  mSwipeyAdapter.instantiateItem(mViewPager, mapTabIndex);
			if (frag != null) {
				Activity hostedActivity = frag.getHostedActivity();
				if (hostedActivity != null) {
					HotelsMapActivity hma = (HotelsMapActivity) hostedActivity;
					hma.onHotelsListUpdated();
				}
			}
		}
	}
		
	public void onChatItemClicked( @Observes ChatItemClicked  event) {
		ChatItem chatItem = event.chatItem;
		Log.i(TAG, "Chat Item clicked "+chatItem.getChat());
		if (chatItem.getFlowElement() != null) {
			executeFlowElement(chatItem.getEvaReply(), chatItem.getFlowElement(), chatItem);
		}
	}

	
}
// TODO: I took the microphone icon from: http://www.iconarchive.com/show/atrous-icons-by-iconleak/microphone-icon.html
// Need to add attribution in the about text.
// TODO: refactor classes out of this mess.
// TODO: progress bar or spinning wheel when contacting Eva?
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
// TODO: chat list should be from top of the screen

// Translate: Bing primary key = "uMLqpa+YkdRvJHukpbt06yQNa+ozPiGwrKSwnvjBYh4="

// I think a crash is caused if I start a speech recognition activity and I rotate the screen.
// The result is delivered to a dead activity and I get 