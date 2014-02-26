package com.virtual_hotel_agent.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.evature.util.Log;
import com.viewpagerindicator.LinePageIndicator;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelDetails.HotelImage;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.util.SystemUiHider;
import com.virtual_hotel_agent.search.views.adapters.BitmapAdapter;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 * @param <T>
 * 
 * @see SystemUiHider
 */
public class ImageGalleryActivity extends Activity implements OnPageChangeListener {
	/**
	 * Whether or not the system UI should be auto-hidden after
	 * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
	 */
	private static final boolean AUTO_HIDE = true;

	/**
	 * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
	 * user interaction before hiding the system UI.
	 */
	private static final int AUTO_HIDE_DELAY_MILLIS = 10000;

	/**
	 * If set, will toggle the system UI visibility upon interaction. Otherwise,
	 * will show the system UI visibility upon interaction.
	 */
	private static final boolean TOGGLE_ON_CLICK = true;

	/**
	 * The flags to pass to {@link SystemUiHider#getInstance}.
	 */
	private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

	private static final String TAG = "ImageGalleryActivity";

	public static final String PHOTO_INDEX = "PhotoIndex";
	public static final String HOTEL_ID = "HotelId";

	/**
	 * The instance of the {@link SystemUiHider} for this activity.
	 */
	private SystemUiHider mSystemUiHider;

	private int initialPage = 99999;
	private ImageDownloader imageDownloader;
	private BitmapAdapter adapter;
	private LinePageIndicator mIndicator;

	static class DownloadedImg extends Handler {
		private WeakReference<ImageGalleryActivity> activityRef;

		public DownloadedImg(WeakReference<ImageGalleryActivity> activity) {
			this.activityRef = activity;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (activityRef != null) {
				ImageGalleryActivity iga = activityRef.get();
				if (iga != null) {
					iga.adapter.addBitmap((Bitmap) msg.obj);
					// downloaded the right bitmap - switch to page chosen in intent
					if (iga.contentView != null && iga.initialPage == iga.adapter.getCount()-1) {
						iga.contentView.setCurrentItem(iga.initialPage);
					}
				}
			}
			super.handleMessage(msg);
		}
	}
	
	private Handler mHandlerImgDownloaded;
	private Button mButton;
	
	static class AllDownloaded extends Handler {
		private WeakReference<ImageGalleryActivity> activityRef;

		public AllDownloaded(WeakReference<ImageGalleryActivity> activity) {
			this.activityRef = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			if (activityRef != null) {
				ImageGalleryActivity iga = activityRef.get();
				if (iga != null) {
					iga.mIndicator.setViewPager(iga.contentView);
					iga.mIndicator.notifyDataSetChanged();
				}
			}
			super.handleMessage(msg);
		}
	}
	private AllDownloaded mHandlerAllDone;

	private ArrayList<String> captions;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_gallery);


		Intent intent = getIntent();
		int hotelId = intent.getIntExtra(HOTEL_ID, -1);
		if (hotelId == -1) {
			Log.e(TAG, "No hotel ID");
			this.finish();
			return;
		}

		XpediaDatabase db = MyApplication.getDb();
		HotelData hotelData = null;
		if (db != null && db.mHotelData != null && db.mHotelData.length > hotelId) {
			hotelData = db.mHotelData[hotelId];
		}
		else {
			Log.e(TAG, "No DB");
			this.finish();
			return;
		}

		WeakReference<ImageGalleryActivity> _this = new WeakReference<ImageGalleryActivity>(this);
		mHandlerAllDone = new AllDownloaded(_this);
		mHandlerImgDownloaded = new DownloadedImg(_this);

		final View controlsView = findViewById(R.id.fullscreen_content_controls);
		contentView = (ViewPager) findViewById(R.id.fullscreen_content);
		captionView = (TextView) findViewById(R.id.photo_caption);

		adapter = new BitmapAdapter(this);
		contentView.setAdapter(adapter);
		
		mIndicator = (LinePageIndicator)findViewById(R.id.indicator);
		
		imageDownloader = new ImageDownloader(db.getImagesCache(), mHandlerImgDownloaded, mHandlerAllDone);
		
		captions = new ArrayList<String>();
		ArrayList<String> urls = new ArrayList<String>();
		if (hotelData != null && hotelData.mDetails != null && hotelData.mDetails.hotelImages != null) {
			for (HotelImage hotelImage : hotelData.mDetails.hotelImages) {
				if (hotelImage.url != null) {
					urls.add(hotelImage.url);
					if (hotelImage.caption != null) {
						captions.add(hotelImage.caption);
					}
					else {
						captions.add(hotelImage.name);
					}
				}
			}
		}

		if (urls.size() == 0) {
			Log.e(TAG, "No images");
			this.finish();
			return;
		}
		initialPage = intent.getIntExtra(PHOTO_INDEX, 99999);
		if (initialPage != 99999) {
			setCaption(initialPage);
		}
		
		imageDownloader.startDownload(urls);
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		mIndicator.setLineWidth(width / urls.size());
		mIndicator.setGapWidth(0);
		
		mIndicator.setOnPageChangeListener(this);
		
		Log.i(TAG, "Showing "+urls.size()+" imgs for hotel "+hotelId+"  jumping to img: "+initialPage);
		
		// Set up an instance of SystemUiHider to control the system UI for
		// this activity.
		mSystemUiHider = SystemUiHider.getInstance(this, contentView,
				HIDER_FLAGS);
		mSystemUiHider.setup();
		mSystemUiHider
				.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
					// Cached values.
					int mControlsHeight;
					int mShortAnimTime;

					@Override
					@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
					public void onVisibilityChange(boolean visible) {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
							// If the ViewPropertyAnimator API is available
							// (Honeycomb MR2 and later), use it to animate the
							// in-layout UI controls at the bottom of the
							// screen.
							if (mControlsHeight == 0) {
								mControlsHeight = controlsView.getHeight();
							}
							if (mShortAnimTime == 0) {
								mShortAnimTime = getResources().getInteger(
										android.R.integer.config_shortAnimTime);
							}
							controlsView
									.animate()
									.translationY(visible ? 0 : mControlsHeight)
									.setDuration(mShortAnimTime);
						} else {
							// If the ViewPropertyAnimator APIs aren't
							// available, simply show or hide the in-layout UI
							// controls.
							controlsView.setVisibility(visible ? View.VISIBLE
									: View.GONE);
						}

						if (visible && AUTO_HIDE) {
							// Schedule a hide().
							delayedHide(AUTO_HIDE_DELAY_MILLIS);
						}
					}
				});

		// Set up the user interaction to manually show or hide the system UI.
		contentView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (TOGGLE_ON_CLICK) {
					mSystemUiHider.toggle();
				} else {
					mSystemUiHider.show();
				}
			}
		});

		mButton = (Button) findViewById(R.id.show_fullscreen);
		mButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mSystemUiHider.hide();
			}
		});
		
		// Upon interacting with UI controls, delay any scheduled hide()
		// operations to prevent the jarring behavior of controls going away
		// while interacting with the UI.
		if (AUTO_HIDE)
			mButton.setOnTouchListener(
					mDelayHideTouchListener);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);

		// Trigger the initial hide() shortly after the activity has been
		// created, to briefly hint to the user that UI controls
		// are available.
		delayedHide(100);
	}

	/**
	 * Touch listener to use for in-layout UI controls to delay hiding the
	 * system UI. This is to prevent the jarring behavior of controls going away
	 * while interacting with activity UI.
	 */
	View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
		@Override
		public boolean onTouch(View view, MotionEvent motionEvent) {
			if (AUTO_HIDE) {
				delayedHide(AUTO_HIDE_DELAY_MILLIS);
			}
			return false;
		}
	};

	Handler mHideHandler = new Handler();
	Runnable mHideRunnable = new Runnable() {
		@Override
		public void run() {
			mSystemUiHider.hide();
		}
	};

	private ViewPager contentView;
	private TextView captionView;

	/**
	 * Schedules a call to hide() in [delay] milliseconds, canceling any
	 * previously scheduled calls.
	 */
	private void delayedHide(int delayMillis) {
		mHideHandler.removeCallbacks(mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, delayMillis);
	}
	
	@Override
	protected void onDestroy() {
		imageDownloader.stopDownload();
		super.onDestroy();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(final int position) {
		captionView.post(new Runnable() {
			
			@Override
			public void run() {
				setCaption(position);
			}
		});
	}

	protected void setCaption(int position) {
		if (captions != null && captions.size() > position && captions.get(position) != null && captions.get(position).equals("Exterior") == false) {
			captionView.setText(captions.get(position));
			captionView.setVisibility(View.VISIBLE);
		}
		else {
			captionView.setVisibility(View.GONE);
		}
		setTitle("Photo "+(position+1)+"/"+captions.size());
	}
}
