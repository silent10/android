package com.virtual_hotel_agent.search.controllers.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelImageTuple;
import com.ean.mobile.hotel.HotelInformation;
import com.evature.util.Log;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.views.adapters.BitmapAdapter;

public class ImageGalleryActivity extends BaseActivity implements OnPageChangeListener {

	private static final String TAG = "ImageGalleryActivity";

	public static final String PHOTO_INDEX = "PhotoIndex";
	public static final String HOTEL_ID = "HotelId";
	public static final String PHOTO_URLS = "PhotoUrls";

	public static final String TITLE = "Title";


	private int initialPage = 99999;
	private ImageDownloader imageDownloader;
	private BitmapAdapter adapter;
	private UnderlinePageIndicator mIndicator;

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
	
	private ArrayList<String> captions;

	private String mTitle;

	private Toolbar mToolbar;


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_gallery);

		Intent intent = getIntent();
		
		String[] urlsArray = intent.getStringArrayExtra(PHOTO_URLS);
		
		long hotelId = intent.getLongExtra(HOTEL_ID, -1l);
		if (hotelId == -1 && urlsArray == null) {
			VHAApplication.logError(TAG, "No hotel ID and no urls");
			this.finish();
			return;
		}

		Hotel hotelData = null;
		if (hotelId != -1) {
			hotelData = VHAApplication.HOTEL_ID_MAP.get(hotelId);
			if (hotelData == null) {
				VHAApplication.logError(TAG, "No DB - hotelId = "+hotelId);
				this.finish();
				return;
			}
			
			mTitle = hotelData.name;
		}
		else {
			mTitle = intent.getStringExtra(TITLE);
			if (mTitle == null) {
				mTitle = "";
			}
		}

		WeakReference<ImageGalleryActivity> _this = new WeakReference<ImageGalleryActivity>(this);
		mHandlerImgDownloaded = new DownloadedImg(_this);

		//final View controlsView = findViewById(R.id.fullscreen_content_controls);
		contentView = (ViewPager) findViewById(R.id.fullscreen_content);
		captionView = (TextView) findViewById(R.id.photo_caption);

		adapter = new BitmapAdapter(this);
		contentView.setAdapter(adapter);
		
		mIndicator = (UnderlinePageIndicator)findViewById(R.id.indicator);
		mIndicator.setViewPager(contentView);
		
		imageDownloader = new ImageDownloader(VHAApplication.HOTEL_PHOTOS, mHandlerImgDownloaded);//, mHandlerAllDone);
		
		ArrayList<String> urls;
		if (urlsArray != null) {
			captions = null;
			urls = new ArrayList<String>(Arrays.asList(urlsArray));
		}
		else {
			captions = new ArrayList<String>();
			urls = new ArrayList<String>();
			if (hotelData != null) {
				HotelInformation info = VHAApplication.EXTENDED_INFOS.get(hotelData.hotelId);
				if (info != null && info.images.size() > 0 ) {
					for (HotelImageTuple hotelImage : info.images) {
						if (hotelImage.mainUrl != null) {
							urls.add(hotelImage.mainUrl.toString());
							if (hotelImage.caption != null) {
								captions.add(hotelImage.caption);
							}
							else {
								captions.add("");
							}
						}
					}
				}
			}
		}

		if (urls.size() == 0) {
			VHAApplication.logError(TAG, "No images");
			this.finish();
			return;
		}
		
		mToolbar = (Toolbar) findViewById(R.id.toolbar);
		mToolbar.setTitle(getString(R.string.app_name));
		setSupportActionBar(mToolbar);
		
		final ActionBar supportActionBar = getSupportActionBar();
		supportActionBar.setHomeButtonEnabled(true);

		
		initialPage = intent.getIntExtra(PHOTO_INDEX, 99999);
		if (initialPage != 99999) {
			setCaption(initialPage);
		}
		else {
			setCaption(0);
		}
		
		imageDownloader.startDownload(urls);
		mIndicator.setOnPageChangeListener(this);
		
		Log.i(TAG, "Showing "+urls.size()+" imgs for hotel "+hotelId+"  jumping to img: "+initialPage);
		
		if (urls.size() > 1) {
			Toast.makeText(this, "Swipe left and right to view other photos\nPress 'back' to close", Toast.LENGTH_LONG).show();
		}
	}



	private ViewPager contentView;
	private TextView captionView;

	
	@Override
	protected void onDestroy() {
		if (imageDownloader != null)
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
		if (position < adapter.getCount()) {
			Bitmap bitmap = adapter.getBitmap(position);
			Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
				@Override
				public void onGenerated(Palette palette) {
					Palette.Swatch vibrant = palette.getVibrantSwatch();
					if (vibrant != null) {
						// If we have a vibrant color update the title TextView
						mToolbar.setBackgroundColor(vibrant.getRgb());
						mToolbar.setTitleTextColor(vibrant.getTitleTextColor());
						mToolbar.setSubtitleTextColor(vibrant.getTitleTextColor());
					}
					Palette.Swatch muted = palette.getLightMutedSwatch();
					if (muted != null) {
						contentView.setBackgroundColor(muted.getRgb());
					}
				}
			});
		}
		
		captionView.setVisibility(View.GONE);
		if (captions != null && captions.size() > position) {
			String text = "";
			if (captions.get(position) != null) {
				text = captions.get(position).trim();
			}
			mToolbar.setTitle((position + 1) + "/" + captions.size() + " - " + mTitle);
			if (text.equals("") == false && text.equals("todo") == false) {
				//captionView.setText(text);
				mToolbar.setSubtitle(text);
				//captionView.setVisibility(View.VISIBLE);
			}
			else {
				mToolbar.setSubtitle("");
			}
			//setTitle((position + 1) + "/" + captions.size() + " - " + mTitle);
		} else {
			//setTitle(mTitle);
			mToolbar.setTitle(mTitle);
		}
	}
}
