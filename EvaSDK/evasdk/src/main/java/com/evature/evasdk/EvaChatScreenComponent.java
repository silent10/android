package com.evature.evasdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.evature.evasdk.appinterface.EvaAppScope;
import com.evature.evasdk.appinterface.EvaAppSetup;
import com.evature.evasdk.appinterface.EvaResult;
import com.evature.evasdk.appinterface.EvaCarSearch;
import com.evature.evasdk.appinterface.EvaCruiseSearch;
import com.evature.evasdk.appinterface.EvaLifeCycleListener;
import com.evature.evasdk.appinterface.EvaFlightNavigate;
import com.evature.evasdk.appinterface.EvaFlightSearch;
import com.evature.evasdk.appinterface.EvaHotelSearch;
import com.evature.evasdk.appinterface.EvaReservationHandler;
import com.evature.evasdk.evaapis.EvaComponent;
import com.evature.evasdk.evaapis.EvaSearchReplyListener;
import com.evature.evasdk.evaapis.EvaSpeechRecogComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaApiReply;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTime;
import com.evature.evasdk.evaapis.crossplatform.EvaWarning;
import com.evature.evasdk.evaapis.crossplatform.HotelAttributes;
import com.evature.evasdk.evaapis.crossplatform.ParsedText;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;
import com.evature.evasdk.evaapis.crossplatform.flow.FlowElement;
import com.evature.evasdk.evaapis.crossplatform.flow.NavigateElement;
import com.evature.evasdk.evaapis.crossplatform.flow.QuestionElement;
import com.evature.evasdk.evaapis.crossplatform.flow.ReplyElement;
import com.evature.evasdk.evaapis.crossplatform.flow.StatementElement;
import com.evature.evasdk.model.ChatItem;
import com.evature.evasdk.model.appmodel.AppCruiseSearchModel;
import com.evature.evasdk.model.appmodel.AppFlightNavigateModel;
import com.evature.evasdk.model.appmodel.AppFlightSearchModel;
import com.evature.evasdk.model.appmodel.AppHotelSearchModel;
import com.evature.evasdk.user_interface.ChatAdapter;
import com.evature.evasdk.user_interface.ErrorSpan;
import com.evature.evasdk.util.DLog;
import com.evature.evasdk.util.StringUtils;
import com.evature.evasdk.util.VolumeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.evature.evasdk.util.StringUtils.randomString;


public class EvaChatScreenComponent implements EvaSearchReplyListener, VolumeUtil.VolumeListener {

	@SuppressWarnings("nls")
	private static final String TAG = "EvaChatScreenComponent";

    public static final String INTENT_EVA_CONTEXT = "evature_context";
    public static final String TOKEN_REFRESHED_EVENT = "GCM_TOKEN_REFRESHED";
    public final static String MESSAGE_RECEIVED_EVENT = "GCM_MSG_EVENT";



    private final boolean resetSessionOnLoad;

    private static class StoreResultData {
		ChatItem storeResultInItem;
		boolean editLastUtterance;
		Spannable preEditChat;
        String rid;
        int interimTransctionIndex;
	}
	
	private static class DeleteChatItemsData {
		int dismissFrom;
		int dismissTo;
	}

	// Different requests to Eva all come back to the same callback (onEvaReply)
	// (eg text vs voice, add vs delete or replace)
	// the "cookie" parameter that you use for the request is returned untouched to the
	// callback, so you can differentiate between the different type of activation calls
	private static final StoreResultData VOICE_COOKIE = new StoreResultData();
	private static final StoreResultData TEXT_TYPED_COOKIE = new StoreResultData();
	private static final DeleteChatItemsData DELETE_UTTERANCE_COOKIE = new DeleteChatItemsData();
	
	// key values to save/restore activity state
	@SuppressWarnings("nls")
	private static final String EVATURE_CHAT_LIST = "evature.chat_list";
	@SuppressWarnings("nls")
	private static final String EVATURE_SESSION_ID = "evature.session_id";

    private static final String EMPTY_FRAGMENT_TAG = "evature_empty_fragment_tag";

	private EvaComponent eva;
	private EvaSpeechRecogComponent speechSearch;
	private EvatureMainView mView;

	private ToneGenerator toneGenerator;  // TODO: replace with custom sounds

    private static String evaSessionId = "1";
    private static ArrayList<ChatItem> chatItems = new ArrayList<ChatItem>();

    private boolean mUseExtraFragmentForBackHack = false; // fragments can't capture "back" button, so instead can use special hack of starting an extra fragment and checking the fragment manager backstack


    private boolean isPaused;
	private boolean mShownWarningsTutorial;
	private static class PendingSayIt {
		public ChatItem chatItem;
		public String sayIt;
		public boolean cancel;
        public boolean alreadySpoken;
		public boolean isQuestion;
		public Spannable chatText;
	}
	private PendingSayIt pendingReplySayit = new PendingSayIt();

    private View rootView;

    private Activity activity;
    public Activity getActivity() {
        return activity;
    }
    private final Executor executor = Executors.newCachedThreadPool();


    public BroadcastReceiver gcmTokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null ) {
                String token = intent.getStringExtra("token");
                EvaAppSetup.gcmToken = token;
            }
        }
    };

    public BroadcastReceiver gcmMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null ) {
                String jStreamingResult = intent.getStringExtra("streaming_result");
                if (jStreamingResult == null) {
                    Log.e(TAG, "streaming result is null");
                    return;
                }
                JSONArray streamingResult;
                try {
                    streamingResult = new JSONArray(jStreamingResult);
                } catch (JSONException e) {
                    Log.e(TAG, "Failed parsing streaming result", e);
                    return;
                }
                String rid = intent.getStringExtra("rid");
                int index = intent.getIntExtra("index", -1);
                boolean isFinal = intent.getBooleanExtra("is_final", false);
                StringBuilder sb = new StringBuilder(streamingResult.length());
                try {
                    for (int i=0; i<streamingResult.length(); i++) {
                        sb.append(streamingResult.getJSONObject(i).getString("transcript"));
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Failed parsing streaming result", e);
                    return;
                }
                String text = sb.toString();
                if (rid == null) {
                    Log.w(TAG, "rid="+rid +"  and text="+text);
                    return;
                }
                if (VOICE_COOKIE != null && rid.equals(VOICE_COOKIE.rid)) {
                    // this transcription is related to ongoing recording
                    if (index <= VOICE_COOKIE.interimTransctionIndex) {
                        Log.w(TAG, "index="+index+ " but existing index="+VOICE_COOKIE.interimTransctionIndex+"  -- ignoring");
                        return;
                    }
                    VOICE_COOKIE.interimTransctionIndex = index;

                    SpannableString stabilityHighlightedText = new SpannableString(text);
                    if (!isFinal) {
                        try {
                            int col = ContextCompat.getColor(activity, R.color.evature_text_muted);
                            int pos = 0;
                            for (int i = 0; i < streamingResult.length(); i++) {
                                JSONObject transcriptObj = streamingResult.getJSONObject(i);
                                String transcript = transcriptObj.getString("transcript");
                                double stability = transcriptObj.getDouble("stability");
                                int pos2 = pos + transcript.length();
                                if (stability < 0.5) {
                                    stabilityHighlightedText.setSpan(new ForegroundColorSpan(col), pos, pos2, 0);
                                }
                                pos = pos2;
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, "Failed parsing streaming result", e);
                            return;
                        }
                    }

                    if (VOICE_COOKIE.storeResultInItem != null) {
                        VOICE_COOKIE.storeResultInItem.setChat(stabilityHighlightedText);
                        mView.notifyDataChanged();
                    }
                    else {
                        ChatItem chatItem = new ChatItem(stabilityHighlightedText);
                        VOICE_COOKIE.storeResultInItem = chatItem;
                        mView.addChatItem(chatItem);
                    }
                }
            }
        }
    };


    public EvaChatScreenComponent(Activity hostActivity,
                                  boolean useExtraFragmentForBackHack,
                                  boolean resetSession
                                  ) {
        this.activity = hostActivity;
        this.resetSessionOnLoad = resetSession;
        this.mUseExtraFragmentForBackHack = useExtraFragmentForBackHack;
    }
    public EvaChatScreenComponent(Activity hostActivity) {
        this(hostActivity, false, false);
    }

	@SuppressLint("NewApi")
	protected void onCreate(Bundle savedInstanceState) {
        DLog.d(TAG, "onCreate");
		//super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = this.getWindow();
//            window.addFlags(Window.FEATURE_ACTIVITY_TRANSITIONS);
//            window.setSharedElementEnterTransition(new ChangeImageTransform());
//            window.setSharedElementExitTransition(new ChangeImageTransform());
//        }


		Intent theIntent = activity.getIntent();
		Bundle bundle = theIntent.getExtras();
		DLog.i(TAG, "Bundle: " + bundle);

        try {
            toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        }
        catch(Exception e) {
            DLog.e(TAG, "Failed to create tone generator ",e);
        }

		EvaComponent.EvaConfig config = new EvaComponent.EvaConfig(EvaAppSetup.apiKey, EvaAppSetup.siteCode);
		// Override Eva's deviceId with ICR deviceId
		/*TODO String deviceId =  ICruiseUtility.getDeviceID(this);
		if (deviceId != null && deviceId.equals("") == false) {
			config.deviceId = deviceId;
		}*/
        if (EvaAppSetup.deviceId != null) {
            config.deviceId =  EvaAppSetup.deviceId;
        }
        config.appVersion = EvaAppSetup.appVersion;

        if (EvaAppSetup.scopeStr == null) {
            config.scope = inferScopeFromHandler();
        }
        else {
            config.scope = EvaAppSetup.scopeStr;
        }
        config.context = theIntent.getStringExtra(INTENT_EVA_CONTEXT);
        config.extraParams = EvaAppSetup.extraParams;

        config.locationEnabled = EvaAppSetup.locationTracking;

        config.semanticHighlightingTimes = EvaAppSetup.semanticHighlightingTimes;
        config.semanticHighlightingLocations = EvaAppSetup.semanticHighlightingLocations;
        config.semanticHighlightingHotelAttributes = EvaAppSetup.semanticHighlightingHotelAttributes;
        config.autoOpenMicrophone = EvaAppSetup.autoOpenMicrophone;

        if (EvaAppSetup.vproxyHost != null) {
            config.vproxyHost = EvaAppSetup.vproxyHost;
        }

		eva = new EvaComponent(activity, this, config);
		eva.onCreate(savedInstanceState);

		speechSearch = new EvaSpeechRecogComponent(eva);
		isPaused = false;
	}

    public List<String> getExamplesStrings() {

        String evaContext = eva.getContext() == null ? eva.getScope() : eva.getContext();

        List<String> examples = new ArrayList<String>();
        Resources resources = activity.getResources();
        if (evaContext.contains(EvaAppScope.Flight.toString())) {
            String[] examplesArr = resources.getStringArray(R.array.evature_examples_flights);
            examples.addAll(Arrays.asList(examplesArr));
        }
        if (evaContext.contains(EvaAppScope.Cruise.toString())) {
            String[] examplesArr = resources.getStringArray(R.array.evature_examples_cruises);
            examples.addAll(Arrays.asList(examplesArr));
        }
        if (evaContext.contains(EvaAppScope.Car.toString())) {
            String[] examplesArr = resources.getStringArray(R.array.evature_examples_cars);
            examples.addAll(Arrays.asList(examplesArr));
        }
        if (evaContext.contains(EvaAppScope.Hotel.toString())) {
            String[] examplesArr = resources.getStringArray(R.array.evature_examples_hotels);
            examples.addAll(Arrays.asList(examplesArr));
        }
        return examples;
    }

    private String getGreeting() {
        Resources resources = activity.getResources();
        String evaContext = eva.getContext() == null ? eva.getScope() : eva.getContext();

        if (evaContext.equals(EvaAppScope.Flight.toString())) {
            return resources.getString(R.string.evature_greeting_flight);
        }
        if (evaContext.equals(EvaAppScope.Cruise.toString())) {
            return resources.getString(R.string.evature_greeting_cruise);
        }
        if (evaContext.equals(EvaAppScope.Car.toString())) {
            return resources.getString(R.string.evature_greeting_car);
        }
        if (evaContext.equals(EvaAppScope.Hotel.toString())) {
            return resources.getString(R.string.evature_greeting_hotel);
        }

        return resources.getString(R.string.evature_greeting);
    }

    public View createMainView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DLog.d(TAG, "createMainView");
        rootView = inflater.inflate(R.layout.evature_chat_layout, container, false);
        if (resetSessionOnLoad) {
            evaSessionId = "1";
            eva.setSessionId(evaSessionId);
        }
        else if (savedInstanceState != null) {
            chatItems = (ArrayList<ChatItem>) savedInstanceState.getSerializable(EVATURE_CHAT_LIST);
            evaSessionId = savedInstanceState.getString(EVATURE_SESSION_ID);
            eva.setSessionId(evaSessionId);
        }

        if (evaSessionId.equals("1")) {
            chatItems.clear();
            mView = new EvatureMainView(this, chatItems);
            Resources resources = activity.getResources();
            String greeting = getGreeting();
            int pos = greeting.length();
            String seeExamples = resources.getString(R.string.evature_tap_for_examples);
            SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
            int col = ContextCompat.getColor(activity, R.color.evature_chat_secondary_text);
            sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
            sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
            ChatItem chat = new ChatItem(sgreet,null, ChatItem.ChatType.EvaWelcome);
            mView.addChatItem(chat);
            speak(greeting, true, new Runnable() {

                @Override
                public void run() {
                    mView.flashSearchButton(5);
                    if (eva.getAutoOpenMicrophone()) {
                        voiceRecognitionSearch(null, false);
                    }
                }
            });
        }
        else {
            mView = new EvatureMainView(this, chatItems);
            eva.setSessionId(evaSessionId);
            mView.flashSearchButton(5);
            if (eva.getAutoOpenMicrophone()) {
                voiceRecognitionSearch(null, false);
            }
        }

        mView.setVolumeIcon();

        return rootView;
    }



    public View getRootView() {
        return rootView;
    }


    private final SimpleDateFormat evaDateFormat = new SimpleDateFormat("yyyy-M-dd", Locale.US);



    private void findSearchResults(final EvaApiReply reply, final FlowElement flow, final ChatItem chatItem) {

        EvaLocation from = null;
        EvaLocation to = null;
        FlowElement.TypeEnum  searchType = flow.Type;
        boolean isComplete = false;
        switch (flow.Type) {
            case Cruise:
            case Flight:
            case Car:
                if (flow.RelatedLocations.length < 2) {
                    DLog.w(TAG, flow.Type.toString()+" search without two locations?");
                    chatItem.setSearchModel(null);
                    return;
                }
                from = flow.RelatedLocations[0];
                to = flow.RelatedLocations[1];
                isComplete = true;
                break;

            case Hotel:
                if (flow.RelatedLocations.length < 1) {
                    DLog.w(TAG, flow.Type.toString()+" search without a location");
                    chatItem.setSearchModel(null);
                    return;
                }
                from = flow.RelatedLocations[0];
                isComplete = true;
                break;

            case Question:
                QuestionElement qe = (QuestionElement)flow;
                searchType = qe.actionType;
                if (searchType == FlowElement.TypeEnum.Hotel && flow.RelatedLocations != null) {
                    from = flow.RelatedLocations[0];
                }
                else if (reply.locations != null) {
                    // assuming only origin and destination...
                    if (reply.locations.length > 0) {
                        from = reply.locations[0];
                    }
                    if (reply.locations.length > 1) {
                        to = reply.locations[1];
                    }
                }
                isComplete = false;
                break;
        }
        EvaResult result = null;

        if (searchType != null) {
            switch (searchType) {
                case Cruise:
                    result = findCruiseResults(isComplete, from, to, reply, chatItem);
                    break;

                case Flight:
                    result = findFlightResults(isComplete, from, to, reply, chatItem);
                    break;

                case Hotel:
                    result = findHotelResults(isComplete, from, reply, chatItem);
                    break;

                case Navigate:
                    result = navigateTo(((NavigateElement) flow).pagePath, reply, chatItem);
                    break;
            }
        }

        handleCallbackResult(result, flow, chatItem);
    }

    void handleCallbackResult(EvaResult result, FlowElement flow, final ChatItem chatItem) {
        handleCallbackResult(result, flow, chatItem, true);
    }

    private String resultsFoundText(FlowElement flow, int count) {
        if (flow == null) {
            DLog.e(TAG, "unexpected showing results found without flow type");
            return count +" results found.";
        }
        FlowElement.TypeEnum type = flow.Type;
        if (flow.Type == FlowElement.TypeEnum.Question) {
            type = ((QuestionElement) flow).actionType;
        }
        if (count == 1) {
            return "One " + type.toString().toLowerCase() + " found.";
        }
        else {
            return count + " " + type.toString().toLowerCase() + "s found.";
        }
    }

    private void handleCallbackResult(EvaResult result, final FlowElement flow, final ChatItem chatItem, final boolean firstResult) {
        if (result == null) {
            result = EvaResult.defaultHandling();
        }
        String sayIt = result.getSayIt();
        Spannable displayIt = result.getDisplayIt();

        // handle synchronous count - if exists
        // show results count in sub-label, special handling for zero results and one result
        int count = result.getCountResult();
        if (count == 0) {  // no matching results found
            String noResultsFound = activity.getString(R.string.evature_zero_count);
            sayIt = noResultsFound; // override the sayit displayIt
            displayIt = new SpannableString(noResultsFound);
            chatItem.setSearchModel(null); // don't allow tapping to see empty search results
            chatItem.setSubLabel(null);
            chatItem.setStatus(ChatItem.Status.NONE);
            mView.notifyDataChanged();
        }
        else if (count > 0) {
            if (flow != null) {
                if (count == 1) { // exactly one found
                    sayIt = activity.getString(R.string.evature_one_count);
                    displayIt = new SpannableString(sayIt);
                    chatItem.setSubLabel(null);
                    // only one result - no need to ask more questions - go ahead and show the search result
                    if (!chatItem.getSearchModel().getIsComplete()) {
                        chatItem.getSearchModel().setIsComplete(true);
                        // TODO: trigger search here?
                        EvaResult result2 = chatItem.getSearchModel().triggerSearch(getActivity());
                        handleCallbackResult(result2, null, chatItem, false);
                        return;
                    }
                } else { // more than 1
                    chatItem.setSubLabel(resultsFoundText(flow, count) + (EvaAppSetup.tapChatToActivate ? "\nTap here to see them.": ""));
                }
            }
        }


        // handle async count - if exists
        final Future<Integer> asyncCount = result.getDeferredCountResults();

        if (sayIt == null) {
            if (flow != null) {
                sayIt = flow.getSayIt();
            }
        }
        else {
            if (result.isAppendToExistingText() && flow != null) {
                sayIt = flow.getSayIt() + sayIt;
            }
        }
        if (displayIt == null) {
            if (flow != null) {
                displayIt = new SpannableString(flow.getSayIt());
            }
        }
        else {
            if (result.isAppendToExistingText()) {
                if (flow != null) {
                    displayIt = new SpannableString(TextUtils.concat(flow.getSayIt(), displayIt));
                }
                else {
                    displayIt = new SpannableString(TextUtils.concat(chatItem.getChat(), displayIt));
                }
            }
        }

        if (sayIt != null && !"".equals(sayIt)) {
            if (asyncCount == null) {  // not waiting for count - speak immediately
                if (flow != null && flow.Type ==  FlowElement.TypeEnum.Statement && ((StatementElement) flow).StatementType == StatementElement.StatementTypeEnum.Chat &&
                        ((StatementElement) flow).newSession) {
                    speak(sayIt, false, new Runnable() {
                        public void run() {
                            DLog.d(TAG, "New session started");
                            mView.flashSearchButton(3);
                            if (eva.getAutoOpenMicrophone()) {
                                voiceRecognitionSearch(null, false);
                            }
                        }
                    });
                }
                else {
                    speak(sayIt, firstResult);  // flush the speak queue only if this is the first handleResult in a chain
                }

            }
            else {
                pendingReplySayit.alreadySpoken = false;
                pendingReplySayit.cancel = false;
                pendingReplySayit.chatItem = chatItem;
                pendingReplySayit.sayIt = sayIt;
                pendingReplySayit.chatText = displayIt;
                chatItem.setChat("");
                mView.notifyDataChanged();

                // delay speak in another thread - give async count a chance to override the speak
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DLog.d(TAG, "Delaying speak to give async count time");
                            synchronized (pendingReplySayit) {
                                pendingReplySayit.wait(EvaAppSetup.delaySpeakWhenWaitingForCount); // wait some for async count
                                // - maybe no results will show and this will be canceled
                            }
                            if (pendingReplySayit.cancel == false) {
                                DLog.d(TAG, "Done - speaking now the pending chat");
                                // not canceled by async count - can go ahead and ask questions, etc...
                                pendingReplySayit.cancel = true;
                                pendingReplySayit.alreadySpoken = true;
                                pendingReplySayit.chatItem.setChat(pendingReplySayit.chatText);
                                mView.notifyDataChanged();
                                if (pendingReplySayit.isQuestion) {
                                    speak(pendingReplySayit.sayIt, false, new Runnable() {
                                        public void run() {
                                            DLog.d(TAG, "Question asked");
                                            mView.flashSearchButton(3);
                                            if (eva.getAutoOpenMicrophone()) {
                                                voiceRecognitionSearch(null, false);
                                            }
                                        }
                                    });
                                } else {
                                    speak(pendingReplySayit.sayIt, false);
                                }
                            } else {
                                DLog.d(TAG, "Not speaking because canceled by async count");
                            }

                        } catch (InterruptedException e) {

                        }
                    }
                });

            }
        }
        if (asyncCount != null) {
            // wait for async count
            chatItem.setStatus(ChatItem.Status.SEARCHING);
            chatItem.setSubLabel("Searching...");
            mView.notifyDataChanged();
            pendingReplySayit.isQuestion = flow != null && flow.Type ==  FlowElement.TypeEnum.Question;
            // wait for async count
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    int count = -1;
                    try {
                        Integer countResult = asyncCount.get(EvaAppSetup.timeoutWaitingForCount, TimeUnit.MILLISECONDS);
                        if (countResult != null) {
                            count = countResult.intValue();
                        }
                    } catch (InterruptedException|ExecutionException e) {
                        DLog.w(TAG, "interrupted while waiting for async count");
                    } catch (TimeoutException e) {
                        DLog.i(TAG, "timeout while waiting for async count");
                    }

                    if (count < 0) {
                        DLog.w(TAG, "No count response");
                        chatItem.setSearchModel(null); // don't allow tapping to see empty search results
                        chatItem.setSubLabel(null);
                        chatItem.setStatus(ChatItem.Status.NONE);
                        mView.notifyDataChanged();
                        return;
                    }

                    DLog.d(TAG, "Count result: " + count);
                    if (count == 0) {
                        // attempt to cancel the pending sayit - no need to ask question or say the cruise if there are no results
                        pendingReplySayit.cancel = true;
                        synchronized (pendingReplySayit) {
                            pendingReplySayit.notifyAll();
                        }

                        // hide the searching sublabel
                        chatItem.setSubLabel(null);
                        chatItem.setStatus(ChatItem.Status.NONE);
                        String noResultsFound = activity.getString(R.string.evature_zero_count);
                        if (pendingReplySayit.alreadySpoken) {
                            // already spoken - add a new chat item
                            ChatItem noResults = new ChatItem(noResultsFound, chatItem.getEvaReplyId(), ChatItem.ChatType.Eva);
                            mView.addChatItem(noResults);
                        } else {
                            // not spoken yet - modify the existing chat item
                            chatItem.setChat(noResultsFound);
                            chatItem.setSearchModel(null); // don't allow tapping to see empty search results
                        }
                        speak(noResultsFound, false);
                    } else {

                        if (count == 1) {
                            pendingReplySayit.cancel = true;
                            synchronized (pendingReplySayit) {
                                pendingReplySayit.notifyAll();
                            }

                            if (pendingReplySayit.alreadySpoken) {
                                chatItem.setSubLabel(resultsFoundText(flow, count));
                            }
                            else {
                                // cancel the pending chat - instead say the "one result" text
                                String oneResultFound = activity.getString(R.string.evature_one_count);
                                chatItem.setChat(oneResultFound);
                                chatItem.setSubLabel(null);
                                speak(oneResultFound, false);
                            }
                        }
                        else { // > 1
                            // there are results - can go ahead and say the pending sayIt
                            synchronized (pendingReplySayit) {
                                pendingReplySayit.notifyAll();
                            }
                            chatItem.setSubLabel(resultsFoundText(flow, count) + (EvaAppSetup.tapChatToActivate ? "\nTap here to see them.": ""));
                        }
                        chatItem.setStatus(ChatItem.Status.HAS_RESULTS);

                        if (!pendingReplySayit.alreadySpoken && !chatItem.getSearchModel().getIsComplete() && count == 1) {
                            // there is only one result found - no need to ask more questions
                            chatItem.getSearchModel().setIsComplete(true);
                            // TODO: trigger search here?
                            EvaResult result2 = chatItem.getSearchModel().triggerSearch(getActivity());
                            handleCallbackResult(result2, null, chatItem, false);
                        }
                    }
                    mView.notifyDataChanged();
                }
            });
        }
        else {
            if (displayIt != null) {
                chatItem.setChat(displayIt);
                mView.notifyDataChanged();
            }
        }

        if (result.isCloseChat()) {
            EvaChatTrigger.closeChatScreen(getActivity());
        }

        final Future<EvaResult> future = result.getDeferredResult();
        if (future != null) {
            chatItem.setStatus(ChatItem.Status.SEARCHING);
            chatItem.setSubLabel("Searching...");
            mView.notifyDataChanged();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    EvaResult result2 = null;
                    try {
                        result2 = future.get(EvaAppSetup.timeoutWaitingForResult, TimeUnit.MILLISECONDS);
                        chatItem.setStatus(ChatItem.Status.NONE);
                        chatItem.setSubLabel(null);
                        mView.notifyDataChanged();
                        handleCallbackResult(result2, null, chatItem, false);
                        return;
                    } catch (InterruptedException e) {
                        DLog.e(TAG, "Interrupted waiting for deferred result", e);
                    } catch (ExecutionException e) {
                        DLog.e(TAG, "Exception waiting for deferred result", e);
                    } catch (TimeoutException e) {
                        DLog.e(TAG, "Timeout waiting for deferred result", e);
                    }
                    chatItem.setStatus(ChatItem.Status.NONE);
                    chatItem.setSubLabel(null);
                    mView.notifyDataChanged();
                }
            });
        }

    }


    private EvaResult findFlightResults(final boolean isComplete, final EvaLocation from, final EvaLocation to, final EvaApiReply reply, final ChatItem chatItem) {

        // TODO: we default to round trip - maybe should use EvaAppSetup to decide if default to round trip or one way?
        boolean oneWay = reply.flightAttributes != null && reply.flightAttributes.oneWay != null && reply.flightAttributes.oneWay.booleanValue() == true;

        Date departDateMin = null;
        Date departDateMax = null;
        String departureStr = (from != null && from.departure != null) ? from.departure.date : null;
        if (departureStr != null) {
            try {
                departDateMin = evaDateFormat.parse(departureStr);

                Integer days = from.departure.daysDelta();
                if (days != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(departDateMin); // Now use today date.
                    c.add(Calendar.DATE, days.intValue()); // Adding duration days
                    departDateMax = c.getTime();
                }
            } catch (ParseException e) {
                DLog.e(TAG, "Failed to parse eva departure date: " + departureStr);
                e.printStackTrace();
            }
        }

        Date returnDateMin = null;
        Date returnDateMax = null;

        if (!oneWay) {
            String returnStr = (to != null && to.departure != null) ? to.departure.date : null;
            if (returnStr == null) {
                oneWay = true;
            }
            else {
                try {
                    returnDateMin = evaDateFormat.parse(returnStr);

                    Integer days = to.departure.daysDelta();
                    if (days != null) {
                        Calendar c = Calendar.getInstance();
                        c.setTime(departDateMin);
                        c.add(Calendar.DATE, days.intValue()); // Adding duration days
                        returnDateMax = c.getTime();
                    }
                } catch (ParseException e) {
                    DLog.e(TAG, "Failed to parse eva departure date: " + departureStr);
                    e.printStackTrace();
                }
            }
        }


        final Context context = activity;
        RequestAttributes.SortEnum sortBy = null;
        RequestAttributes.SortOrderEnum sortOrder = null;
        if (reply.requestAttributes != null) {
            sortBy = reply.requestAttributes.sortBy;
            sortOrder = reply.requestAttributes.sortOrder;
        }

        chatItem.setSearchModel(new AppFlightSearchModel(isComplete, from, to, departDateMin, departDateMax, returnDateMin, returnDateMax, reply.travelers,
                reply.flightAttributes, sortBy, sortOrder));


        if (EvaComponent.evaAppHandler instanceof EvaFlightSearch) {
            return chatItem.getSearchModel().triggerSearch(context);
        }
        // TODO: insert new chat item saying the app doesn't support flight search?
        Log.e(TAG, "App reached flight search, but has no matching handler");
        return null;
    }


    private EvaResult findHotelResults(boolean isComplete, EvaLocation location, EvaApiReply reply, ChatItem chatItem) {

        Date arriveDateMin = null;
        Date arriveDateMax = null;
        String arrivalStr = (location != null && location.arrival != null) ? location.arrival.date : null;
        if (arrivalStr != null) {
            try {
                arriveDateMin = evaDateFormat.parse(arrivalStr);

                Integer days = location.arrival.daysDelta();
                if (days != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(arriveDateMin); // Now use today date.
                    c.add(Calendar.DATE, days.intValue()); // Adding duration days
                    arriveDateMax = c.getTime();
                }
            } catch (ParseException e) {
                DLog.e(TAG, "Failed to parse eva arrival date: " + arrivalStr);
                e.printStackTrace();
            }
        }

        Integer durationMin = null, durationMax = null;
        if (location != null && location.stay != null) {
            if (location.stay.minDelta != null && location.stay.maxDelta != null) {
                durationMin = EvaTime.daysDelta(location.stay.minDelta);
                durationMax = EvaTime.daysDelta(location.stay.maxDelta);
            } else {
                durationMin = location.stay.daysDelta();
                durationMax = durationMin;
            }
        }


        final Context context = activity;
        RequestAttributes.SortEnum sortBy = null;
        RequestAttributes.SortOrderEnum sortOrder = null;
        if (reply.requestAttributes != null) {
            sortBy = reply.requestAttributes.sortBy;
            sortOrder = reply.requestAttributes.sortOrder;
        }

        // merge hotel attributes from location and reply
        HotelAttributes merged = new HotelAttributes();
        ArrayList<HotelAttributes> attributesArray =  new ArrayList<>();
        if (reply.hotelAttributes != null) {
            attributesArray.add(reply.hotelAttributes);
        }
        if (location.hotelAttributes != null) {
            attributesArray.add(location.hotelAttributes);
        }
        for (HotelAttributes ha : attributesArray) {

            if (ha.selfCatering != null) {
                merged.selfCatering = ha.selfCatering;
            }
            if (ha.bedAndBreakfast != null) {
                merged.bedAndBreakfast = ha.bedAndBreakfast;
            }
            if (ha.halfBoard != null) {
                merged.halfBoard = ha.halfBoard;
            }
            if (ha.fullBoard != null) {
                merged.fullBoard = ha.fullBoard;
            }
            if (ha.drinksInclusive != null) {
                merged.drinksInclusive = ha.drinksInclusive;
            }
            if (ha.allInclusive != null) {
                merged.allInclusive = ha.allInclusive;
            }
            if (ha.amenities.size() > 0) {
                merged.amenities = ha.amenities;
            }
            if (ha.parkingFacilities != null) {
                merged.parkingFacilities = ha.parkingFacilities;
            }
            if (ha.parkingFree != null) {
                merged.parkingFree = ha.parkingFree;
            }
            if (ha.parkingValet != null) {
                merged.parkingValet = ha.parkingValet;
            }

            if (ha.pool != null) {
                merged.pool = ha.pool;
            }
//            if (ha.accommodation != EVHotelAttributesAccommodationTypeUnknown) {
//                merged.accommodationType = ha.accommodationType;
//            }

            if (ha.minStars != null) {
                merged.minStars = ha.minStars;
            }
            if (ha.maxStars != null) {
                merged.maxStars = ha.maxStars;
            }
            if (ha.chains.size() > 0) {
                merged.chains = ha.chains;
            }
        }



        chatItem.setSearchModel(new AppHotelSearchModel(isComplete, location,
                arriveDateMin, arriveDateMax,
                durationMin, durationMax,
                reply.travelers,
                merged,
                sortBy, sortOrder));


        if (EvaComponent.evaAppHandler instanceof EvaHotelSearch) {
            return chatItem.getSearchModel().triggerSearch(context);
        }
        // TODO: insert new chat item saying the app doesn't support search?
        Log.e(TAG, "App reached hotel search, but has no matching handler");
        return null;
    }


    private EvaResult navigateTo(String pagePath, EvaApiReply reply, ChatItem chatItem) {
        String[] tokens = pagePath.split("/");
        if (tokens[0].equals("trip")) {
            EvaFlightNavigate.FlightPageType page;
            try {
                page = EvaFlightNavigate.FlightPageType.valueOf(StringUtils.toCamelCase(tokens[1]));

            }
            catch(IllegalArgumentException e) {
                DLog.w(TAG, "Unknown page type "+pagePath);
                page = EvaFlightNavigate.FlightPageType.Unknown;
            }
            chatItem.setSearchModel(new AppFlightNavigateModel(page));
            if (EvaComponent.evaAppHandler instanceof EvaFlightNavigate) {
                return chatItem.getSearchModel().triggerSearch(activity);
            }
            // TODO: insert new chat item saying the app doesn't support search?
            Log.e(TAG, "App reached flight navigate, but has no matching handler");
        }
        else {
            DLog.w(TAG, "Unexpected navigate pagePath "+pagePath);
        }
        return null;
    }


    private EvaResult findCruiseResults(boolean isComplete, EvaLocation from, EvaLocation to, final EvaApiReply reply, final ChatItem chatItem) {
        Date dateFrom = null, dateTo = null;
        Integer durationFrom = null, durationTo = null;

        String departure = (from != null && from.departure != null) ? from.departure.date : null;
        if (departure != null) {
            try {
                dateFrom = evaDateFormat.parse(departure);

                Integer days = from.departure.daysDelta();
                if (days != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(dateFrom); // Now use today date.
                    c.add(Calendar.DATE, days.intValue()); // Adding duration days
                    dateTo = c.getTime();
                }
            } catch (ParseException e) {
                DLog.e(TAG, "Failed to parse eva departure date: " + departure);
                e.printStackTrace();
            }
        }

        if (to != null && to.stay != null) {
            if (to.stay.minDelta != null && to.stay.maxDelta != null) {
                durationFrom = EvaTime.daysDelta(to.stay.minDelta);
                durationTo = EvaTime.daysDelta(to.stay.maxDelta);
            } else {
                durationFrom = to.stay.daysDelta();
                durationTo = durationFrom;
            }
        }

        if (from != null && from.nearestCustomerLocation != null) {
            from = from.nearestCustomerLocation;
        }
        if (to != null && to.nearestCustomerLocation != null) {
            to = to.nearestCustomerLocation;
        }

        RequestAttributes.SortEnum sortBy = null;
        RequestAttributes.SortOrderEnum sortOrder = null;

        if (reply.requestAttributes != null) {
            sortBy = reply.requestAttributes.sortBy;
            sortOrder = reply.requestAttributes.sortOrder;
        }

        chatItem.setSearchModel(new AppCruiseSearchModel(isComplete, from, to, dateFrom, dateTo, durationFrom, durationTo, reply.cruiseAttributes, sortBy, sortOrder));
        if (EvaComponent.evaAppHandler instanceof EvaCruiseSearch) {
            return chatItem.getSearchModel().triggerSearch(activity);
        }
        // TODO: insert new chat item saying the app doesn't support search?
        Log.e(TAG, "App reached cruise search, but has no matching handler");
        return null;
    }


    public void onSaveInstanceState(Bundle savedInstanceState) {
		DLog.d(TAG, "onSaveInstanceState");
		mView.handleBackPressed(); // cancel edit chat item if one is being edited
		savedInstanceState.putSerializable(EVATURE_CHAT_LIST, mView.getChatListModel());
		savedInstanceState.putString(EVATURE_SESSION_ID, eva.getSessionId());
	}

//    handled in onCreate
//    public void onRestoreInstanceState(Bundle savedInstanceState) {
//        DLog.i(TAG, "onRestoreInstanceState");
//        if (savedInstanceState != null) {
//            ArrayList<ChatItem> chatItems = (ArrayList<ChatItem>) savedInstanceState.getSerializable(EVATURE_CHAT_LIST);
//
//            mView = new EvatureMainView(this, chatItems );
//            eva.setSessionId(savedInstanceState.getString(EVATURE_SESSION_ID));
//            mView.flashSearchButton(5);
//        }
//    }
//
	
	public void onResume() {
        DLog.d(TAG, "onResume");
        for (Iterator<WeakReference<ImageButton>> iterator = EvaChatTrigger.evaButtons.iterator(); iterator.hasNext();) {
            WeakReference<ImageButton> weakRef = iterator.next();
            ImageButton imgButton = weakRef.get();
            if (imgButton == null) {
                iterator.remove();
            }
            else {
                //EvatureMainView.scaleButton(imgButton, 400, 1.0f, 0.0f);
                //EvatureMainView.animateButton(imgButton, "alpha", 400, 1.0f, 0.0f);
                imgButton.setVisibility(View.GONE);
            }
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.activity);
        localBroadcastManager.registerReceiver(gcmTokenReceiver, new IntentFilter(TOKEN_REFRESHED_EVENT));
        localBroadcastManager.registerReceiver(gcmMessageReceiver, new IntentFilter(MESSAGE_RECEIVED_EVENT));


//        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
		isPaused = false;
		Intent intent = activity.getIntent();
		if ("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
            eva.stopSpeak();
			eva.resetSession();
			eva.cancelSearch();
			if (speechSearch != null) {
				speechSearch.cancel();
			}
			mView.clearChatHistory();
			
			String searchString = intent.getStringExtra(SearchManager.QUERY);
			ChatItem chat = new ChatItem(searchString);
			mView.addChatItem(chat);
			VOICE_COOKIE.storeResultInItem = chat;
			eva.searchWithText(searchString, VOICE_COOKIE, false);
			mView.addChatItem(new ChatItem(getActivity().getString(R.string.evature_thinking), null, ChatItem.ChatType.Eva));
			
			// clear the intent - it shouldn't run again resuming!
			//activity.onNewIntent(new Intent());
		}

		eva.onResume();
		VolumeUtil.register(activity, this);

        if (EvaComponent.evaAppHandler != null && EvaComponent.evaAppHandler instanceof EvaLifeCycleListener) {
            ((EvaLifeCycleListener) EvaComponent.evaAppHandler).onResume(mView);
        }
		
	}

//	@Override
//	protected void onNewIntent(Intent intent) {
//	    super.onNewIntent(intent);
//	    // getIntent() should always return the most recent
//	    setIntent(intent);
//	}

	public void onPause() {
		DLog.i(TAG, "onPause");
        evaSessionId = eva.getSessionId();
        for (Iterator<WeakReference<ImageButton>> iterator = EvaChatTrigger.evaButtons.iterator(); iterator.hasNext();) {
            WeakReference<ImageButton> weakRef = iterator.next();
            ImageButton imgButton = weakRef.get();
            if (imgButton == null) {
                iterator.remove();
            }
            else {
                //EvatureMainView.scaleButton(imgButton, 400, 0f, 0.666f);
                //EvatureMainView.animateButton(imgButton, "alpha", 400, 0.0f, 1.0f);
                imgButton.setVisibility(View.VISIBLE);
            }
        }

        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this.activity);
        localBroadcastManager.unregisterReceiver(gcmTokenReceiver);
        localBroadcastManager.unregisterReceiver(gcmMessageReceiver);


        eva.onPause();
		eva.cancelSearch();
		isPaused = true; // don't allow speech callbacks to start followup recording
		// cancel recording if during recording
	    if (speechSearch != null && speechSearch.isInSpeechRecognition()) {
		   DLog.i(TAG, "Canceling recording");
		   speechSearch.cancel();
		   mView.hideSpeechWave();
		   mView.deactivateSearchButton();
	    }
		VolumeUtil.unregister(activity);

        if (EvaComponent.evaAppHandler != null && EvaComponent.evaAppHandler instanceof EvaLifeCycleListener) {
            ((EvaLifeCycleListener) EvaComponent.evaAppHandler).onPause();
        }

    }

    public void onDestroy() {
		eva.onDestroy();
		if (speechSearch != null) {
			speechSearch.onDestroy();
		}
	}
	
	public void speak(String sayIt) {
		speak(sayIt, true);
	}
	
	public void speak(String sayIt, boolean flush) {
		speak(sayIt, flush, null);
	}

	public void speak(String sayIt, boolean flush, final Runnable onComplete) {
		// Do not speak if a recording is taking place!
		if (mView.isRecording() == false) {
			if (onComplete == null) {
				eva.speak(sayIt, flush, null);
			}
			else {
				eva.speak(sayIt, flush, new Runnable() {
					@Override
					public void run() {
						if (isPaused == false)  {
							onComplete.run();
						}
					}
				});
			}
		}
	}
	
	
	private EvaSpeechRecogComponent.SpeechRecognitionResultListener mSpeechSearchListener = new EvaSpeechRecogComponent.SpeechRecognitionResultListener() {
		
		@Override
		public void speechResultError(final String message, final Object cookie) {
			DLog.d(TAG, "Speech recognition error: "+message);
            String networkError = getActivity().getResources().getString(R.string.evature_network_error);
            if (networkError.equals(message)) {
                ChatItem myChat = new ChatItem(networkError, null, ChatItem.ChatType.Eva);
                mView.addChatItem(myChat);
            }
			mView.hideSpeechWave();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
                        if (toneGenerator != null) {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
                            Thread.sleep(120);
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 25);
                        }
					} catch (InterruptedException e) {
					}
					finally {
						activity.runOnUiThread(new Runnable() {
                            public void run() {
                                eva.speechResultError(message, cookie);
                            }
                        });
					}
				}
			});
		}

		@Override
		public void speechResultOK(final String evaJson, final Bundle debugData, final Object cookie) {
			DLog.d(TAG, "Speech recognition ok");
			mView.hideSpeechWave();
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
                        if (toneGenerator != null) {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
                            Thread.sleep(120); // small delay
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 25);
                        }
					} catch (InterruptedException e) {
					}
					finally {
						activity.runOnUiThread(new Runnable() {
                            public void run() {
                                eva.speechResultOK(evaJson, debugData, cookie);
                            }

                            ;
                        });
					}
				}
			});
			
		}
	};
	
	
	
	/*** Start a voice recognition - and place the results in the chatItem (or add
	 * a new one if null) */
	// - adding chatItem to the cookie that will be
	// returned to the onEvaReply callback
	// will be using it in the callback
	// the cookie is just something that will be returned untouched to the
	// "onEvaReply" callback - so in the callback you can tell which
	// code triggered it (ie. there could be multiple entry point all leading to
	// the same callback)
	private void voiceRecognitionSearch(ChatItem chatItem, final boolean editLastUtterance) {

        final String rid = randomString(32);
		VOICE_COOKIE.storeResultInItem = chatItem;
		VOICE_COOKIE.editLastUtterance = editLastUtterance;
        VOICE_COOKIE.rid = rid;
        VOICE_COOKIE.interimTransctionIndex = -1;
		
		executor.execute(new Runnable() {
			
			@Override
			public void run() {
				if (mView.areMainButtonsShown() == false) {
					DLog.d(TAG, "Not starting voice recognition because main button isn't shown");
					return;
				}
				if (mView.isRecording()) {
					DLog.d(TAG, "Not starting voice recognition because isRecording already!");
					return;
				}
				eva.stopSpeak();// stop the speech if there is one going
                speechSearch.fakeAudioStreaming(mSpeechSearchListener, editLastUtterance, rid); // optimization - start the streaming as soon as possible
                if (toneGenerator != null) {
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
                    try {
                        Thread.sleep(120); // small delay
                    } catch (InterruptedException e) {
                    }
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
                    try {
                        Thread.sleep(25); // wait for beep to end - do NOT record the beep
                    } catch (InterruptedException e) {
                    }
                }
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        DLog.i(TAG, "Starting recognizer");
                        eva.stopSpeak();
                        mView.startSpeechRecognition(speechSearch, VOICE_COOKIE);
                        addEmptyFragment();
                    }
                });
			}
		});
	}

	public void resetSession() {
        this.startNewSession();
    }
	

	@SuppressWarnings("nls")
	public void buttonClickHandler(View view) {
		int viewId = view.getId();
		if (viewId == R.id.restart_button) {   // can't use Switch-Case because R.id  isn't final for library code
			startNewSession();
		} else if (viewId == R.id.voice_search_button) {
			if (speechSearch.isInSpeechRecognition() == true) {
				speechSearch.stop();
			} else {
				voiceRecognitionSearch(null, false);
			}
		} else if (viewId == R.id.undo_button) {
			undoLastUserChat();
		}
//		else if (viewId == R.id.volume_button) {
//			Intent intent = new Intent(this, EvaVolumeSettingsDialog.class);
//			startActivity(intent);
//		}
	}// End of evatureClickHandler


	private void undoLastUserChat() {
		ArrayList<ChatItem> chatList= mView.getChatListModel();
		if (chatList.size() > 0) {
			int dismissFrom = -1;
			// search for last user chat - dismiss from the point forward
			for (int i=chatList.size()-1; i>=0; i--) {
				if (chatList.get(i).getType() == ChatItem.ChatType.User) {
					dismissFrom = i;
					break;
				}
			}
			if (dismissFrom > 0) {
				// dismiss also the eva reply that came before it - it will be added again
				String lastId = chatList.get(dismissFrom-1).getEvaReplyId();
				if (lastId != null) {
					for (int i=dismissFrom-1; i>=0; i--) {
						if (lastId.equals(chatList.get(i).getEvaReplyId()) == false) {
							dismissFrom = i+1;
							break;
						}
					}
				}
			}
			
			if (dismissFrom > 0) {
				DELETE_UTTERANCE_COOKIE.dismissFrom = dismissFrom;
				int dismissTo = chatList.size();
				DELETE_UTTERANCE_COOKIE.dismissTo = dismissTo;
				mView.dismissItems(dismissFrom, dismissTo, ChatAdapter.DismissStep.ANIMATE_DISMISS);
			}
		}
		mView.disableSearchButton();
		eva.searchWithText("", DELETE_UTTERANCE_COOKIE, true); // undo last utterance - edit it to an empty string
	}


    /***
     * returns true if handled internally
     * @return
     */
	public boolean onBackPressed() {
	   DLog.d(TAG, "onBackPressed Called");

	   // cancel recording if during recording
	   if (speechSearch.isInSpeechRecognition()) {
		   DLog.i(TAG, "Canceling recording");
		   speechSearch.cancel();
		   mView.deactivateSearchButton();
		   mView.hideSpeechWave();
		   return true;
	   }

		return mView.handleBackPressed();
	}
	
	
	/**** Start new session from menu item */
	@SuppressWarnings("nls")
	private void startNewSession() {
		mView.clearChatHistory();
		eva.stopSpeak();
		eva.cancelSearch();
		if (speechSearch != null) {
			speechSearch.cancel();
 		    mView.hideSpeechWave();
		    mView.deactivateSearchButton();
		}
		
		eva.resetSession();
		// triggered by button - create a "start new session" fake chat
		VOICE_COOKIE.storeResultInItem = null;
        Resources resources = activity.getResources();
		ChatItem myChat = new ChatItem(resources.getString(R.string.evature_user_start_new));
		mView.addChatItem(myChat);
		String greeting = resources.getString(R.string.evature_start_new_session_speak);
		int pos = greeting.length();
		String seeExamples = resources.getString(R.string.evature_tap_for_examples);
		SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
		int col = resources.getColor(R.color.evature_chat_secondary_text);
		sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
		sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
		ChatItem chat = new ChatItem(sgreet,null, ChatItem.ChatType.EvaWelcome);
		mView.addChatItem(chat);
		speak(greeting, true, new Runnable() {
			
			@Override
			public void run() {
				mView.flashSearchButton(3);
				if (eva.getAutoOpenMicrophone()) {
					voiceRecognitionSearch(null, false);
				}
			}
		});
	

	}

	
	/****
	 * newSessionStarted - callback from EvaComponent - activated when a response arrives with different SessionId than was requested 
     *
	 * selfTriggered:
	 * 	  true - user requested new session (eg. said  "clear all")
	 *   false - server decided to change session (eg. session timeout) 
	 */
	@Override
	@SuppressWarnings("nls")
	public void newSessionStarted(boolean selfTriggered) {
		// self triggered are already handled
		if (!selfTriggered) {
			mView.clearChatHistory();
			// triggered from server or by chat - the last chat utterance should be included in this session
			if (VOICE_COOKIE.storeResultInItem != null) {
				mView.addChatItem(VOICE_COOKIE.storeResultInItem);
			}
		}
	}


	

	@SuppressWarnings("nls")
	public void onEventChatItemModified(ChatItem chatItem, Spannable preEditChat, boolean startRecord, boolean editLastUtterance) {
		if (startRecord) {
			if (chatItem == null) {
				DLog.e(TAG, "Unexpected chatItem=null startRecord");
				return;
			}
			voiceRecognitionSearch(chatItem, editLastUtterance);
		} else {
			if (chatItem == null) {
				// removed last item
				undoLastUserChat();
			} else {
				if (editLastUtterance) {
					int index = mView.getChatListModel().indexOf(chatItem);
					mView.dismissItems(index+1, mView.getChatListModel().size(), ChatAdapter.DismissStep.ANIMATE_DISMISS);
				}
				String searchText = chatItem.getChat().toString();
				TEXT_TYPED_COOKIE.storeResultInItem = chatItem;
				TEXT_TYPED_COOKIE.editLastUtterance = editLastUtterance;
				TEXT_TYPED_COOKIE.preEditChat = preEditChat;
				mView.disableSearchButton();
				eva.searchWithText(searchText, TEXT_TYPED_COOKIE, editLastUtterance);
			}
		}
	}

	public void onEvaReply(EvaApiReply reply, Object cookie) {
//		if (reply.chat != null && reply.chat.newSession && cookie == DELETE_UTTERANCE_COOKIE) {
//			mView.clearChatHistory();
//			//mView.getChatListModel().clear();
//			handleFlow(reply);
//			mView.notifyDataChanged();
//			return;
//		}

		SpannableString chat = null;
		boolean hasWarnings = false;
		if (reply.transcribedText != null) {
			// reply of voice -  add a "Me" chat item for the input text
			chat = new SpannableString(reply.transcribedText);
			if (reply.evaWarnings.size() > 0) {
				for (EvaWarning warning: reply.evaWarnings) {
					if (warning.position == -1) {
						continue;
					}
					hasWarnings = true;
					if (warning.text.equals(reply.transcribedText)) {
						// for some odd reason the error span doesn't show when its the entire string, so until a fix is available skip it
						break;
					}
					//chat.setSpan( new ForegroundColorSpan(col), warning.position, warning.position+warning.text.length(), 0);
					chat.setSpan( new ErrorSpan(activity.getResources()), warning.position, warning.position+warning.text.length(), 0);
				}
			}
			if (eva.semanticHighlightingEnabled() && reply.parsedText != null) {
				try {
					if (eva.getSemanticHighlightTimes() && reply.parsedText.times != null) {
						int col = ContextCompat.getColor(activity, R.color.evature_times_markup);
						
						for (ParsedText.TimesMarkup time : reply.parsedText.times) {
							chat.setSpan( new ForegroundColorSpan(col), time.position, time.position+time.text.length(), 0);
						}
					}
					
					if (eva.getSemanticHighlightLocations() && reply.parsedText.locations != null) {
						int col = ContextCompat.getColor(activity, R.color.evature_locations_markup);

						for (ParsedText.LocationMarkup location: reply.parsedText.locations) {
							chat.setSpan( new ForegroundColorSpan(col), location.position, location.position+location.text.length(), 0);
						}
					}

                    if (eva.getSemanticHighlightHotelAttributes() && reply.parsedText.hotelAttributes != null) {
                        int col = ContextCompat.getColor(activity, R.color.evature_hotel_attr_markup);

                        for (ParsedText.HotelAttributesMarkup hotelAttr: reply.parsedText.hotelAttributes) {
                            chat.setSpan( new ForegroundColorSpan(col), hotelAttr.position, hotelAttr.position+hotelAttr.text.length(), 0);
                        }
                    }
				}
				catch (IndexOutOfBoundsException e) {
					DLog.e(TAG, "Index out of bounds setting spans of chat ["+chat+"]", e);
				}
			}
		}
		
		mView.deactivateSearchButton();
        removeEmptyFragment();


		if (VOICE_COOKIE == cookie) {
			if (chat != null) {
				if (VOICE_COOKIE.storeResultInItem != null) {
					// this voice recognition replaces the last utterance
					mView.voiceResponseToChatItem(VOICE_COOKIE.storeResultInItem, chat);
				}
				else {
					mView.addChatItem(new ChatItem(chat));
				}
			}
			VOICE_COOKIE.rid = null;
		}

		if (cookie == TEXT_TYPED_COOKIE) {
			if (TEXT_TYPED_COOKIE.storeResultInItem != null) {
				// replaces the last utterance
				mView.voiceResponseToChatItem(TEXT_TYPED_COOKIE.storeResultInItem, chat);

				if (TEXT_TYPED_COOKIE.editLastUtterance) {
					// we animated the dismiss of the follow up chat items - now edit is success do the actual delete
					int index = mView.getChatListModel().indexOf(TEXT_TYPED_COOKIE.storeResultInItem);
					mView.dismissItems(index+1, mView.getChatListModel().size(), ChatAdapter.DismissStep.DO_DELETE);
				}
			}
		}
		
		if (cookie == DELETE_UTTERANCE_COOKIE) {
			// deleted successfully last utterance 
			// make sure not to play pending SayIt if it was undone before sayIt was spoken 
			pendingReplySayit.cancel = true;
   	    	synchronized (pendingReplySayit) {
   	    		pendingReplySayit.notifyAll();
   	    	}
   	    	// remove the chat items
   	    	mView.dismissItems(DELETE_UTTERANCE_COOKIE.dismissFrom, DELETE_UTTERANCE_COOKIE.dismissTo, ChatAdapter.DismissStep.DO_DELETE);
		}
		
		if (reply.flow != null) {
			handleFlow(reply);
		}

		if (hasWarnings && !mShownWarningsTutorial) {
			mShownWarningsTutorial = true;
			ChatItem warnExplanation = new ChatItem("", reply.transactionId, ChatItem.ChatType.Eva);
			warnExplanation.setSubLabel(activity.getString(R.string.evature_undo_tutorial));
			mView.addChatItem(warnExplanation);
		}
	}


    /***
     * The only purpose of this fragment is to be on top of the chat screen while the recording is running,
     * so pressing "back" will destroy this invisible fragment and trigger the "onBackPressed" of the chatScreen
     * ie. it will cancel the recording
     */
    public static class EmptyFragment extends Fragment {
        boolean necessary = true;

        @Override
        public void onResume() {
            super.onResume();
            if (ChatAdapter.currentEditBox != null) {
                Handler handler = new Handler(Looper.getMainLooper());
                final Runnable r = new Runnable() {
                    public void run() {
                        if (ChatAdapter.currentEditBox != null) {
                            EditText editTextToFocus = (EditText) ChatAdapter.currentEditBox.get();
                            if (editTextToFocus != null) {
                                editTextToFocus.requestFocus();
                                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    }
                };
                handler.postDelayed(r, 1500);
            }
        }

        @Override
        public void onDestroy() {
            DLog.d(TAG, "Destroying empty fragment, triggerback necessary: " + necessary);
            if (necessary) {
                FragmentManager manager = getFragmentManager();
                Fragment fragment = manager.findFragmentByTag(EvaChatScreenFragment.TAG);
                if (fragment != null) {
                    EvaChatScreenFragment evaChatScreenFragment = (EvaChatScreenFragment) fragment;
                    boolean handledInternally = evaChatScreenFragment.chatScreen.onBackPressed();
                }
            }
            super.onDestroy();
        }

        public void notNecessaryAnymore() {
            DLog.d(TAG, "Setting necessary to false");
            necessary = false;
        }
    }


    void addEmptyFragment() {
        if (mUseExtraFragmentForBackHack) {
            DLog.d(TAG, "Adding empty fragment");
            FragmentActivity fa = (FragmentActivity) activity;
            FragmentManager manager = fa.getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(EMPTY_FRAGMENT_TAG);
            if (fragment != null) {
                DLog.e(TAG, "Unexpected fragment stack state - already has empty fragment");
                return;
            }
            EmptyFragment empty = new EmptyFragment();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.evature_root_view, empty, EMPTY_FRAGMENT_TAG);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    void removeEmptyFragment() {
        if (mUseExtraFragmentForBackHack) {
            DLog.d(TAG, "Removing empty fragment");
            FragmentActivity fa = (FragmentActivity)activity;
            FragmentManager manager = fa.getSupportFragmentManager();
            Fragment fragment = manager.findFragmentByTag(EMPTY_FRAGMENT_TAG);
            if (fragment == null) {
                DLog.i(TAG, "Unexpected fragment stack state - missing empty fragment");
                return;
            }
            EmptyFragment emptyFragment = (EmptyFragment) fragment;
            emptyFragment.notNecessaryAnymore();
            manager.popBackStack();  // assume empty fragment is on top!
        }

    }


    public void closeChatScreen() {
        FragmentActivity fa = (FragmentActivity)activity;
        FragmentManager manager = fa.getSupportFragmentManager();
        Fragment fragment = manager.findFragmentByTag(EMPTY_FRAGMENT_TAG);
//        FragmentTransaction transaction = manager.beginTransaction();
        if (fragment != null) {
            DLog.d(TAG, "Removing empty fragment");
//            EmptyFragment emptyFragment = (EmptyFragment) fragment;
//            emptyFragment.notNecessaryAnymore();
//            transaction.remove(emptyFragment);
            manager.popBackStack();  // assume empty fragment is on top!
        }
        else {
            DLog.d(TAG, "No empty fragment found");
        }
        fragment = manager.findFragmentByTag(EvaChatScreenFragment.TAG);
        if (fragment != null) {
//            transaction.remove(fragment);
            DLog.d(TAG, "Removing chat fragment");
            manager.popBackStack();  // assume chat fragment is on top!
        }
        else {
            DLog.e(TAG, "No chat fragment found!");
        }
//        transaction.commit();

    }


    /**** Display chat items for each flow element - execute the first question
	 * element or, if no question element, execute the first flow element
	 * 
	 * @param reply */
	private void handleFlow(EvaApiReply reply) {
		boolean hasQuestion = false;
		for (FlowElement flow : reply.flow.Elements) {
			if (flow.Type == FlowElement.TypeEnum.Question) {
				hasQuestion = true;
				break;
			}
		}

		boolean first = true;

		// if there is a question - show and activate only statements and questions
		// otherwise - show all items and activate the first
		for (FlowElement flow : reply.flow.Elements) {
			ChatItem chatItem = null;
			if (flow.Type == FlowElement.TypeEnum.Question) {
				// This question element for multiple choice answers... not integrated yet :(
//				QuestionElement question = (QuestionElement) flow;
				// DialogQuestionChatItem questionChatItem = new
				// DialogQuestionChatItem(flow.getSayIt(), reply, flow);
				ChatItem questionChatItem = new ChatItem(flow.getSayIt(), reply.transactionId, ChatItem.ChatType.Eva);
				chatItem = questionChatItem;
				mView.addChatItem(questionChatItem);

				// if (question.choices != null && question.choices.length > 0)
				// {
				// for (int index=0; index < question.choices.length; index++) {
				// addChatItem(new DialogAnswerChatItem(questionChatItem, index,
				// question.choices[index]));
				// }
				// }
				executeFlowElement(reply, flow, chatItem);
			} else {
				if (!hasQuestion || flow.Type == FlowElement.TypeEnum.Statement) {
					chatItem = new ChatItem(flow.getSayIt(), reply.transactionId, ChatItem.ChatType.Eva);
					mView.addChatItem(chatItem);
					if (!hasQuestion && flow.Type !=  FlowElement.TypeEnum.Statement && first) {
						first = false;
						// activate only the first non-statement
						executeFlowElement(reply, flow, chatItem);
					}
				}
				if (flow.Type ==  FlowElement.TypeEnum.Statement) {
					executeFlowElement(reply, flow, chatItem);
				}
			}
		}
	}

	
	@SuppressWarnings("nls")
	private void executeFlowElement(EvaApiReply reply, FlowElement flow, ChatItem chatItem) {
		// chatItem.setActivated(true);

		switch (flow.Type) {
			case Reply:
                handleReplyFlow(reply, (ReplyElement) flow, chatItem);
				break;
            case Navigate:
			case Flight:
			case Car:
			case Hotel:
			case Explore:
			case Train:
			case Cruise:
            case Question:
                findSearchResults(reply, flow, chatItem);
				break;

			case Statement:
				StatementElement se = (StatementElement) flow;
				switch (se.StatementType) {
					case Understanding:
					case Unknown_Expression:
					case Unsupported:
						mShownWarningsTutorial = true;
						chatItem.setSubLabel(activity.getString(R.string.evature_undo_tutorial));
						break;
                    case Chat:
                        if (reply.originalInputText != null && reply.originalInputText.toLowerCase().equals("bye bye")) {
                            EvaChatTrigger.closeChatScreen(getActivity());
                        }
                        break;
				}
                handleCallbackResult(null, flow, chatItem);
				break;

            default:
                DLog.w(TAG, "Unexpected flow type "+flow.Type);
				break;

		}
	}


    private void handleReplyFlow(EvaApiReply reply, ReplyElement flow, ChatItem chatItem) {
        EvaResult result = null;
        switch (flow.attributeKey) {
            case CallSupport:
            case Baggage:
            case MultiSegment:
                // TODO: make applicative callbacks for these
                break;
            case ReservationID:
                if (EvaComponent.evaAppHandler instanceof EvaReservationHandler) {
                    result = ((EvaReservationHandler)EvaComponent.evaAppHandler).showReservation();
                }
                break;
            case Cancellation:
                if (EvaComponent.evaAppHandler instanceof EvaReservationHandler) {
                    result = ((EvaReservationHandler)EvaComponent.evaAppHandler).cancelReservation();
                }
                break;
        }
        handleCallbackResult(result, flow, chatItem);
    }

	public void onEvaError(String message, EvaApiReply reply, boolean isServerError, Object cookie) {
		mView.flashBadSearchButton(2);
        removeEmptyFragment();

		// You can show the message with a Toast, or ChatItem
		// Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		// or
		// mView.addChatItem(new ChatItem(message, null, ChatItem.ChatType.Eva));

		// if this is a response to the delete-last-utterance - restore the chat items
		if (cookie == DELETE_UTTERANCE_COOKIE) {
			// we animated-dismissed the items - need to restore them
			mView.dismissItems(DELETE_UTTERANCE_COOKIE.dismissFrom, DELETE_UTTERANCE_COOKIE.dismissTo, ChatAdapter.DismissStep.ANIMATE_RESTORE);
		}
		else {
			// failed edit chat item - restore the chat item text before the edit
			StoreResultData data =  (StoreResultData)cookie;
			if (data.storeResultInItem != null) {
				if (data.preEditChat != null) {
					data.storeResultInItem.setChat(data.preEditChat);
					mView.notifyDataChanged();
				}
			
				// we animated-dismissed the items following the chatitem - need to restore them
				int index = mView.getChatListModel().indexOf(data.storeResultInItem);
				mView.dismissItems(index+1, mView.getChatListModel().size(), ChatAdapter.DismissStep.ANIMATE_RESTORE);
			}
			data.rid = null;
		}
	}

	@Override
	public void onVolumeChange() {
		mView.setVolumeIcon();
	}

    private String inferScopeFromHandler() {
        StringBuilder builder = new StringBuilder();
        if (EvaComponent.evaAppHandler instanceof EvaCarSearch) {
            builder.append(EvaAppScope.Car.toString());
        }
        if (EvaComponent.evaAppHandler instanceof EvaCruiseSearch) {
            builder.append(EvaAppScope.Cruise.toString());
        }
        if (EvaComponent.evaAppHandler instanceof EvaFlightSearch) {
            builder.append(EvaAppScope.Flight.toString());
        }
        if (EvaComponent.evaAppHandler instanceof EvaHotelSearch) {
            builder.append(EvaAppScope.Hotel.toString());
        }
        return builder.toString();
    }
}

