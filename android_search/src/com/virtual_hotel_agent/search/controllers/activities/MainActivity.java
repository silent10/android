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

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.json.JSONObject;

import roboguice.activity.RoboFragmentActivity;
import roboguice.event.Observes;
import roboguice.inject.InjectView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.viewpagerindicator.TitlePageIndicator;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.events.ChatItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;
import com.virtual_hotel_agent.search.controllers.events.HotelsListUpdated;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskInterface;
import com.virtual_hotel_agent.search.controllers.web_services.HotelDownloaderTask;
import com.virtual_hotel_agent.search.controllers.web_services.HotelListDownloaderTask;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.chat.ChatItem.Status;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.DialogAnswerChatItem;
import com.virtual_hotel_agent.search.models.chat.DialogQuestionChatItem;
import com.virtual_hotel_agent.search.models.expedia.ExpediaRequestParameters;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.views.MainView;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment.DialogClickHandler;
import com.virtual_hotel_agent.search.views.fragments.ChildAgeDialogFragment;
//import com.virtual_hotel_agent.search.views.fragments.ExamplesFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelDetailFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelsFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelsMapFragment;
//import com.virtual_hotel_agent.search.views.fragments.ExamplesFragment.ExampleClickedHandler;

public class MainActivity extends RoboFragmentActivity implements 
													EvaSearchReplyListener,
													OnSharedPreferenceChangeListener,
													DownloaderTaskInterface {

	private static final String ITEMS_IN_SESSION = "items_in_session";


	private static final String TAG = MainActivity.class.getSimpleName();
	// private static String mExternalIpAddress = null;
	
	
	private static boolean mSpeechToTextWasConfigured = false;
	private List<String> mTabTitles;
	@Inject Injector injector;
	
	@InjectView(R.id.viewpager) private ViewPager mViewPager; 
//	@InjectView(R.id.indicator) private TabPageIndicator mTabs;
	@InjectView(R.id.indicator) private TitlePageIndicator mTabs;
	//SearchVayantTask mSearchVayantTask;
	//SearchTravelportTask mSearchTravelportTask;
	SwipeyTabsPagerAdapter mSwipeyAdapter;
	HotelListDownloaderTask mSearchExpediaTask;
		
	private boolean mIsNetworkingOk = false;

	private String mChatTabName;

//	private String mExamplesTabName;
	private String mHotelsTabName;
	private String mHotelTabName;

	static HotelDownloaderTask mHotelDownloader = null;
	

	// public for unit tests
	public EvaComponent eva;

	EvaSpeechComponent speechSearch = null;
	
	MainView mainView;


	@Override
	public void onDestroy() {
		eva.onDestroy();

		super.onDestroy();
	}
	
	
	
	
// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		eva.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	// This examples assumes the use of Google Analytics campaign
	// "utm" parameters, like "utm_source"
	private static final String CAMPAIGN_SOURCE_PARAM = "utm_source";
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
		Tracker t1 = GoogleAnalytics.getInstance(this).getTracker("UA-47284954-1");

		Intent intent = this.getIntent();
	    Uri uri = intent.getData();
		MapBuilder.createAppView().setAll(getReferrerMapFromUri(uri));
	    EasyTracker.getInstance(this).activityStart(this);
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}
	
	
	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		eva.onResume();
		super.onResume();
//		setDebugData(DebugTextType.None, null);
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		if (mHotelDownloader != null) {
			mHotelDownloader.cancel(true);
			mHotelDownloader = null;
		}
		eva.onPause();
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Called when the activity is first created.
		Log.d(TAG, "onCreate()");
		SettingsAPI.getLocale(this);
		eva = new EvaComponent(this, this);
		eva.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		
		speechSearch = new EvaSpeechComponent(eva);
		setContentView(R.layout.new_main);
		
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
		
		mainView = new MainView(this);
		
		eva.setApiKey(SettingsAPI.getEvaKey(this));
		eva.setSiteCode(SettingsAPI.getEvaSiteCode(this));
		
		mChatTabName = getString(R.string.CHAT);
//		mExamplesTabName = getString(R.string.EXAMPLES);
		//mDebugTabName = getString(R.string.DEBUG);
		mHotelsTabName = getString(R.string.HOTELS);
		mHotelTabName = getString(R.string.HOTEL);

		
		if (savedInstanceState != null  && MyApplication.getDb().mHotelData != null) { // Restore state
			// Same code as onRestoreInstanceState() ?
			Log.d(TAG, "restoring saved instance state");
			mTabTitles = savedInstanceState.getStringArrayList("mTabTitles");
		} else {
			Log.d(TAG, "no saved instance state");
			mTabTitles = new ArrayList<String>(Arrays.asList(/*mExamplesTabName,*/ mChatTabName));
		}
		
		
		
		mSwipeyAdapter = new SwipeyTabsPagerAdapter(getSupportFragmentManager());

		mViewPager.setAdapter(mSwipeyAdapter);
		mTabs.setViewPager(mViewPager);
		mViewPager.setOffscreenPageLimit(5);
		mTabs.setOnPageChangeListener(mSwipeyAdapter);
		

		//getActionBar().setDisplayHomeAsUpEnabled(true);
		
		//mViewPager.setOnPageChangeListener(mTabs); // To sync the tabs with the viewpager
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
		
		// if starting from empty 
		showIntro();
	}
	
	private boolean shownExamples = false;
	private void showExamples() {
		if (shownExamples) {
			return;
		}
		shownExamples = true;
		String[] examples = {
				"  Hotel tonight", 
				"  Hotel in Paris", 
				"  Hotel near the Eiffel tower",
				"  3 star hotel near the Eiffel tower",
				"  Sort by price"
			};
		String greeting = getResources().getString(R.string.examples_greetings);
		String examplesString = "";
		for (String example : examples) {
			examplesString += "\n"+example;
		}
		SpannableString chatFormatted = new SpannableString(greeting+examplesString);
		chatFormatted.setSpan( new StyleSpan(Typeface.ITALIC), greeting.length(), chatFormatted.length(), 0);

		ChatItem chatItem = new ChatItem(chatFormatted, null, null, ChatType.VirtualAgentContinued);
		MainActivity.this.addChatItem(chatItem);
	}

	private void showIntro() {
		String greeting =  getResources().getString(R.string.greeting);
		if (SettingsAPI.getShowIntroTips(this)) {
			ChatItem chat = new ChatItem(greeting,null, null, ChatType.VirtualAgent);
			MainActivity.this.addChatItem(chat);
			showExamples();
		}
		else {
			int pos = greeting.length();
			String seeExamples = "\nTap here to see some examples.";
			greeting += new SpannedString(seeExamples);
			SpannableString sgreet = new SpannableString(greeting);
			int col = getResources().getColor(R.color.vha_chat_no_session_text);
			sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
			sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
			ChatItem chat = new ChatItem(sgreet,null, null, ChatType.VirtualAgentWelcome);
			MainActivity.this.addChatItem(chat);
		}
		
		//mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}
	
	@Override
	public void onBackPressed() {
	   Log.d(TAG, "onBackPressed Called");
	   int chatInd = mTabTitles.indexOf(mChatTabName);
	   if (mSwipeyAdapter.lastShown == chatInd) {
		   super.onBackPressed();
	   }
	   else {
		   mTabs.setCurrentItem(chatInd);
	   }
	}

	public class SwipeyTabsPagerAdapter  extends FragmentPagerAdapter implements ViewPager.OnPageChangeListener {
		
		//private final ViewPager mViewPager;
		private final String TAG = SwipeyTabsPagerAdapter.class.getSimpleName();

		public SwipeyTabsPagerAdapter( FragmentManager fm) {
			super(fm);
			Log.i(TAG, "CTOR");
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
				Log.e(TAG, "No fragment made for Position "+position);
				return null;
			}
			if (mTabTitles.get(position).equals(mChatTabName)) { // Main Chat window
				Log.d(TAG, "Chat Fragment");
				ChatFragment chatFragment = injector.getInstance(ChatFragment.class);
				chatFragment.setDialogHandler(new DialogClickHandler() {
					
					@Override
					public void onClick(SpannableString dialogResponse, int responseIndex) {
						MainActivity.this.addChatItem(new ChatItem(dialogResponse));
						MainActivity.this.eva.replyToDialog(responseIndex);
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
			
//			if (mTabTitles.get(position).equals(getString(R.string.FLIGHTS))) { // flights list
//				Log.i(TAG, "Flights Fragment");
//				return injector.getInstance(FlightsFragment.class);
//			}
			
			if (mTabTitles.get(position).equals(mHotelTabName)) { // Single hotel
				int hotelIndex = MyApplication.getExpediaRequestParams().getHotelId();
				Log.i(TAG, "starting hotel Fragment for hotel # " + hotelIndex);
				return HotelDetailFragment.newInstance(hotelIndex);
			}
//			if (mTabTitles.get(position).equals(getString(R.string.TRAINS))) { // trains list window
//				Log.i(TAG, "Trains Fragment");
//				return injector.getInstance(TrainsFragment.class);
//			}

			Log.e(TAG, "No fragment made for Position "+position+(position< size ? " titled "+mTabTitles.get(position) : ""));
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
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			if (positionOffset > 0) {
				lastShown = position+1;
			}
			else {
				lastShown = position;
			}
		}

		@Override
		public void onPageSelected(int position) {
			lastShown = position;
		}
		
		@Override
		public void notifyDataSetChanged() {
			mTabs.notifyDataSetChanged();
			super.notifyDataSetChanged();
		}
		
		int lastShown = -1;

		// Internal helper function
		public void showTab(int position) {
			Log.d(TAG, "showTab "+position);
			lastShown = position;
			mViewPager.setCurrentItem(position, true);
//			mTabs.onPageSelected(position);
//			this.notifyDataSetChanged();
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
		return true;
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
			Bundle a_bundle = new Bundle(); // Lets send some data to the preferences activity
		//	a_bundle.putStringArrayList("mLanguages", (ArrayList<String>) mSpeechRecognition.getmGoogleLanguages());
			intent.putExtras(a_bundle);
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
		//mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}
	
	private void invalidateChatFragment() {
		//showTab(R.string.CHAT);
		int index = mTabTitles.indexOf(mChatTabName);
				Log.i(TAG, "Chat tab at index "+index);
		if (index == -1) {
			mSwipeyAdapter.addTab(mChatTabName);
			index = mTabTitles.size() - 1;
		}
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
	
	private int showTab(int tabNameId) {
		String tabName = getString(tabNameId);
		int index = mTabTitles.indexOf(tabName);
		if (index == -1) {
			mSwipeyAdapter.addTab(tabName);
			index = mTabTitles.size() - 1;
		}
		mTabs.setCurrentItem(index);
		return index;
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
	public void endProgressDialog(int id, JSONObject result) { // we got the hotels list or hotel details reply successfully
		Log.d(TAG, "endProgressDialog() for id " + id);
		mainView.hideStatus();
//		setDebugData(DebugTextType.ExpediaDebug, result);
		
		if (id == R.string.HOTEL && mHotelDownloader != null) {
			int hotelIndex = mHotelDownloader.getHotelIndex();
			Log.d(TAG, "endProgressDialog() Hotel # " + hotelIndex);
			MyApplication.getExpediaRequestParams().setHotelId(hotelIndex);
			
			// remove hotel tab and add it again
			String tabName = getString(id); // Yeah, I'm using the string ID for distinguishing between downloader tasks
			int index = mTabTitles.indexOf(tabName);
//			if (index != -1)
//				mSwipeyAdapter.removeTab(index);
//			mSwipeyAdapter.addTab(tabName);
			if (index == -1) {
				mSwipeyAdapter.addTab(tabName);
			}
			else {
	
				HotelDetailFragment fragment = (HotelDetailFragment) mSwipeyAdapter.instantiateItem(mViewPager, index);
				if (fragment != null) // could be null if not instantiated yet
				{
					fragment.changeHotelId(hotelIndex);
				}
			}

			index = mTabTitles.indexOf(tabName);
			mTabs.setCurrentItem(index);
		}

		

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
	public void endProgressDialogWithError(int id, JSONObject result) {
//		setDebugData(DebugTextType.ExpediaDebug, result);
		mainView.hideStatus();
	}

	@Override
	public void updateProgress(int id, DownloaderStatus mProgress) {
	}


	
	// search button click handler ("On Click property" of the button in the xml)
	// http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments
	public void myClickHandler(View view) {
		switch (view.getId()) {
		case R.id.restart_button:
			startNewSession();
			return;

		case R.id.search_button:
			// simplest method:  default 
			// MainActivity.this.eva.searchWithVoice("voice");
			
			if ("google_local".equals(MainActivity.this.eva.getVrService())) {
				MainActivity.this.eva.searchWithLocalVoiceRecognition(4);
				return;
			}
			
			if (speechSearch.isInSpeechRecognition() == true) {
				speechSearch.stop();
				return;
			}
			
			MainActivity.this.eva.speak("");
			Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createEvent("speech_search", "speech_search_start", "", 0l)
					    .build()
					   );
			
			mainView.startSpeechSearch(speechSearch);
			
			break;
		}
	}




//	int mDebugTab = -1;
//	private String lastEvaReply;
//	private String lastVayantReply;
//	private String lastExpediaReply;
//	enum DebugTextType {
//		None,
//		EvaDebug,
//		VayantDebug,
//		ExpediaDebug
//	}
	
//	private void setDebugData(DebugTextType debugType, JSONObject jTxt) {
//		String txt;
//		try {
//			if (jTxt == null) {
//				txt = "null !!!";
//			}
//			else {
//				txt = jTxt.toString(2);
//			}
//		} catch (JSONException e1) {
//			txt = "Exception "+e1;
//			e1.printStackTrace();
//			
//		}
//		switch (debugType) {
//		case EvaDebug:
//			lastEvaReply = txt;
//			break;
//		case VayantDebug:
//			lastVayantReply = txt;
//			break;
//		case ExpediaDebug:
//			lastExpediaReply = txt;
//			break;
//		}
//
//		
//		if (false == eva.isDebug()) {
//			if (mDebugTab != -1) {
//				mSwipeyAdapter.removeTab(mDebugTab);
//				mDebugTab = -1;
//			}
//			return;
//		}
//		
//		if (mDebugTab == -1) {
//			mDebugTab = mTabTitles.indexOf(mDebugTabName);
//			if (mDebugTab == -1) {
//				try {
//					mSwipeyAdapter.addTab(mDebugTabName, 1);
//				} catch (IllegalStateException e) {
//					Log.e(TAG, "Exception of IllegalStateException while adding debug tab",e);
//				}
//				mDebugTab = 1; // mTabTitles.size() - 1;
//			}
//		}
//		DebugFragment fragment = (DebugFragment) mSwipeyAdapter
//				.instantiateItem(mViewPager, mDebugTab);
//		if (fragment == null) {
//			Log.e(TAG, "No debug fragment");
//			return;
//		}
//		switch (debugType) {
//		case EvaDebug:
//			fragment.setDebugText(lastEvaReply);
//			break;
//		case VayantDebug:
//			fragment.setVayantDebugText(lastVayantReply);
//			break;
//		case ExpediaDebug:
//			fragment.setExpediaDebugText(lastExpediaReply);
//			break;
//		}
//	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		eva.onSharedPreferenceChanged(sharedPreferences, key);
		if (EvaComponent.DEBUG_PREF_KEY.equals(key)) {
			ActivityCompat.invalidateOptionsMenu(this);
		}
		else if (SettingsAPI.EVA_KEY.equals(key)) {
			eva.setApiKey(SettingsAPI.getEvaKey(this));
		}
		else if (SettingsAPI.EVA_SITE_CODE.equals(key)) {
			eva.setSiteCode(SettingsAPI.getEvaSiteCode(this));
		}
	}
	

	@Override
	public void onEvaReply(EvaApiReply reply, Object cookie) {

		if (MyApplication.AcraInitialized) {
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
		if ("voice".equals(cookie)) {
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
			if (chat != null)
				addChatItem(new ChatItem(chat));
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


	static ChatItem currentHotelSearch = null;
	static ChatItem currentFlightSearch = null;
	static ChatItem lastHotelCompleted = null;
	static ChatItem lastFlightCompleted = null;

	class DownloaderListener implements DownloaderTaskInterface {
		
		ChatItem currentItem;
		boolean switchToResult;
		
		DownloaderListener(ChatItem chatItem, boolean switchToResult) {
			currentItem = chatItem;
			this.switchToResult = switchToResult;
		}
		
		@Override
		public void endProgressDialog(int id, JSONObject result) {
			Log.i(TAG, "End search for "+currentItem.getChat());
			mainView.hideStatus();
			currentItem.setSearchResults(result);
			currentItem.setStatus(Status.HasResults);
			
			String tabName = getString(id); // Yeah, I'm using the string ID for distinguishing between downloader tasks
			
			int index = mTabTitles.indexOf(tabName);
			if (index == -1) {
				mSwipeyAdapter.addTab(tabName);
				index = mTabTitles.size() - 1;
				
				if(tabName.equals("HOTELS"))
				{
					mSwipeyAdapter.addTab("MAP");
				}
			} else 
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
			if (this.switchToResult) {
				mTabs.setCurrentItem(index);
			}
			
			
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
		public void endProgressDialogWithError(int id, JSONObject result) {
			Log.i(TAG, "End search with ERROR for "+currentItem.getChat());
			mainView.hideStatus();
			currentItem.setStatus(Status.ToSearch);
			if (currentItem.getFlowElement().Type == TypeEnum.Hotel) {
				XpediaDatabase db = MyApplication.getDb();
				if (db != null && db.unrecoverableError && XpediaDatabase.retries < 5) {
					XpediaDatabase.retries++;
					executeFlowElement(currentItem.getEvaReply(), currentItem.getFlowElement(), currentItem, false);
				}
			}
			invalidateChatFragment();
		}

		@Override
		public void updateProgress(int id, DownloaderStatus mProgress) {
		}
	}

	public void clearExpediaCache() {
		if (lastHotelCompleted != null) {
			
			lastHotelCompleted.setStatus(Status.ToSearch);
			
			// ??? mViewPager.setAdapter(null);
			//mTabs.setAdapter(null);
			
			// clear the hotel, flights and map
			int index = mTabTitles.indexOf(mHotelsTabName);
			if (index != -1)
				mTabTitles.remove(index);
			index = mTabTitles.indexOf(mHotelTabName);
			if (index != -1)
				mTabTitles.remove(index);
			index = mTabTitles.indexOf("MAP");
			if (index != -1)
				mTabTitles.remove(index);
			
			//mSwipeyAdapter.stuffChanged(mTabTitles.indexOf(mChatTabName));
			
			XpediaDatabase evaDb = MyApplication.getDb();
			if (evaDb != null) {
				evaDb.mHotelData = null;
				evaDb.clearImagesCache();
			}
		}
	}
	
	private void searchHotels(final ChatItem _chatItem,
			final EvaApiReply _reply,
			final boolean _switchToResult) {
		
		// reached hotel search - no need showing tips & examples next time
		SettingsAPI.setShowIntroTips(this, false);
		
		mSearchExpediaTask = injector.getInstance(HotelListDownloaderTask.class);
		mSearchExpediaTask.initialize(MainActivity.this, _reply,  SettingsAPI.getCurrencyCode(MainActivity.this)); // TODO: change to be based on flow element, // TODO: change to use currency
		mSearchExpediaTask.attach(new DownloaderListener(_chatItem, _switchToResult));
//		if (currentHotelSearch.getStatus() == Status.HasResults) {
//			// this chat item was already activated and has results - bypass the cloud service and fake results
//			mSearchExpediaTask.setCachedResults(_chatItem.getSearchResult());
//		}
//		else {
			mainView.showStatus("Searching for hotels...");
			mSearchExpediaTask.execute();
//		}
	}
	
	private void executeFlowElement(EvaApiReply reply, FlowElement flow, ChatItem chatItem, boolean switchToResult) {
//		chatItem.setActivated(true);
		
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
						Toast.makeText(MainActivity.this, "Children ages are required for searching hotels", Toast.LENGTH_LONG).show();
					}
				});
		        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
				
			}
			else {
				 Log.d(TAG, "Running Hotel Search!");
				 ExpediaRequestParameters db = MyApplication.getExpediaRequestParams();
				 if (reply.travelers != null) {
					 db.setNumberOfAdults(reply.travelers.allAdults());
 					 db.setNumberOfChildrenParam(reply.travelers.allChildren());
				 }
				 else {
					 // default 2 adults
					 db.setNumberOfAdults(2);
					 db.setNumberOfChildrenParam(0);
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
			
			Handler flash = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					mainView.flashSearchButton(3);
					super.handleMessage(msg);
				}
			}; 
			
			flash.sendEmptyMessageDelayed(1, 2000);
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
			eva.resetSession();
			String greeting = "Starting a new search. How may I help you?";
			
			shownExamples = false;
			
			int pos = greeting.length();
			String seeExamples = "\nClick here to see some examples.";
			SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
			int col = getResources().getColor(R.color.vha_chat_no_session_text);
			sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
			sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
			ChatItem chat = new ChatItem(sgreet,null, null, ChatType.VirtualAgentWelcome);
			MainActivity.this.addChatItem(chat);
			eva.speak(greeting);

//		}
	}
	
	@Inject private ChatItemList mChatListModel;
	
	/****
	 * handler for  "new session was started" event 
	 * this is either in response to menu click or to a server side initiated new session
	 */
	@Override
	public void newSessionStarted(boolean selfTriggered) {
		if (MyApplication.AcraInitialized) {
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
		lastFlightCompleted = null;
		lastHotelCompleted = null;

		mSwipeyAdapter.showTab(mTabTitles.indexOf(mChatTabName));
		// ??? mViewPager.setAdapter(null);
		//mTabs.setAdapter(null);
		
		// clear the hotel, flights and map
		int index = mTabTitles.indexOf(mHotelsTabName);
		if (index != -1)
			mTabTitles.remove(index);
		index = mTabTitles.indexOf(mHotelTabName);
		if (index != -1)
			mTabTitles.remove(index);
		index = mTabTitles.indexOf(getString(R.string.FLIGHTS));
		if (index != -1)
			mTabTitles.remove(index);
		index = mTabTitles.indexOf("MAP");
		if (index != -1)
			mTabTitles.remove(index);
		
		mSwipeyAdapter.notifyDataSetChanged();
		// not sure why - but in order for chat fragment to update I remove it here also :b
//		index = mTabTitles.indexOf(getString(R.string.CHAT));
//		if (index != -1)
//			mTabTitles.remove(index);
		
		//mSwipeyAdapter.stuffChged(mTabTitles.indexOf(mChatTabName));
		
		XpediaDatabase evaDb = MyApplication.getDb();
		if (evaDb != null) {
			evaDb.mHotelData = null;
		}
		
		S3DrawableBackgroundLoader.getInstance().Reset();
	}
	
	// note "onEvent" template is needed for progruard to not break roboguice
	public void onEventHotelsListUpdated( @Observes HotelsListUpdated event) {
		int mapTabIndex = mTabTitles.indexOf("MAP");
		if (mapTabIndex != -1) {
			HotelsMapFragment frag = (HotelsMapFragment)  mSwipeyAdapter.instantiateItem(mViewPager, mapTabIndex);
			if (frag != null) {
				frag.onHotelsListUpdated();
			}
		}
	}
	
	public void onEventHotelItemClicked( @Observes HotelItemClicked event) {
		Log.d(TAG, "onHotelItemClicked("+event.hotelIndex+")");
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

		
		mainView.showStatus("Getting Hotel info...");
		mHotelDownloader = new HotelDownloaderTask(this, event.hotelIndex);
		//this.endProgressDialog(R.string.HOTEL, "fake response");
		mHotelDownloader.execute();

	}
		
	public void onEventChatItemClicked( @Observes ChatItemClicked  event) {
		ChatItem chatItem = event.chatItem;
		Log.i(TAG, "Chat Item clicked "+chatItem.getChat());
		if (chatItem.getType() == ChatType.VirtualAgentWelcome ) {
			showExamples();
		}
		
		if (chatItem.getType() == ChatType.VirtualAgentContinued && Log.DEBUG) {
			addChatItem(new ChatItem("Hotel tonight"));
			eva.searchWithText("Hotel tonight");
		}
		
		if (chatItem.getFlowElement() != null) {
			executeFlowElement(chatItem.getEvaReply(), chatItem.getFlowElement(), chatItem, true);
		}
	}

	public void hotelsFragmentVisible() {
		// ugly hack to trigger sayit...
		if (currentHotelSearch != null && currentHotelSearch.sayitActivated == false) {
			String sayIt = currentHotelSearch.getFlowElement().getSayIt();
			if (sayIt != null && !"".equals(sayIt) ) {
				eva.speak(sayIt);
				currentHotelSearch.sayitActivated = true;
			}
		}
	}

	public void flightsFragmentVisible() {
		if (currentFlightSearch != null && currentFlightSearch.sayitActivated == false) {
			String sayIt = currentFlightSearch.getFlowElement().getSayIt();
			if (sayIt != null && !"".equals(sayIt) ) {
				eva.speak(sayIt);
				currentFlightSearch.sayitActivated = true;
			}
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