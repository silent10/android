package com.virtual_hotel_agent.search.views;


import java.lang.ref.WeakReference;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.evaapis.EvaException;
import com.evaapis.android.EvaSpeechRecogComponent;
import com.evaapis.android.EvaSpeechRecogComponent.SpeechRecognitionResultListener;
import com.evaapis.android.SoundLevelView;
import com.evaapis.android.SpeechAudioStreamer;
import com.evature.util.DLog;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.util.VolumeUtil;
import com.virtual_hotel_agent.search.views.fragments.ChatFragment;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;


/****
 *  User interface parts of the VHA
 */
public class MainView  {

	private static final String TAG = "MainView";
	private ImageButton mSearchButton;
	private SoundLevelView mSoundView;
	private ImageButton mUndoButton;
	private ImageButton mResetButton;
	private ImageButton mVolumeButton;
	private ProgressBar mProgressBar;
	private View mSearchButtonCont;
	
	private MainActivity mEvaActivity;
	
	private boolean mSideButtonsVisible;

	private ViewPager mViewPager;
	private MyPagerAdapter mPagerAdapter;
	private String mChatTabName;
	private String mHotelsTabName;

	private List<String> mTabTitles;
	private ListView mDrawerList;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;

	private HotelListFragment mHotelsListFragment;
	private ChatFragment mChatFragment;
	

	private WeakReference<Handler> mUpdateLevel;
	public static final long UPDATE_SOUND_VIEW_INTERVAL = 60; // update the soundView every such ms
	
	final static float INITIAL_SCALE_SIDE_BUTTON = 0.64f; // should be in sync with XML scaleX/Y of restart_button and undo_button

	
	enum SearchButtonIcon{
		MICROPHONE,
		FLAT,
		NONE
	};
	
	SearchButtonIcon mSearchButtonIcon = SearchButtonIcon.MICROPHONE;
	boolean pendingIconSwitch = false;
	

	public final int CHAT_TAB_INDEX = 0;
	public final int HOTEL_LIST_TAB_INDEX = 1;

	private boolean mainButtonsShown = true;

	
	@SuppressLint("NewApi")
	public MainView(final MainActivity mainActivity,  Bundle savedInstanceState,  List<String> tabTitles) {
		mEvaActivity = mainActivity;
		mSearchButton = (ImageButton) mainActivity.findViewById(R.id.voice_search_button);
		mSoundView = (SoundLevelView)mainActivity.findViewById(R.id.surfaceView_sound_wave);
		mUndoButton = (ImageButton)mainActivity.findViewById(R.id.undo_button);
		mResetButton = (ImageButton)mainActivity.findViewById(R.id.restart_button);
		mSearchButtonCont = mainActivity.findViewById(R.id.voice_search_container);
		mProgressBar = (ProgressBar) mainActivity.findViewById(R.id.progressBar1);
		
		//mVolumeButton = (ImageButton) mainActivity.findViewById(R.id.volume_button);
		
		if (savedInstanceState == null) {
			mChatFragment = new ChatFragment();
		}
		
		mSoundView.setColor(0xffffffff);
		mSoundView.setAlign(Gravity.RIGHT);
		
		setupSearchButtonDrag();
		
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
		supportActionBar.setHomeButtonEnabled(true);

		mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, android.graphics.PorterDuff.Mode.SRC_ATOP);
		
		final String[] drawerItems = {
				"Chat with Virtual Agent",
				"Search Results",
				"My Bookings",
				"-",
				//mainActivity.getString(R.string.tutorial),
				mainActivity.getString(R.string.faq),
				mainActivity.getString(R.string.settings),
				mainActivity.getString(R.string.report_a_bug),
				mainActivity.getString(R.string.about)
		};
        mDrawerList = (ListView) mainActivity.findViewById(R.id.left_drawer);

        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(mainActivity,
                R.layout.drawer_list_item, drawerItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new ListView.OnItemClickListener() {

    		@Override
    		public void onItemClick(android.widget.AdapterView<?> parent,
    				View view, int position, long id) {
    			mainActivity.selectDrawerItem(position, drawerItems[position]);
    		}
        });
        
        
		mDrawerLayout = (DrawerLayout) mainActivity.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
        		mainActivity,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {
        	
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                //mainActivity.getActionBar().setTitle(mTitle);
                mainActivity.invalidateOptionsMenu();
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //mainActivity.getActionBar().setTitle(mDrawerTitle);
                mainActivity.invalidateOptionsMenu();
            }
        };
        
        mDrawerToggle.setDrawerIndicatorEnabled(true);

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);


		//---------------
        // setup the tab switching
		mPagerAdapter = new MyPagerAdapter(mainActivity.getFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		
		if (savedInstanceState == null) {
			mChatFragment = new ChatFragment();
		}
		
		mSoundView.setColor(0xffffffff);
		mSoundView.setAlign(Gravity.CENTER);

		// TODO: change toolbar based on 
		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
	
			@Override
			public void onPageSelected(int position) {
				//supportActionBar.setDisplayHomeAsUpEnabled(position > 0);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
//		mTabs.setCurrentItem(mTabTitles.indexOf(mChatTabName));
	}
	

	private static void scaleButton(final View button, int duration, float fromScale, float toScale) {
		button.setVisibility(View.VISIBLE);
		// no idea why the 1.5 factor is needed, but without it the size is smaller than when using setScale!
		ScaleAnimation anim = new ScaleAnimation(fromScale, 1.5f*toScale, fromScale, 1.5f*toScale, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f );
		anim.setDuration(duration);
		anim.setFillAfter(true);
		anim.setFillBefore(true);
		if (toScale > 0) {
			anim.setInterpolator(new OvershootInterpolator());
			button.setEnabled(true);
		}
		else {
			button.setEnabled(false);
			anim.setAnimationListener(new Animation.AnimationListener() {
				@Override public void onAnimationStart(Animation animation) {}
				@Override public void onAnimationRepeat(Animation animation) {}
				@Override
				public void onAnimationEnd(Animation animation) {
			        button.setVisibility(View.INVISIBLE);
				}
			});
		}
		button.startAnimation(anim);
	}
	
	@SuppressLint("NewApi")
	private static void animateButton(final View button, String animProperty, int duration, float from, float to) {
		// TODO: use http://nineoldandroids.com/ (for compatibility with older devices)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			button.setVisibility(View.VISIBLE);
			Animator propAnim = ObjectAnimator.ofFloat(button, animProperty, from, to);
			propAnim.setInterpolator(new OvershootInterpolator());
			propAnim.setDuration(duration);
			propAnim.start();
			
		}
	}

	
	@SuppressLint("NewApi")
	private void toggleSideButtons(boolean show) {
		if (mSideButtonsVisible == show) {
			return;
		}
		mSideButtonsVisible = show;
		// use animation to show/hide buttons
		int animDuration = 400;
		if (show) {
			// turn search button to shadow
			showMicButton(SearchButtonIcon.FLAT);
			animateButton(mSearchButton, "alpha", animDuration/2, 1.0f, 0.7f);
			
			// show undo/reset
			scaleButton(mUndoButton, animDuration, 0f, INITIAL_SCALE_SIDE_BUTTON);
			scaleButton(mResetButton, animDuration, 0f, INITIAL_SCALE_SIDE_BUTTON);
		}
		else {
			// show search button
			showMicButton(SearchButtonIcon.MICROPHONE);
			animateButton(mSearchButton, "alpha", animDuration, 0.7f, 1.0f);
			animateButton(mSearchButtonCont, "translationX", animDuration, mSearchButtonCont.getTranslationX(), 0f);
			
			
			// hide undo/reset button
			scaleButton(mUndoButton, animDuration/2, INITIAL_SCALE_SIDE_BUTTON, 0f);
			scaleButton(mResetButton, animDuration/2, INITIAL_SCALE_SIDE_BUTTON, 0f);
		}
	}
	
	private void setupSearchButtonDrag() {
		Resources r = mEvaActivity.getResources();
		final int margin = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
		
		mSearchButton.setOnTouchListener(new View.OnTouchListener() {
			boolean hoveringReset = false;
			boolean hoveringUndo = false;
			
			@SuppressLint("NewApi")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					toggleSideButtons(false);
					if (event.getEventTime() - event.getDownTime() <= 500) {
						float x = event.getRawX() - mSearchButtonCont.getLeft();
				        float y = event.getRawY() - mSearchButtonCont.getTop();
				        if (y > mSearchButton.getTop()- margin && 
				        		x < mSearchButton.getRight() + margin && x > mSearchButton.getLeft() - margin ) {
				        	// mSearchButtonCont.performClick(); for some reason this doesn't work when the searchButton is embedded inside FrameLayout
				        	mEvaActivity.buttonClickHandler(mSearchButton);
				        }
					}
					if (hoveringUndo) {
						mUndoButton.performClick();
						mUndoButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
					}
					if (hoveringReset) {
						mResetButton.performClick();
						mResetButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
					}
					hoveringUndo = hoveringReset = false;
					break;
					
				case MotionEvent.ACTION_DOWN:
					hoveringUndo = false;
					hoveringReset = false;
					mSideButtonsVisible = false;
					break;
				
				case MotionEvent.ACTION_MOVE:
					float x = event.getRawX();
			        float y = event.getRawY();
			        // moved up, or recording started
			        if (y < mSearchButtonCont.getTop()+ mSearchButton.getTop()- margin
			        		||  isRecording()) {
			        	toggleSideButtons(false);
						hoveringUndo = false;
						hoveringReset = false;
			        	break;
			        }
			        
			        int searchRight = mSearchButtonCont.getLeft() + mSearchButton.getRight();
			        int searchLeft = mSearchButtonCont.getLeft() + mSearchButton.getLeft();
			        
					if (mSideButtonsVisible == false && isRecording() == false) {
						// show side buttons if long-press of beginning drag
						if (event.getEventTime() - event.getDownTime() > 500) {  
							mSearchButton.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
						}
						if ((event.getEventTime() - event.getDownTime() > 500) ||
								x > searchRight + margin || 
								x < searchLeft - margin ) {
							toggleSideButtons(true);
//							// show drag shadow - if honeycomb+
//							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//								if (mSearchButtonShadow == null) {
//									mSearchButtonShadow = new View.DragShadowBuilder();
//								}
//								mSearchButton.startDrag(ClipData.newPlainText("Nada",  "Nothing"), (View.DragShadowBuilder)mSearchButtonShadow, null, 0);
//							}
						}
					}
					if (mSideButtonsVisible) {
						int searchCenter = (searchRight+searchLeft)/2;
						float delta = x - searchCenter;
					
						if (x > searchCenter+10) {
							int resetCenter = (mResetButton.getRight()+ mResetButton.getLeft())/2;
							delta = Math.min(delta, resetCenter - searchCenter);
							// linearly scale button up based on distance
							float fraction =  Math.min(1f, (x - searchRight) / Math.max(1f, (resetCenter - searchRight)));
							
							// TODO: use com.nineoldandroids.view.ViewHelper for older devices
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								float scale = INITIAL_SCALE_SIDE_BUTTON + (1f-INITIAL_SCALE_SIDE_BUTTON)*fraction;
								mResetButton.clearAnimation();
								mResetButton.setScaleX(scale);
								mResetButton.setScaleY(scale);
							}
							hoveringReset = fraction > 0.7;
						}
						else if (x < searchCenter-10) {
							int undoCenter = (mUndoButton.getRight()+mUndoButton.getLeft())/2;
							delta = Math.max(delta, undoCenter - searchCenter);
							// linearly scale button up based on distance
							float fraction =  Math.min(1f, (searchLeft - x) / Math.max(1f, (searchLeft - undoCenter)));
							
							if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
								float scale = INITIAL_SCALE_SIDE_BUTTON + (1f-INITIAL_SCALE_SIDE_BUTTON)*fraction;
								mUndoButton.clearAnimation();
								mUndoButton.setScaleX(scale);
								mUndoButton.setScaleY(scale);
							}
							hoveringUndo = fraction > 0.7;
						}

						mSearchButtonCont.setTranslationX(delta);
						
					}
					break;
				}
				
				return true;
			}
		});
		
	}
	
	private boolean isRecording() {
		if (mUpdateLevel != null && mUpdateLevel.get() != null) {
			SearchHandler  handler = (SearchHandler)mUpdateLevel.get();
			if (handler != null && handler.isRecording()) {
				return true;
			}
		}
		return false;
	}

	/**********  UI of Recording button state **************/
	
	@SuppressLint("NewApi")
	private void showMicButton(SearchButtonIcon what) {
		if (mSearchButtonIcon == what) {
			return;
		}
		if (what == SearchButtonIcon.FLAT) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mSearchButton.clearAnimation();
				AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) mSearchButton.getResources().getDrawable(R.drawable.animated_microphone);
				mSearchButton.setImageDrawable(animatedDrawable);
				animatedDrawable.start();

				// hide the flat microphone when the animation completes
				mSearchButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						if (mSearchButtonIcon == SearchButtonIcon.FLAT) {
							mSearchButton.setImageDrawable(null);
						}
					}
				}, 450); // should be in sync with the animation duration defined in the xml
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mSearchButton.clearAnimation();
				ValueAnimator animator = ValueAnimator.ofInt(mSearchButton.getPaddingTop(), mSearchButton.getHeight()/2);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				    @Override
				    public void onAnimationUpdate(ValueAnimator valueAnimator){
				    	int val =  ((Integer) valueAnimator.getAnimatedValue()).intValue();
				        mSearchButton.setPadding(mSearchButton.getPaddingLeft(), 
				        		val, mSearchButton.getPaddingRight(), val);
				    }
				});
				animator.setDuration(450);
				animator.start();
			}
			else {
				mSearchButton.setImageDrawable(null);
			}
		}
		else if (what == SearchButtonIcon.MICROPHONE) {
			if (isRecording()) {
				DLog.d(TAG, "Not setting to microphone icon because in recording");
				return; // don't change back to icon if recording
			}
//			if (mProgressBar.getVisibility() == View.VISIBLE) {
//				// wait with icon switch until progressbar is hidden
//				pendingIconSwitch = true;
//				return;
//			}
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mSearchButton.clearAnimation();
				AnimatedVectorDrawable animatedDrawable = (AnimatedVectorDrawable) mSearchButton.getResources().getDrawable(R.drawable.animated_microphone_reverse);
				mSearchButton.setImageDrawable(animatedDrawable);
				animatedDrawable.start();
			}
			else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				mSearchButton.clearAnimation();
				mSearchButton.setImageResource(R.drawable.evature_microphone_icon);
				ValueAnimator animator = ValueAnimator.ofInt(mSearchButton.getHeight()/2, 0);
				animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				    @Override
				    public void onAnimationUpdate(ValueAnimator valueAnimator){
				    	int val =  ((Integer) valueAnimator.getAnimatedValue()).intValue();
				        mSearchButton.setPadding(mSearchButton.getPaddingLeft(), 
				        		val, mSearchButton.getPaddingRight(), val);
				    }
				});
				animator.setDuration(450);
				animator.start();
			}
			else {
				mSearchButton.setImageResource(R.drawable.evature_microphone_icon);
			}
		}
		else {
			mSearchButton.setImageDrawable(null);
		}
		mSearchButtonIcon = what;
	}

	
	public void activateSearchButton() {
		DLog.d(TAG, "activate search button");
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
		    	//to show the recording is active
		    	mSearchButton.setEnabled(true);
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.startTransition(100);
				showMicButton(SearchButtonIcon.FLAT);
		    }
		});
	}
	
	@SuppressLint("NewApi")
	public void flashSearchButton(final int times) {
		// flash is used to draw user attention to button
		
		if (isRecording() || mSideButtonsVisible) {
			return; // do not flash button while recording, or while user is choosing side button
		}
		if (times == 0) {
			return;
		}
		
		if (times == -1) {
			DLog.d(TAG, "flash search button");
		}
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
				if (times > 0) {
					showMicButton(SearchButtonIcon.MICROPHONE);
					mSearchButton.setEnabled(true);
					bounceView(times, mSearchButtonCont);
				}
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				final int duration = 150;

				drawable.startTransition(duration);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
				      // reverse the transition after it completes
				    	mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				    	TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				    	drawable.reverseTransition(duration);
				    	mSearchButton.postDelayed(new Runnable() {
						    @Override
						    public void run() {
						    	flashSearchButton(-(Math.abs(times)-1));
						    }
				    	}, duration);
				    }
				}, duration);
		    }
		});
	}

	public static void bounceView(final int height, View view) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			bounceView(height, view, "translationY", -30);
		}
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//			bounceView(height, view, "translationZ", 5);
//		}
	}

	@SuppressLint("NewApi")
	private static void bounceView(final int height, View view, String property, int heightFactor) {
		ObjectAnimator anim1 = ObjectAnimator.ofFloat(view, property, 0, heightFactor*height);
		final ObjectAnimator anim2 = ObjectAnimator.ofFloat(view, property, heightFactor*height, 0);
		anim1.setDuration(70*height);
		anim2.setDuration(300*height);
		anim1.setInterpolator(new DecelerateInterpolator());
		anim2.setInterpolator(new BounceInterpolator());
		anim1.addListener(new AnimatorListenerAdapter() {
		    @Override
		    public void onAnimationEnd(Animator animation) {
		        super.onAnimationEnd(animation);
		        anim2.start();
		    }
		});
		anim1.start();
	}
	
	public void disableSearchButton() {
		DLog.d(TAG, "disable search button");
		mSearchButton.post(new Runnable() {
			@Override
			public void run() {
				mSearchButton.setEnabled(false);
				showMicButton(SearchButtonIcon.NONE);
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.reverseTransition(50);
				mSearchButton.postDelayed(new Runnable() {
					@Override
					public void run() {
						mSearchButton.setBackgroundResource(R.drawable.transition_button_dectivate);
						TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
						drawable.startTransition(50);
					}
				}, 60);
			}
		});
	}
	
	// return to normal button view
	public void deactivateSearchButton() {
		DLog.d(TAG, "deactivate search button");
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
		    	mSearchButton.setEnabled(true);
		    	showMicButton(SearchButtonIcon.MICROPHONE);
				mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.reverseTransition(100);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
				    	mSearchButton.setBackgroundResource(R.drawable.transition_button_activate);
				    	TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				    	drawable.resetTransition();
				    }
				}, 110);
		    }
		});
	}
	
	
	@SuppressLint("NewApi")
	public void flashBadSearchButton(final int times) {
		if (isRecording() || mSideButtonsVisible) {
			return;
		}
		
		if (times == 0) {
			return;
		}
		
		if (times == -1) {
			DLog.d(TAG, "flash bad search button");
		}
				
		mSearchButton.post(new Runnable() {
		    @Override
		    public void run() {
		    	if (times > 0) {
		    		showMicButton(SearchButtonIcon.MICROPHONE);
		    		mSearchButton.setEnabled(true);
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) { 
						ObjectAnimator anim = ObjectAnimator.ofFloat(mSearchButtonCont, "translationX", 50, -50f);
						anim.setDuration(110);
						anim.setRepeatCount(times);
						anim.setRepeatMode(ValueAnimator.REVERSE);
						anim.setInterpolator(new AccelerateDecelerateInterpolator());
						anim.addListener(new AnimatorListenerAdapter() {
						    @Override
						    public void onAnimationEnd(Animator animation) {
						        super.onAnimationEnd(animation);
						        mSearchButtonCont.setTranslationX(0);
						    }
						});
						anim.start();
					}
				}
		    	
				mSearchButton.setBackgroundResource(R.drawable.transition_button_bad);
				TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
				drawable.startTransition(100);
				mSearchButton.postDelayed(new Runnable() {
				    @Override
				    public void run() {
						mSearchButton.setBackgroundResource(R.drawable.transition_button_bad);
						TransitionDrawable drawable = (TransitionDrawable) mSearchButton.getBackground();
						drawable.reverseTransition(150);
						// repeat
						mSearchButton.postDelayed(new Runnable() { 
							public void run() {		flashBadSearchButton(-(Math.abs(times)-1)); }
						}, 110);
				    }
				}, 110);
		    }
		});
	}
	
//	public void setSearchButtonBg(int color) {
//		mSearchButton.setBackgroundColor(color);
//	}

	public boolean areMainButtonsShown() {
		return mainButtonsShown;
	}

	// used to hide microphone when editing existing chatItem
	public void toggleMainButtons(boolean showMainButtons) {
		mainButtonsShown = showMainButtons;
		DLog.d(TAG, "Setting main button to "+showMainButtons);
		mSearchButtonCont.setVisibility(showMainButtons ? View.VISIBLE : View.GONE);
	}
	

	
	public void showStatus(String text) {
		//mStatusText.setText(text);
		mProgressBar.setVisibility(View.VISIBLE);
		//mStatusPanel.setVisibility(View.VISIBLE);
	}
	public void hideStatus() {
		//mStatusText.setText("");
		mProgressBar.setVisibility(View.GONE);
		//mStatusPanel.setVisibility(View.GONE);
		if (pendingIconSwitch) {
			showMicButton(SearchButtonIcon.MICROPHONE);
			pendingIconSwitch = false;
		}
	}
	
	
	public void hideSpeechWave() {
		if (mUpdateLevel != null) {
			Handler handler = mUpdateLevel.get();
			if (handler != null)
				handler.removeMessages(0);
			mUpdateLevel = null;
		}
		mSoundView.setVisibility(View.GONE);
	}

	
	/***********  Update View based on Recording Volume **************/

	static class SearchHandler extends Handler {
		private boolean processing = false;
		private EvaSpeechRecogComponent speechSearch;
		private MainView view;
		
		public SearchHandler(EvaSpeechRecogComponent speechSearch, MainView view) {
			this.speechSearch = speechSearch;
			this.view = view;
		}
		
		public boolean isRecording() {
			return speechSearch.getSpeechAudioStreamer().getIsRecording();
		}
		
		@Override
		public void handleMessage(Message msg) {
			SpeechAudioStreamer  speechAudioStreamer = speechSearch.getSpeechAudioStreamer();

			if (speechAudioStreamer.getIsRecording()) {
				view.mSoundView.setSoundData(
						speechAudioStreamer.getSoundLevelBuffer(), 
						speechAudioStreamer.getBufferIndex()
				);
				//view.mSoundView.stopSpringAnimation();
				if (view.mSoundView.getVisibility() != View.VISIBLE)
					view.mSoundView.setVisibility(View.VISIBLE);
				
			}
			else {
				// continue sending data to soundView - but fake it as a zero volume sound
				speechAudioStreamer.addVolumeSample(speechAudioStreamer.getMinSoundLevel());
				view.mSoundView.setSoundData(
						speechAudioStreamer.getSoundLevelBuffer(), 
						speechAudioStreamer.getBufferIndex()
				);

				
				if (speechAudioStreamer.wasNoise && !processing) {
					processing = true;
					view.disableSearchButton();
					//view.showStatus("Processing...");
				}
			}
			
			sendEmptyMessageDelayed(0, UPDATE_SOUND_VIEW_INTERVAL);
			super.handleMessage(msg);
		}
	};

	
	public void startSpeechRecognition(final SpeechRecognitionResultListener listener, final EvaSpeechRecogComponent speechSearch, Object cookie, boolean editLastUtterance) {
		//showStatus("Listening...");
		
		mUpdateLevel = new WeakReference<Handler>(new SearchHandler(speechSearch, this));
		try {
			Handler handler = mUpdateLevel.get();
			if (handler != null) {
				speechSearch.startRecognizer(listener, cookie, editLastUtterance);
				handler.sendEmptyMessage(0);
			}
			else {
				throw new EvaException("updateVolume Level is null");
			}
		}
		catch (EvaException e) {
			DLog.e(TAG, "Exception starting recorder", e);
		}
		
		activateSearchButton();
	}
	

	public void setVolumeIcon() {
		if (mVolumeButton == null) {
			return;
		}
		VolumeUtil.checkVolume(this.mEvaActivity);
		if (VolumeUtil.isLowVolume()) {
			mVolumeButton.setVisibility(View.VISIBLE);
			mVolumeButton.setImageResource(VolumeUtil.getVolumeIcon());
		}
		else {
			mVolumeButton.setVisibility(View.GONE);
		}
	}


	//  Tabs handling

	public String getChatTabName()    {  return mChatTabName; 	}
	public String getHotelsListTabName()  {  return mHotelsTabName; }

	public ChatFragment 		getChatFragment()    {  return mChatFragment; 	}
	public HotelListFragment 	getHotelsListFragment()  {  return mHotelsListFragment; }

	

	public class MyPagerAdapter extends FragmentPagerAdapter /*implements ViewPager.OnPageChangeListener */ {
		
		private final String TAG = "MyPagerAdapter";
		
		public MyPagerAdapter(FragmentManager fm) {
			super(fm);
			DLog.i(TAG, "CTOR");
		}
//		
		@Override 
		public void destroyItem(android.view.ViewGroup container, int position, Object object) {
			if (position >= getCount()) {
				DLog.d(TAG, "Destryoing tab at position "+position);
		        FragmentManager manager = ((Fragment) object).getFragmentManager();
		        FragmentTransaction trans = manager.beginTransaction();
		        trans.remove((Fragment) object);
		        trans.commit();
		    }
			else {
				DLog.d(TAG, "Ignoring destroyItem at position "+position);
			}
		};
		
		@Override public Object instantiateItem (ViewGroup container, int position) {
			DLog.d(TAG, "instantiateItem "+position);
			Object result = super.instantiateItem(container, position);
			
			// hack to restore pointer to fragments:
			if (position == CHAT_TAB_INDEX) {
				mChatFragment = (ChatFragment)result;
			}
			else if (position == HOTEL_LIST_TAB_INDEX) {
				mHotelsListFragment = (HotelListFragment) result;
			}
			return result;
		}
		
//	    @Override
	    public int getItemPosition(Object object){
	    	if (object == mChatFragment) {
	    		return POSITION_UNCHANGED;
	    	}
	        return POSITION_NONE;
	    }

	    @Override
		public Fragment getItem(int position) {// Asks for the main fragment
			DLog.d(TAG, "getItem " + String.valueOf(position));
			int size = mTabTitles.size();
			if (position >= size) {
				DLog.e(TAG, "No fragment made for Position "+position);
				return null;
			}
			String tabTitle = mTabTitles.get(position);
			if (tabTitle.equals(mChatTabName)) { // Main Chat window
				DLog.d(TAG, "Chat Fragment");
				if (mChatFragment == null) {
					mChatFragment = new ChatFragment();
				}
				return mChatFragment;
			}
			else if (tabTitle.equals(mHotelsTabName)) { // Hotel list window
				DLog.i(TAG, "Hotels Fragment");
				if (mHotelsListFragment == null) {
					mHotelsListFragment = new HotelListFragment();
				}
				return mHotelsListFragment;
			}

			DLog.e(TAG, "No fragment made for Position "+position+(position< size ? " titled "+tabTitle : ""));
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
		DLog.d(TAG, "showTab "+position);
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
		DLog.d(TAG, "addTab "+name);
		mTabTitles.add(name);
		mPagerAdapter.notifyDataSetChanged();
	}
	
	public void addTab(String name, int position) { // Dynamic tabs add to certain position
		DLog.d(TAG, "addTab "+name);
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
		DLog.d(TAG, "removeTab "+tabIndex);
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
//		if (buttons)
//			mBottomBar.startAnimation(anim);
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
//		if (buttons)
//			mBottomBar.startAnimation(anim);
	}

	public void onPostCreate() {
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
	}

	public void onConfigurationChanged(Configuration newConfig) {
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public boolean isDrawerSelected(MenuItem item) {
		// return true if DrawerToggle handled this menu option click
		return mDrawerToggle.onOptionsItemSelected(item);
	}

	public void closeDrawer() {
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	public boolean isDrawerOpen() {
		return mDrawerLayout.isDrawerOpen(mDrawerList);
	}

}
