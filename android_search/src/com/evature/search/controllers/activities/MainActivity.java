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

import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
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
import com.evature.components.MyViewPager;
import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface;
import com.evature.search.controllers.web_services.EvaHotelDownloaderTask;
import com.evature.search.controllers.web_services.HotelListDownloaderTask;
import com.evature.search.controllers.web_services.SearchTravelportTask;
import com.evature.search.controllers.web_services.SearchVayantTask;
import com.evature.search.models.chat.ChatItem;
import com.evature.search.views.SwipeyTabs;
import com.evature.search.views.adapters.FlightListAdapterTP;
import com.evature.search.views.adapters.SwipeyTabsAdapter;
import com.evature.search.views.adapters.TrainListAdapter;
import com.evature.search.views.fragments.ChatFragment;
import com.evature.search.views.fragments.ExamplesFragment;
import com.evature.search.views.fragments.ExamplesFragment.ExampleClickedHandler;
import com.evature.search.views.fragments.FlightsFragment;
import com.evature.search.views.fragments.HotelFragment;
import com.evature.search.views.fragments.HotelsFragment;
import com.evature.search.views.fragments.HotelsMapFragment;
import com.evature.search.views.fragments.TrainsFragment;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class MainActivity extends EvaBaseActivity implements TextToSpeech.OnInitListener, EvaDownloaderTaskInterface {

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

	static EvaHotelDownloaderTask mHotelDownloader = null;

	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Called when the activity is first created.
		Log.d(TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_main);
		if (savedInstanceState != null) { // Restore state
			// Same code as onRestoreInstanceState() ?
			Log.d(TAG, "restoring saved instance state");
			mTabTitles = savedInstanceState.getStringArrayList("mTabTitles");
		} else {
			Log.d(TAG, "no saved instance state");
			mTabTitles = new ArrayList<String>(Arrays.asList("EXAMPLES", "CHAT"));
		}
		mSwipeyAdapter = new SwipeyTabsPagerAdapter(this, getSupportFragmentManager(), mViewPager, mTabs);
		mViewPager.setAdapter(mSwipeyAdapter);
		mTabs.setAdapter(mSwipeyAdapter);
		mViewPager.setOnPageChangeListener(mTabs); // To sync the tabs with the viewpager
		mViewPager.setCurrentItem(1);
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
			if (position < size && mTabTitles.get(position).equals(getString(R.string.EXAMPLES))) { // Examples window
				final String[] examples = {
					"Fly to NY next Tuesday morning",
					"Hotels in Arlington",
					"3 Star hotels in NYC",
					"Fly to NY next Sunday, return 5 days later",
					"Train ride from NYC to Washington DC next Wednesday"
				};
				Log.d(TAG, "Example Fragment");
				ExamplesFragment fragment = injector.getInstance(ExamplesFragment.class);
				fragment.setExamples(examples);
				fragment.setHandler(new ExampleClickedHandler() {
					@Override
					public void onClick(String example) {
						Log.d(TAG, "Running example: "+example);
						addChatItem(example, false);
						mViewPager.setCurrentItem(1, true);
						MainActivity.this.searchWithText(example);
					}});
				return fragment; 
			}
			if (position < size && mTabTitles.get(position).equals(getString(R.string.CHAT))) { // Main Chat window
				Log.i(TAG, "Chat Fragment");
				return injector.getInstance(ChatFragment.class);
			}
			if (position < size && mTabTitles.get(position).equals(getString(R.string.HOTELS))) { // Hotel list window
				Log.i(TAG, "Hotels Fragment");
				return injector.getInstance(HotelsFragment.class);
			}
			
			if (position < size && mTabTitles.get(position).equals(getString(R.string.HOTELS_MAP))) { // Hotel list window
				Log.i(TAG, "HotelsMap Fragment");
				Fragment fragment= injector.getInstance(HotelsMapFragment.class);
				return fragment;
			}
			
			if (position < size && mTabTitles.get(position).equals(getString(R.string.FLIGHTS))) { // flights list
				Log.i(TAG, "Flights Fragment");
				return injector.getInstance(FlightsFragment.class);
			}
			if (position < size && mTabTitles.get(position).equals(getString(R.string.HOTEL))) { // Single hotel
				int hotelIndex = MyApplication.getDb().getHotelId();
				Log.i(TAG, "starting hotel Fragment for hotel # " + hotelIndex);
				return HotelFragment.newInstance(hotelIndex);
			} else if (position < size && mTabTitles.get(position).equals(getString(R.string.TRAINS))) { // trains list window
				Log.i(TAG, "Trains Fragment");
				return injector.getInstance(TrainsFragment.class);
			}
			else {
				Log.e(TAG, "No fragment made for Position "+position+(position< size ? " titled "+mTabTitles.get(position) : ""));
				return null;
			}
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // user pressed the menu button
		switch (item.getItemId()) {
		case R.id.settings: // Did the user select "settings"?
			Intent intent = new Intent();
			// Then set the activity class that needs to be launched/started.
			intent.setClass(this, MyPreferences.class);
			Bundle a_bundle = new Bundle(); // Lets send some data to the preferences activity
		//	a_bundle.putStringArrayList("mLanguages", (ArrayList<String>) mSpeechRecognition.getmGoogleLanguages());
			intent.putExtras(a_bundle);
			startActivity(intent); // start the activity by calling
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
		super.onSaveInstanceState(savedInstanceState);
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
	
	private void addChatItem(String text, boolean fromEva) {
		Log.d(TAG, "Adding chat item: '"+text+"'  fromEva: "+fromEva);
		String chatTabName = getString(R.string.CHAT);
		int index = mTabTitles.indexOf(chatTabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(chatTabName);
			index = mTabTitles.size() - 1;
		}
		// http://stackoverflow.com/a/8886019/78234
		ChatFragment fragment = (ChatFragment) mSwipeyAdapter.instantiateItem(mViewPager, index);
		if (fragment != null) // could be null if not instantiated yet
		{
			if (fragment.getView() != null) {
				fragment.addChatItem(new ChatItem(text, fromEva ? ChatItem.CHAT_EVA : ChatItem.CHAT_ME));
			} else {
				Log.e(TAG, "chat fragment.getView() == null!?!");
			}
		} else {
			Log.e(TAG, "chat fragment == null!?");
		}

	}
	

	protected void handleSayIt(EvaApiReply apiReply) {
		handleChat(apiReply);
		if (apiReply.sayIt != null) {
			String say_it = apiReply.sayIt;
			if (say_it != null && !say_it.isEmpty() && !say_it.trim().isEmpty()) {
				// say_it = "Searching for a " + say_it; Need to create an international version of this...
				addChatItem(say_it, true);
				
				// Iftah: commented - debug without annoying sounds
				//speak(say_it);
			}
		}
	}

	private void handleChat(EvaApiReply apiReply) {
		if (!apiReply.isFlightSearch() && !apiReply.isHotelSearch() && (apiReply.chat != null)) {
			if (apiReply.chat.hello != null && apiReply.chat.hello) {
				apiReply.sayIt = "Why, Hello there!";
			}
			if (apiReply.chat.who != null && apiReply.chat.who) {
				apiReply.sayIt = "I'm Eva, your travel search assistant";
			}
			if (apiReply.chat.meaningOfLife != null && apiReply.chat.meaningOfLife) {
				apiReply.sayIt = "Disrupting travel search, of course!";
			}
		}
	}


	public void setVayantReply() {
		String tabName = getString(R.string.FLIGHTS);
		int index = mTabTitles.indexOf(tabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(tabName);
			index = mTabTitles.size() - 1;
		}
		// get the fragment: http://stackoverflow.com/a/7393477/78234
		String tag = "android:switcher:" + R.id.viewpager + ":" + index; // wtf...
		FlightsFragment fragment = (FlightsFragment) getSupportFragmentManager().findFragmentByTag(tag);
		if (fragment != null) // could be null if not instantiated yet
		{
			fragment.getAdapter().notifyDataSetChanged();
		} else {
			Log.e(TAG, "Flights fragment == null!?!");
		}
		
		mViewPager.setCurrentItem(index, true);
	}

	public void setTravelportReply(boolean train) {
		// get the fragment: http://stackoverflow.com/a/7393477/78234
		int string_id = train ? R.string.TRAINS : R.string.FLIGHTS;
		String tabName = getString(string_id);
		int index = mTabTitles.indexOf(tabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(tabName);
			index = mTabTitles.size() - 1;
		}
		mViewPager.setCurrentItem(index, true);
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
				FlightListAdapterTP adapter = fragment.getAdapter();
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					Log.d(TAG, "Flights fragment adapter notifyDataSetChanged()");
				} else {
					Log.e(TAG, "Flights fragment adapter == null!?!");
				}
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

	int mMapTabIndex=-1;
	int mHotelTabIndex=-1;
	
	@Override
	public void endProgressDialog(int id) { // we got the hotel search reply successfully
		Log.d(TAG, "endProgressDialog() for id " + id);

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
				mMapTabIndex = mTabTitles.size() - 1;
			}
		} else if (id == R.string.HOTEL) {
			mSwipeyAdapter.removeTab();
			mSwipeyAdapter.addTab(tabName);
			// HotelFragment fragment = (HotelFragment) adapter.instantiateItem(mViewPager, index);
			// fragment.mAdapter.notifyDataSetChanged();
			// I need to invalidate the entire view somehow!!!
			mSwipeyAdapter.stuffChanged(index);
			mHotelTabIndex = mTabTitles.size() - 1;
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
			
			if(mHotelTabIndex!=-1)
			{
				mSwipeyAdapter.removeTab(mHotelTabIndex);
				mHotelTabIndex=-1;
			}
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
		// TODO Auto-generated method stub

	}

	@Override
	public void endProgressDialogWithError(int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateProgress(int id, int mProgress) {
		// TODO Auto-generated method stub

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

		mHotelDownloader.execute();

	}

	

	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {
		handleSayIt(reply); // Say (using TTS) the eva reply
		if (reply.isHotelSearch()) {
			Log.d(TAG, "Running Hotel Search!");
			mSearchExpediaTask = injector.getInstance(HotelListDownloaderTask.class);
			mSearchExpediaTask.initialize(this, reply, "$");
			mSearchExpediaTask.execute();
		}
//		if (reply.isFlightSearch()) {
//			Log.d(TAG, "Running Vayant Search!");
//			mSearchVayantTask = new SearchVayantTask(this, reply);
//			mSearchVayantTask.execute();
//		}
//		if (reply.isTrainSearch()) {
//			Log.d(TAG, "Running Travelport Search!");
//			mSearchTravelportTask = new SearchTravelportTask(this, reply);
//			mSearchTravelportTask.execute();
//		}
		
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