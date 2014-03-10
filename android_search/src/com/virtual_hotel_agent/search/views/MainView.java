package com.virtual_hotel_agent.search.views;

import java.lang.ref.WeakReference;

import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
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
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;

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
	private final int search_button_padding = 24;

	private WeakReference<Handler> mUpdateLevel;
	
	private MainActivity mainActivity;

	
	public MainView(MainActivity mainActivity) {
		this.mainActivity = mainActivity;
		mStatusPanel = mainActivity.findViewById(R.id.status_panel);
		mStatusText = (TextView)mainActivity.findViewById(R.id.text_listeningStatus);
		mProgressBar = (ProgressBar)mainActivity.findViewById(R.id.progressBar1);
		mSoundView = (SoundLevelView)mainActivity.findViewById(R.id.surfaceView_sound_wave);
		mSearchButton = (ImageButton) mainActivity.findViewById(R.id.search_button);

		
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
		Handler h = mUpdateLevel.get();
		if (h != null)
			h.removeMessages(0);
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
			mainActivity.eva.speechResultError(message, cookie);
			flashBadSearchButton(2);
			Tracker defaultTracker = GoogleAnalytics.getInstance(mainActivity).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createEvent("speech_search", "speech_search_end_bad", message, 0l)
					    .build()
					   );
		}

		@Override
		public void speechResultOK(String evaJson, Bundle debugData, Object cookie) {
			finishSpeech();
			mainActivity.eva.speechResultOK(evaJson, debugData, cookie);
		}
	};
	
	public void startSpeechSearch(final EvaSpeechComponent speechSearch) {
		showStatus("Listening...");
		
		activateSearchButton();
		//view.setBackgroundResource(R.drawable.custom_button_active);
		mUpdateLevel = new WeakReference<Handler>(new SearchHandler(speechSearch, this));
		
		try {
			Handler handler = mUpdateLevel.get();
			if (handler != null) {
				speechSearch.start(mSpeechSearchListener, "voice");
				handler.sendEmptyMessageDelayed(0, 50);
			}
			else {
				throw new EvaException("updateVolume Level is null");
			}
		}
		catch (EvaException e) {
			Toast.makeText(mainActivity, "Failed to start recorder, please try again later and contact the developers if the problem persists", Toast.LENGTH_LONG).show();
			MainActivity.LogError(TAG, "Exception starting recorder", e);
		}
	}
		
	
}
