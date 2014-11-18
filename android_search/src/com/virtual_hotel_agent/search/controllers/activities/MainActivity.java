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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acra.ACRA;
import org.acra.ErrorReporter;

import roboguice.RoboGuice;
import roboguice.activity.event.OnStopEvent;
import roboguice.context.event.OnCreateEvent;
import roboguice.context.event.OnDestroyEvent;
import roboguice.event.EventManager;
import roboguice.event.Observes;
import roboguice.inject.ContentViewListener;
import roboguice.inject.RoboInjector;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
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
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBar;
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
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
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
import com.virtual_hotel_agent.search.controllers.tutorial.TutorialController;
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
import com.virtual_hotel_agent.search.util.VolumeUtil;
import com.virtual_hotel_agent.search.util.VolumeUtil.VolumeListener;
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
													VolumeListener,
													OnSharedPreferenceChangeListener {

	private static final String ITEMS_IN_SESSION = "items_in_session";


	private static final String TAG = MainActivity.class.getSimpleName();
	// private static String mExternalIpAddress = null;
	
	private List<String> mTabTitles;
	@Inject Injector injector;
	@Inject private ChatItemList mChatListModel;
	
	//SearchVayantTask mSearchVayantTask;
	//SearchTravelportTask mSearchTravelportTask;
		
	private boolean mIsNetworkingOk = false;

	static private HotelListDownloaderTask mSearchExpediaTask = null;
	static private RoomsUpdaterTask mRoomUpdater = null;
	static private HotelDownloaderTask mHotelDownloader = null;
	
	private EvaSpeechComponent speechSearch = null;
	
	MainView mainView;


	private MenuItem soundControlMenuItem;

	
	// following https://github.com/roboguice/roboguice/wiki/Using-your-own-BaseActivity-with-RoboGuice
    protected EventManager eventManager;
    protected HashMap<Key<?>,Object> scopedObjects = new HashMap<Key<?>, Object>();
    @Inject ContentViewListener ignored; // BUG find a better place to put this



	@Override
	public void onDestroy() {
		try {
			VHAApplication.EVA.onDestroy();
            eventManager.fire(new OnDestroyEvent<Activity>(this));
        } finally {
            try {
                RoboGuice.destroyInjector(this);
            } finally {
                super.onDestroy();
            }
        }
	}
	
    public Map<Key<?>, Object> getScopedObjectMap() {
        return scopedObjects;
    }	
	
// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case EvaComponent.VOICE_RECOGNITION_REQUEST_CODE_GOOGLE:
				// store the text in a new or existing chat-item
				if (data != null && data.getExtras() != null) {
					Bundle bundle = data.getExtras();
					ArrayList<String> matches = bundle.getStringArrayList(RecognizerIntent.EXTRA_RESULTS);
					if (matches.size() > 0) {
						if (VOICE_COOKIE.storeResultInItem == null) {
							ChatItem newChatItem = new ChatItem(matches.get(0));
							VOICE_COOKIE.storeResultInItem = newChatItem;
							addChatItem(newChatItem);
						} else {
							VOICE_COOKIE.storeResultInItem.setChat(matches.get(0));
						}
					}
				}
			break;
			default:
			break;
		}

		VHAApplication.EVA.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	// This examples assumes the use of Google Analytics campaign
	// "utm" parameters, like "utm_source"
	private static final String CAMPAIGN_SOURCE_PARAM = "utm_source";


	static class VoiceCookie {
		ChatItem storeResultInItem;
	}

	// Different requests to Eva all come back to the same callback (onEvaReply)
	// (eg text vs voice, add vs delete or replace)
	// the "cookie" parameter that you use for the request is returned in the
	// callback, so you can differentiate between the different calls
	private static final VoiceCookie VOICE_COOKIE = new VoiceCookie();
	private static final Object DELETED_UTTERANCE_COOKIE = new Object();
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
		try {
			cancelBackgroundThreads();
			EasyTracker.getInstance(this).activityStop(this);  // Add this method.
            eventManager.fire(new OnStopEvent(this));
        } finally {
            super.onStop();
        }
	}
	

	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    // getIntent() should always return the most recent
	    setIntent(intent);
	}
	
	@Override 
	public void onResume() {
		Log.d(TAG, "onResume()");
		super.onResume();
		
		Intent myIntent = getIntent();
		if ("com.google.android.gms.actions.SEARCH_ACTION".equals(myIntent.getAction())) {
			startNewSession(false);
			String searchString = myIntent.getStringExtra(SearchManager.QUERY);
			ChatItem chat = new ChatItem(searchString);
			addChatItem(chat);
			VOICE_COOKIE.storeResultInItem = chat;
			VHAApplication.EVA.searchWithText(searchString, VOICE_COOKIE, false);
			
			// clear the intent - it shouldn't run again resuming!
			onNewIntent(new Intent());
		}
		
		VHAApplication.EVA.onResume();
//		setDebugData(DebugTextType.None, null);
		
		VolumeUtil.register(this, this);
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "onPause()");
		VHAApplication.EVA.onPause();
		
		VolumeUtil.unregister(this);
		super.onPause();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) { // Called when the activity is first created.
		Log.d(TAG, "onCreate()");
		
		final RoboInjector injector = RoboGuice.getInjector(this);
        eventManager = injector.getInstance(EventManager.class);
        injector.injectMembersWithoutViews(this);
        super.onCreate(savedInstanceState);
        eventManager.fire(new OnCreateEvent<Activity>(this,savedInstanceState));
    
	
		
		SettingsAPI.getLocale(this);
		VHAApplication.EVA = new EvaComponent(this, this);
		EvaComponent eva = VHAApplication.EVA;
		eva.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		
		setVolumeControlStream(VolumeUtil.currentStream); // TODO: move to EvaComponent?
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
		
		
		eva.setApiKey(SettingsAPI.getEvaKey(this));
		eva.setSiteCode(SettingsAPI.getEvaSiteCode(this));
		
		CommonParameters.currencyCode = SettingsAPI.getCurrencyCode(this);
		
//		if (savedInstanceState != null  && MyApplication.FOUND_HOTELS.size() > 0) { // Restore state
//			// Same code as onRestoreInstanceState() ?
//			Log.d(TAG, "restoring saved instance state");
//			mTabTitles = savedInstanceState.getStringArrayList("mTabTitles");
//		} else {
//			Log.d(TAG, "no saved instance state");
			mTabTitles = new ArrayList<String>(Arrays.asList(/*mExamplesTabName,*/ getString(R.string.CHAT)));
//		}
		
		mainView = new MainView(this, injector, mTabTitles);
		TutorialController.mainView = mainView; // accessible to all tutorials
		
		if (savedInstanceState == null) {
			clearChatList();
		}
		
	
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
		
		ActionBar actionBar = getSupportActionBar();
		actionBar.setHomeButtonEnabled(true);
		
		// patch for debug - bypass the speech recognition:
		// Intent data = new Intent();
		// Bundle a_bundle = new Bundle();
		// ArrayList<String> sentences = new ArrayList<String>();
		// sentences.add("3 star hotel in rome");
		// a_bundle.putStringArrayList(RecognizerIntent.EXTRA_RESULTS, sentences);
		// data.putExtras(a_bundle);
		// onActivityResult(VOICE_RECOGNITION_REQUEST_CODE, RESULT_OK, data);
		
	}
	
	@Override
	public void onBackPressed() {
	   Log.d(TAG, "onBackPressed Called");

	   // cancel recording if during recording
	   if (speechSearch.isInSpeechRecognition()) {
		   Log.i(TAG, "Canceling recording");
		   speechSearch.cancel();
		   mainView.deactivateSearchButton();
		   mainView.hideStatus();
		   mainView.hideSpeechWave();
		   TutorialController.canceledRecording(this);
		   return;
	   }
	   
	   if (TutorialController.onBackPressed(this)) {
		   return;
	   }
	   
	   int chatInd = mainView.getChatTabIndex();
	   if (mainView.getCurrentPage() == chatInd) {
		   // close edit text if chat text is being edited
		   boolean handled = mainView.getChatFragment().handleBackPressed();
		   if (!handled) {
			   // nothing handled - close application
			   Log.i(TAG, "Back pressed, nothing to close - closing activity");
			   super.onBackPressed();
		   }
	   }
	   else {
		   // switch to Chat tab if not there 
		   mainView.setCurrentItem(chatInd);
	   }
	}
	

	
//	@Override
//	public boolean onPrepareOptionsMenu (Menu menu) {
//		//menu.getItem(2).setVisible(eva.isDebug());
//		return super.onPrepareOptionsMenu(menu);
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mainmenu, menu);

		soundControlMenuItem = menu.findItem(R.id.audio);
		
		setVolumeIcon();

		return true;
	}
	
	

	private void setVolumeIcon() {
		if (soundControlMenuItem == null) {
			return;
		}
		VolumeUtil.checkVolume(this);
		soundControlMenuItem.setIcon(VolumeUtil.getVolumeIcon());
	}
	

	@Override
	public void onVolumeChange() {
		setVolumeIcon();
	}





	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // user pressed the menu button
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			mainView.setCurrentItem(mainView.getChatTabIndex());
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

//		case R.id.tutorial:
//			TutorialController.showRecordButtonTutorial(this);
//	        return true;
	        
		case R.id.audio:
			intent = new Intent(this, VolumeSettingsDialog.class);
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
		case R.id.about: // Did the user select "About us"?
			intent = new Intent(this, AboutDialog.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	




	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is killed and restarted.
		Log.d(TAG, "onSaveInstanceState");
		try {
			super.onSaveInstanceState(savedInstanceState);
		}
		catch (IllegalStateException e) {
			// this sometimes happens when "fragment not in fragment manager" - not sure why
			VHAApplication.logError(TAG, "Illegal state while saving instance state in main activity", e);
		}

		savedInstanceState.putString("sessionId", VHAApplication.EVA.getSessionId());
		
		// savedInstanceState.putBoolean("mTtsWasConfigured", mSpeechToTextWasConfigured);
		savedInstanceState.putStringArrayList("mTabTitles", (ArrayList<String>) mTabTitles);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		// restore state:
		// mSpeechToTextWasConfigured = savedInstanceState.getBoolean("mTtsWasConfigured");
		
		String sessionId = savedInstanceState.getString("sessionId");
		VHAApplication.EVA.setSessionId(sessionId);
	}
	
	private void clearChatList() {
		if (mChatListModel.size() > 0) {
			ChatFragment chatFragment = mainView.getChatFragment();
			if (chatFragment != null && chatFragment.isReady()) {
				chatFragment.clearChat();
			}
			else {
	//			ChatItem lastItem = mChatListModel.get(mChatListModel.size()-1);
				mChatListModel.clear();
	//			if (lastItem.getType() == ChatType.Me) {
	//				addChatItem(lastItem);
	//			}
			}
		}
	}

	
	private void addChatItem(ChatItem item) {
		Log.d(TAG, "Adding chat item  type = "+item.getType()+ "  '"+item.getChat()+"'");
		ChatFragment chatFragment = mainView.getChatFragment();
		if (chatFragment != null && chatFragment.isReady()) {
			chatFragment.addChatItem(item);
		}
		else {
			mChatListModel.add(item);
		}
		//mainView.setCurrentItem(mainView.getChatTabIndex());
	}
	
//	private String handleChat(EvaApiReply apiReply) {
//		if (!apiReply.isFlightSearch() && !apiReply.isHotelSearch() && (apiReply.chat != null)) {
//			if (apiReply.chat.hello != null && apiReply.chat.hello) {
//				return "Why, Hello there!";
//			}
//			if (apiReply.chat.who != null && apiReply.chat.who) {
//				return "I'm your virtual hotel agent";
//			}
//			if (apiReply.chat.meaningOfLife != null && apiReply.chat.meaningOfLife) {
//				return "Staying in awesome hotels, of course!";
//			}
//		}
//		return null;
//	}


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
			VHAApplication.EVA.searchWithLocalVoiceRecognition(4, VOICE_COOKIE, editLastUtterance);
			Tracker defaultTracker = GoogleAnalytics.getInstance(this).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createEvent("native_speech_search", "native_speech_search_start", "", editLastUtterance ? 1l: 0l)
					    .build()
					   );
			
			return;
		}
		
		if (speechSearch.isInSpeechRecognition() == true) {
			speechSearch.stop();
			return;
		}
		
		TutorialController.onMicrophonePressed(this);
		
		VOICE_COOKIE.storeResultInItem = chatItem;
		
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
			ChatFragment chatFragment = mainView.getChatFragment();
			if (chatFragment != null)
				chatFragment.addUtterance();
			mainView.setCurrentItem(mainView.getChatTabIndex());
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
		TutorialController.onEvaReply(this, reply);
		
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
				if (VOICE_COOKIE.storeResultInItem != null) {
					// this voice recognition replaces the last utterance
					mainView.getChatFragment().voiceResponseToChatItem(VOICE_COOKIE.storeResultInItem, chat);
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
				mainView.removeTabs();
				Toast.makeText(MainActivity.this, R.string.no_hotels, Toast.LENGTH_LONG ).show();
			}
			else {
				int index = mTabTitles.indexOf(tabName);
				if (index == -1) {
					mainView.addTab(tabName);
					index = mTabTitles.size()-1;
				} 

				if (id == R.string.HOTELS) {
					int mapIndex = mainView.getMapTabIndex();
					if (mapIndex == -1) {
						mainView.addTab(mainView.getMapTabName());
						mapIndex = mTabTitles.size()-1;
					}
					HotelsMapFragment mapFragment = mainView.getMapFragment();
					mapFragment.onHotelsListUpdated();
					
					mainView.removeTab(mainView.getHotelTabName());
					mainView.removeTab(mainView.getRoomsTabName());
					mainView.removeTab(mainView.getReviewsTabName());
					mainView.removeTab(mainView.getBookingTabName());
					
					HotelListFragment fragment = mainView.getHotelsListFragment();
					if (fragment != null) {
						fragment.newHotelsList();
					}
					else {
						VHAApplication.logError(TAG, "Unexpected hotel list fragment is null");
					}
				}
				
				if (this.switchToResult) {
					// do not switch tabs in the middle of a tutorial
					if (TutorialController.currentTutorial == TutorialController.NO_TUTORIAL)
						mainView.setCurrentItem(index);
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
			
			mainView.removeTabs();
			
			//mSwipeyAdapter.stuffChanged(mainView.getChatTabIndex());
			
			VHAApplication.clearSearch();
		}
	}
	
	private void searchHotels(final ChatItem _chatItem,
			final EvaApiReply _reply,
			final boolean _switchToResult) {
		
		// reached hotel search - no need showing tips & examples next time
		SettingsAPI.setShowIntroTips(this, false);
		
		mSearchExpediaTask = injector.getInstance(HotelListDownloaderTask.class);
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


	/**
	 * Listener to Room-information request complete
	 */
	private class RoomTaskListener extends DownloaderTaskListener {
		private boolean mSwitchToTab = false;

		@Override
		public void endProgressDialog(int id, Object result) { // we got the hotel rooms reply successfully
			Log.d(TAG, "endProgressDialog() for hotel rooms for hotel "+mRoomUpdater.hotelId);
			mainView.hideStatus();
			
			
			int index = mainView.getRoomsTabIndex();
			if (index == -1) {
				mainView.addTab(mainView.getRoomsTabName());
				index = mTabTitles.size()-1;
			}
			RoomsSelectFragement fragment = mainView.getRoomsFragment();
			if (fragment != null) // could be null if not instantiated yet
			{
				fragment.changeHotelId(mRoomUpdater.hotelId);
				if (mSwitchToTab) {
					mainView.setCurrentItem(index);
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
					int index = mainView.getHotelTabIndex();
					if (index != -1) {
						HotelDetailFragment fragment = mainView.getHotelFragment();
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
			int index = mainView.getHotelTabIndex();
			if (index == -1) {
				mainView.addTab(mainView.getHotelTabName());
				index = mTabTitles.size() - 1;
			}
	
			HotelDetailFragment fragment = mainView.getHotelFragment();
			if (fragment != null) // could be null if not instantiated yet
			{
				fragment.changeHotelId(hotelId);
			}

			index = mainView.getHotelTabIndex();
			mainView.setCurrentItem(index);

			index = mainView.getReviewsTabIndex();
			if (index == -1) {
				mainView.addTab(mainView.getReviewsTabName());
			}
			ReviewsFragment reviews = mainView.getReviewsFragment();
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
		startNewSession(true);
	}
	private void startNewSession(boolean speakGreeting) {
//		if (isNewSession() == false) {
			mainView.setCurrentItem(mainView.getChatTabIndex());
			VHAApplication.EVA.resetSession();
			VHAApplication.EVA.stopSearch();
			ChatItem myChat = new ChatItem(ChatItem.START_NEW_SESSION);
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
			if (speakGreeting) {
				VHAApplication.EVA.speak(greeting);
				mainView.flashSearchButton(3);
			}

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

		mainView.setCurrentItem(mainView.getChatTabIndex());
		// ??? mViewPager.setAdapter(null);
		//mTabs.setAdapter(null);
		
		mainView.removeTabs();

		VHAApplication.clearSearch();
	}
	


	// note "onEvent" template is needed for progruard to not break roboguice
	// this event happens after a next page of hotel list results is downloaded
	public void onEventHotelsListUpdated( @Observes HotelsListUpdated event) {
		int mapTabIndex = mainView.getMapTabIndex();
		if (mapTabIndex != -1) {
			HotelsMapFragment frag = (HotelsMapFragment) mainView.getMapFragment();
			if (frag != null) {
				frag.onHotelsListUpdated();
			}
		}
	}
	
	public void onEventRatingClicked( @Observes RatingClickedEvent event) {
		int reviewsIndex = mainView.getReviewsTabIndex();
		if (reviewsIndex != -1)
			mainView.setCurrentItem(reviewsIndex);
	}
	
	public void onEventBookingCompleted( @Observes BookingCompletedEvent event) {
		int reservationTabIndex = mainView.getReservationsTabIndex();
		if (reservationTabIndex == -1) {
			mainView.addTab(mainView.getReservationsTabName());
			reservationTabIndex = mTabTitles.size() - 1;
		}
		
		ReservationDisplayFragment frag = (ReservationDisplayFragment)  mainView.getReservationsFragment();
		if (frag != null) {
			frag.showLatestReservation();
		}
		mainView.setCurrentItem(reservationTabIndex);
	}
	
	
	public void onEventRoomSelected( @Observes RoomSelectedEvent event) {

		Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(event.hotelId);
		VHAApplication.selectedRoom = event.room;
		
		int bookingIndex = mainView.getBookingTabIndex();
		if (bookingIndex == -1) {
			mainView.addTab(mainView.getBookingTabName());
			bookingIndex = mTabTitles.size() - 1;
		}
		BookingFragement frag = (BookingFragement)  mainView.getBookingFragment();
		if (frag != null) {
			frag.changeHotelRoom(hotel, event.room);
		}
		mainView.setCurrentItem(bookingIndex);
	}
	
	public void onEventHotelItemClicked( @Observes HotelItemClicked event) {
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
	
	public void onEventChatItemModified( @Observes ChatItemModified event) {
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
	
	public void onEventHotelSelected( @Observes HotelSelected event) {
		Log.d(TAG, "onHotelSelected("+event.hotelId+")");

		int index = mainView.getRoomsTabIndex();
		if (index == -1) {
			// no rooms tab - will be soon - so mark as switch to it
			if (mRoomUpdater == null) {
				startRoomSearch(event.hotelId);
			}
			mRoomUpdaterListener.switchToTab();
		}
		else {
			// room fragment is available - this is in sync with the selected hotel
			mainView.setCurrentItem(index);
		}
	}
	
	public void onEventToggleMainButtons( @Observes ToggleMainButtonsEvent event) {
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
		
		mainView.removeTab(mainView.getRoomsTabName());
		mainView.removeTab(mainView.getBookingTabName());
		mainView.removeTab(mainView.getReservationsTabName());
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