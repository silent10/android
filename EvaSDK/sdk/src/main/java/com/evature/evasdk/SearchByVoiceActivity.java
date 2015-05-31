package com.evature.evasdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Toast;

import com.evature.evasdk.evaapis.android.EvaComponent;
import com.evature.evasdk.evaapis.android.EvaSearchReplyListener;
import com.evature.evasdk.evaapis.android.EvaSpeechRecogComponent;
import com.evature.evasdk.evaapis.crossplatform.EvaApiReply;
import com.evature.evasdk.evaapis.crossplatform.EvaLocation;
import com.evature.evasdk.evaapis.crossplatform.EvaTime;
import com.evature.evasdk.evaapis.crossplatform.EvaWarning;
import com.evature.evasdk.evaapis.crossplatform.ParsedText;
import com.evature.evasdk.evaapis.crossplatform.RequestAttributes;
import com.evature.evasdk.evaapis.crossplatform.ServiceAttributes;
import com.evature.evasdk.evaapis.crossplatform.flow.FlowElement;
import com.evature.evasdk.evaapis.crossplatform.flow.QuestionElement;
import com.evature.evasdk.evaapis.crossplatform.flow.ReplyElement;
import com.evature.evasdk.evaapis.crossplatform.flow.StatementElement;
import com.evature.evasdk.model.ChatItem;
import com.evature.evasdk.util.DLog;
import com.evature.evasdk.EvaAppSetup;

import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;


public class SearchByVoiceActivity extends Activity implements EvaSearchReplyListener, VolumeUtil.VolumeListener {



	@SuppressWarnings("nls")
	private static final String TAG = "SearchByVoiceActivity";

	private static class StoreResultData {
		ChatItem storeResultInItem;
		boolean editLastUtterance;
		SpannableString preEditChat;
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

	private EvaComponent eva;
	private EvaSpeechRecogComponent speechSearch;
	private EvatureMainView mView;

	private ToneGenerator toneGenerator;  // TODO: replace with custom sounds
	

	private boolean isPaused;
	private boolean mShownWarningsTutorial;
	private static class PendingSayIt {
		public ChatItem chatItem;
		public String sayIt;
		public boolean cancel;
		public boolean isQuestion;
		public SpannableString chatText;
	}
	private PendingSayIt pendingReplySayit = new PendingSayIt();
	
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.evature_chat_layout);
		
		// show Eva logs only in Debug build
		DLog.DebugMode = BuildConfig.DEBUG;
		
		Intent myIntent = getIntent();
		Bundle bundle = myIntent.getExtras();
		DLog.i("EvatureChatActivity", "Bundle: " + bundle);

		toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

		EvaComponent.EvaConfig config = new EvaComponent.EvaConfig();
		// Override Eva's deviceId with ICR deviceId
		/*TODO String deviceId =  ICruiseUtility.getDeviceID(this);
		if (deviceId != null && deviceId.equals("") == false) {
			config.deviceId = deviceId;
		}*/
		EvaAppSetup.setupEva(config);

		eva = new EvaComponent(this, this, config);
		eva.onCreate(savedInstanceState);
		
		speechSearch = new EvaSpeechRecogComponent(eva);
		isPaused = false;
		// setup the Chat View
		if (savedInstanceState == null) {
			mView = new EvatureMainView(this, null);

			String greeting = getResources().getString(R.string.evature_greeting);
			int pos = greeting.length();
			String seeExamples = "\nTap here to see some examples.";
			SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
			int col = getResources().getColor(R.color.eva_chat_secondary_text);
			sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
			sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
			ChatItem chat = new ChatItem(sgreet,null, ChatItem.ChatType.EvaWelcome);
			mView.addChatItem(chat);
			speak(greeting, true, new Runnable() {
				
				@Override
				public void run() {
					mView.flashSearchButton(5);
					if (EvaAppSetup.AUTO_OPEN_MICROPHONE) {
						voiceRecognitionSearch(null, false);
					}
				}
			});
		}
		else {
			@SuppressWarnings("unchecked")
			ArrayList<ChatItem> chatItems = (ArrayList<ChatItem>) savedInstanceState.getSerializable(EVATURE_CHAT_LIST);
			
			mView = new EvatureMainView(this, chatItems );
			eva.setSessionId(savedInstanceState.getString(EVATURE_SESSION_ID));
			mView.flashSearchButton(5);
		}
		
		mView.setVolumeIcon();
	}
	

	private final SimpleDateFormat evaDateFormat = new SimpleDateFormat("yyyy-M-dd", Locale.US);



    private void findNumberOfResults(final EvaApiReply reply, final FlowElement flow, final ChatItem chatItem) {

        EvaLocation from = null;
        EvaLocation to = null;
        if (flow.Type ==  FlowElement.TypeEnum.Cruise) {
            if (flow.RelatedLocations.length < 2) {
                DLog.w(TAG, "Cruise search without two locations?");
                chatItem.setSearchModel(null);
                return;
            }
            from = flow.RelatedLocations[0];
            to = flow.RelatedLocations[1];
        }
        else {
            // alternative - for partial flow
            if (flow.Type ==  FlowElement.TypeEnum.Question) {
                QuestionElement qe = (QuestionElement)flow;
                if (qe.actionType ==  FlowElement.TypeEnum.Cruise) {
                    // cruises have (for now) only origin and destination
                    if (reply.locations.length > 0) {
                        from = reply.locations[0];
                    }
                    if (reply.locations.length > 1) {
                        to = reply.locations[1];
                    }
                }
            }
        }

        Date dateFrom=null, dateTo=null;
        Integer durationFrom=null, durationTo=null;

        String departure = (from != null && from.Departure != null) ?  from.Departure.Date : null;
        if (departure != null) {
            try {
                Date startDepDate = evaDateFormat.parse(departure);
                dateFrom = startDepDate; //icrDateFormat.format(startDepDate);

                Integer days = from.Departure.daysDelta();
                if (days != null) {
                    Calendar c = Calendar.getInstance();
                    c.setTime(startDepDate); // Now use today date.
                    c.add(Calendar.DATE, days.intValue()); // Adding duration days
                    dateTo = c.getTime();
                }
            } catch (ParseException e) {
                DLog.e(TAG, "Failed to parse eva departure date: "+departure);
                e.printStackTrace();
            }
        }

        if (to != null && to.Stay != null) {
            if (to.Stay.MinDelta != null && to.Stay.MaxDelta != null) {
                durationFrom = EvaTime.daysDelta(to.Stay.MinDelta);
                durationTo = EvaTime.daysDelta(to.Stay.MaxDelta);
            }
            else {
                durationFrom = to.Stay.daysDelta();
                durationTo = durationFrom;
            }
        }


        if (from != null && from.nearestCustomerLocation != null) {
            from = from.nearestCustomerLocation;
        }
        if (to != null && to.nearestCustomerLocation != null) {
            to = to.nearestCustomerLocation;
        }

        final EvaLocation fTo = to;
        final EvaLocation fFrom = from;
        final Date fDateFrom = dateFrom;
        final Date fDateTo = dateTo;
        final Integer fDurationFrom = durationFrom;
        final Integer fDurationTo = durationTo;
        final Context context = this;
        RequestAttributes.SortEnum sortBy = null;
        if (reply.requestAttributes != null) {
            sortBy = reply.requestAttributes.sortBy;
        }

        chatItem.setStatus(ChatItem.Status.SEARCHING);
        chatItem.setSubLabel("Searching...");
        chatItem.setSearchModel(new EvaAppSearchModel(from, to, dateFrom, dateTo, durationFrom, durationTo, reply.cruiseAttributes, sortBy ));
        mView.notifyDataChanged();

        EvaAppSetup.evaAppHandler.getCruiseCount(context, from, to, dateFrom, dateTo, durationFrom, durationTo, reply.cruiseAttributes, new EvaAppInterface.AsyncCountResult() {
            @Override
            public void handleCountResult(final int count) {
                if (count < 0) {
                    DLog.w(TAG, "No count response from icruise");
                    chatItem.setSearchModel(null); // don't allow tapping to see empty search results
                    chatItem.setSubLabel(null);
                    chatItem.setStatus(ChatItem.Status.NONE);
                    mView.notifyDataChanged();
                }
                else {
                    Thread t= new Thread(new Runnable() {
                        @Override
                        public void run() {
                            DLog.d(TAG, "Count result: "+count);
                            if (count == 0) {
                                boolean alreadySpoken = pendingReplySayit.cancel == true;

                                // attempt to cancel the pending sayit - no need to ask question or say the cruise if there are no results
                                pendingReplySayit.cancel = true;
                                synchronized (pendingReplySayit) {
                                    pendingReplySayit.notifyAll();
                                }

                                // hide the searching sublabel
                                chatItem.setSubLabel(null);
                                chatItem.setStatus(ChatItem.Status.NONE);
                                String noCruisesStr = getString(R.string.no_cruises);
                                if (alreadySpoken) {
                                    ChatItem noCruises = new ChatItem(noCruisesStr, chatItem.getEvaReplyId(), ChatItem.ChatType.Eva);
                                    mView.addChatItem(noCruises);
                                }
                                else {
                                    chatItem.setChat(noCruisesStr);
                                    chatItem.setSearchModel(null); // don't allow tapping to see empty search results
                                    mView.notifyDataChanged();
                                }
                                speak(noCruisesStr, false);
                            }
                            else {
                                // there are results - can go ahead and say the pending sayIt
                                synchronized (pendingReplySayit) {
                                    pendingReplySayit.notifyAll();
                                }
                                if (count == 1) {
                                    chatItem.setSubLabel("One cruise found.\nTap here to see it.");
                                }
                                else {
                                    chatItem.setSubLabel(count+" cruises found.\nTap here to see them.");
                                }
                                chatItem.setStatus(ChatItem.Status.HAS_RESULTS);
                                if (flow.Type == FlowElement.TypeEnum.Cruise || count == 1) {
                                    // this is a final flow element, not a question, so trigger cruise search
                                    // alternatively, there is only one left - no need to ask more questions
                                    EvaAppSetup.evaAppHandler.handleCruiseSearch(context, fFrom, fTo, fDateFrom, fDateTo, fDurationFrom, fDurationTo, reply.cruiseAttributes, null);
                                }
                            }
                            mView.notifyDataChanged();
                        }
                    });
                    t.start();
                }
            }
        });


    }



    public void onSaveInstanceState(Bundle savedInstanceState) {
		DLog.d(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(savedInstanceState);
		
		mView.handleBackPressed(); // cancel edit chat item if one is being edited
		savedInstanceState.putSerializable(EVATURE_CHAT_LIST,  mView.getChatListModel());
		savedInstanceState.putString(EVATURE_SESSION_ID, eva.getSessionId());
	}

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        DLog.i(TAG, "onRestoreInstanceState");
        if (savedInstanceState != null) {
            ArrayList<ChatItem> chatItems = (ArrayList<ChatItem>) savedInstanceState.getSerializable(EVATURE_CHAT_LIST);

            mView = new EvatureMainView(this, chatItems );
            eva.setSessionId(savedInstanceState.getString(EVATURE_SESSION_ID));
            mView.flashSearchButton(5);
        }
    }

	
	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;
		Intent intent = getIntent();
		if ("com.google.android.gms.actions.SEARCH_ACTION".equals(intent.getAction())) {
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
			mView.addChatItem(new ChatItem("Eva Thinking...", null, ChatItem.ChatType.Eva));
			
			// clear the intent - it shouldn't run again resuming!
			onNewIntent(new Intent());
		}

		eva.onResume();
		VolumeUtil.register(this, this);
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
	    super.onNewIntent(intent);
	    // getIntent() should always return the most recent
	    setIntent(intent);
	}
	
	@Override
	protected void onPause() {
		DLog.i(TAG, "onPause");
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
		VolumeUtil.unregister(this);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		eva.onDestroy();
		if (speechSearch != null) {
			speechSearch.onDestroy();
		}
		super.onDestroy();
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
			mView.hideSpeechWave();
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
						Thread.sleep(120);
						toneGenerator.startTone(ToneGenerator.TONE_PROP_NACK, 25);
					} catch (InterruptedException e) {
					}
					finally {
						runOnUiThread(new Runnable() {
							public void run() {
								eva.speechResultError(message, cookie);
							}
						});
					}
				}
			});
			t.start();
		}

		@Override
		public void speechResultOK(final String evaJson, final Bundle debugData, final Object cookie) {
			DLog.d(TAG, "Speech recognition ok");
			mView.hideSpeechWave();
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25);
						Thread.sleep(120); // small delay
						toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 25);
					} catch (InterruptedException e) {
					}
					finally {
						runOnUiThread(new Runnable() {
							public void run() {
								eva.speechResultOK(evaJson, debugData, cookie);
							};
						});
					}
				}
			});
			t.start();
			
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

		VOICE_COOKIE.storeResultInItem = chatItem;
		VOICE_COOKIE.editLastUtterance = editLastUtterance;
		
		Thread t = new Thread(new Runnable() {
			
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
		        runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						DLog.i(TAG, "Starting recognizer");
						eva.stopSpeak();
						mView.startSpeechRecognition(mSpeechSearchListener, speechSearch, VOICE_COOKIE, editLastUtterance);				
						
					}
				});
			}
		});
		t.start();
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
//			Intent intent = new Intent(this, VolumeSettingsDialog.class);
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



	@Override 
	public void onBackPressed() {
	   DLog.d(TAG, "onBackPressed Called");

	   // cancel recording if during recording
	   if (speechSearch.isInSpeechRecognition()) {
		   DLog.i(TAG, "Canceling recording");
		   speechSearch.cancel();
		   mView.deactivateSearchButton();
		   mView.hideSpeechWave();
		   return;
	   }

		if (!mView.handleBackPressed()) {
			super.onBackPressed();
		}
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
		ChatItem myChat = new ChatItem(getString(R.string.evature_user_start_new));
		mView.addChatItem(myChat);
		String greeting = getString(R.string.evature_start_new_session_speak);
		int pos = greeting.length();
		String seeExamples = "\nTap here to see some examples.";
		SpannableString sgreet = new SpannableString(greeting + new SpannedString(seeExamples));
		int col = getResources().getColor(R.color.eva_chat_secondary_text);
		sgreet.setSpan(new ForegroundColorSpan(col), pos, pos+seeExamples.length(), 0);
		sgreet.setSpan( new StyleSpan(Typeface.ITALIC), pos, pos+seeExamples.length(), 0);
		ChatItem chat = new ChatItem(sgreet,null, ChatItem.ChatType.EvaWelcome);
		mView.addChatItem(chat);
		speak(greeting, true, new Runnable() {
			
			@Override
			public void run() {
				mView.flashSearchButton(3);
				if (EvaAppSetup.AUTO_OPEN_MICROPHONE) {
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
	public void onEventChatItemModified(ChatItem chatItem, SpannableString preEditChat, boolean startRecord, boolean editLastUtterance) {
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
		if (reply.processedText != null) {
			// reply of voice -  add a "Me" chat item for the input text
			chat = new SpannableString(reply.processedText);
			if (reply.evaWarnings.size() > 0) {
				for (EvaWarning warning: reply.evaWarnings) {
					if (warning.position == -1) {
						continue;
					}
					hasWarnings = true;
					if (warning.text.equals(reply.processedText)) {
						// for some odd reason the error span doesn't show when its the entire string, so until a fix is available skip it
						break;
					}
					//chat.setSpan( new ForegroundColorSpan(col), warning.position, warning.position+warning.text.length(), 0);
					chat.setSpan( new ErrorSpan(getResources()), warning.position, warning.position+warning.text.length(), 0);
				}
			}
			if (EvaAppSetup.SEMANTIC_HIGHLIGHTING && reply.parsedText != null) {
				try {
					if (EvaAppSetup.SEMANTIC_HIGHLIGHT_TIMES && reply.parsedText.times != null) {
						int col = getResources().getColor(R.color.times_markup);
						
						for (ParsedText.TimesMarkup time : reply.parsedText.times) {
							chat.setSpan( new ForegroundColorSpan(col), time.position, time.position+time.text.length(), 0);
						}
					}
					
					if (EvaAppSetup.SEMANTIC_HIGHLIGHT_LOCATIONS && reply.parsedText.locations != null) {
						int col = getResources().getColor(R.color.locations_markup);
						
						for (ParsedText.LocationMarkup location: reply.parsedText.locations) {
							chat.setSpan( new ForegroundColorSpan(col), location.position, location.position+location.text.length(), 0);
						}
					}
				}
				catch (IndexOutOfBoundsException e) {
					DLog.e(TAG, "Index out of bounds setting spans of chat ["+chat+"]", e);
				}
			}
		}
		
		mView.deactivateSearchButton();

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
			warnExplanation.setSubLabel(getString(R.string.undo_tutorial));
			mView.addChatItem(warnExplanation);
		}

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
				//SumitK-Comment - object question not used any where 
				// Iftah: I used this question element for multiple choice answers... not integrated yet :(
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
		final String sayIt = flow.getSayIt();
		if (sayIt != null && !"".equals(sayIt)) {
			// non-statement flow types are delayed until a search-count is returned or timeout
			if (flow.Type == FlowElement.TypeEnum.Cruise || flow.Type ==  FlowElement.TypeEnum.Question ) {
				pendingReplySayit.cancel = false;
				pendingReplySayit.chatItem = chatItem;
				pendingReplySayit.sayIt = sayIt;
				pendingReplySayit.chatText = chatItem.getChat();
				chatItem.setChat("");
				mView.notifyDataChanged();
				pendingReplySayit.isQuestion = flow.Type ==  FlowElement.TypeEnum.Question;
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							synchronized (pendingReplySayit) {
							    pendingReplySayit.wait(1200); // wait some - maybe no results will show and this will be canceled
							}
							if (pendingReplySayit.cancel == false) {
								pendingReplySayit.cancel = true;
								pendingReplySayit.chatItem.setChat(pendingReplySayit.chatText);
								mView.notifyDataChanged();
								if (pendingReplySayit.isQuestion) {
									speak(sayIt, false, new Runnable() {
										public void run() {
											DLog.d(TAG, "Question asked");
											mView.flashSearchButton(3);
											if (EvaAppSetup.AUTO_OPEN_MICROPHONE) {
												voiceRecognitionSearch(null, false);
											}
										}
									});
								}
								else {
									speak(sayIt, false);
								}
							}
						} catch (InterruptedException e) {
						}
					}
				});
				t.start();
			}
			else if (flow.Type ==  FlowElement.TypeEnum.Statement && ((StatementElement) flow).StatementType == StatementElement.StatementTypeEnum.Chat &&
					reply.chat != null && reply.chat.newSession) {
				speak(sayIt, false, new Runnable() {
					public void run() {
						DLog.d(TAG, "New session started");
						mView.flashSearchButton(3);
						if (EvaAppSetup.AUTO_OPEN_MICROPHONE) {
							voiceRecognitionSearch(null, false);
						}
					}
				});
			}
			else {
				speak(sayIt, false);
			}
		}

		switch (flow.Type) {
			case Reply:
				ReplyElement replyElement = (ReplyElement) flow;
				if (ServiceAttributes.CALL_SUPPORT.equals(replyElement.AttributeKey)) {
					// TODO: trigger call support
					Toast.makeText(this, "Phoning Call Support", Toast.LENGTH_LONG).show();
				}
				break;
			case Flight:
			case Car:
			case Hotel:
			case Explore:
			case Train:
			case Cruise:
				if (EvaAppSetup.ASYNC_COUNT) {
					findNumberOfResults(reply, flow, chatItem);
				}
				else {

				}
				break;

			case Question:
				if (EvaAppSetup.ASYNC_COUNT) {
					findNumberOfResults(reply, flow, chatItem);
				}
				break;
				
			case Statement:
				StatementElement se = (StatementElement) flow;
				switch (se.StatementType) {
					case Understanding:
					case Unknown_Expression:
					case Unsupported:
						mShownWarningsTutorial = true;
						chatItem.setSubLabel(getString(R.string.undo_tutorial));
						break;
				}
				break;
			
			default:
				DLog.w(TAG, "Unexpected flow type "+flow.Type);
				break;

		}
	}

	public void onEvaError(String message, EvaApiReply reply, boolean isServerError, Object cookie) {
		mView.flashBadSearchButton(2);
		
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
		}
		
		
	}

	@Override
	public void onVolumeChange() {
		mView.setVolumeIcon();
	}

}

