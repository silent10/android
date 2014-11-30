package com.virtual_hotel_agent.search.controllers.activities;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
		
		mToolbar.setTitle(mTitle);
		
		final ActionBar supportActionBar = getSupportActionBar();
		supportActionBar.setHomeButtonEnabled(true);

		
		initialPage = intent.getIntExtra(PHOTO_INDEX, 99999);
		if (initialPage != 99999) {
			setCaption(initialPage, 0);
		}
		else {
			setCaption(0, 0);
		}
		
		imageDownloader.startDownload(urls);
		mIndicator.setOnPageChangeListener(this);
		
		Log.i(TAG, "Showing "+urls.size()+" imgs for hotel "+hotelId+"  jumping to img: "+initialPage);
		
		if (urls.size() > 1) {
			Toast.makeText(this, "Swipe left and right to view other photos\nPress 'back' to close", Toast.LENGTH_LONG).show();
		}
	}



	private ViewPager contentView;

	
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
	public void onPageScrolled(final int position, final float positionOffset, int positionOffsetPixels) {
		contentView.post(new Runnable() {
			@Override
			public void run() {
				setCaption(position, positionOffset);
			}
		});
	}

	@Override
	public void onPageSelected(final int position) {
		contentView.post(new Runnable() {
			@Override
			public void run() {
				setCaption(position, 0);
			}
		});
	}
	
	private int transitionColor(int color1, int color2, float offset) {
		float[] hsv1 = {0,0,0};
		float[] hsv2 = {0,0,0};
		Color.RGBToHSV(Color.red(color1), Color.green(color1), Color.blue(color1), hsv1);
		Color.RGBToHSV(Color.red(color2), Color.green(color2), Color.blue(color2), hsv2);
		if (Math.abs(hsv1[0]-hsv2[0]) > 180.0) {
			if (hsv1[0] < hsv2[0]) {
				hsv1[0] += 360.0;
			}
			else {
				hsv2[0] += 360.0;
			}
		}
		float invOff = 1-offset;
		float hue = hsv1[0]* invOff + hsv2[0]*offset;
		if (hue > 360.0) {
			hue -= 360.0;
		}
		float sat = hsv1[1]* invOff + hsv2[1]*offset;
		float val = hsv1[2]* invOff + hsv2[2]*offset;
		
		return Color.HSVToColor(new float[] {hue, sat,val});
	}

	protected void setCaption(final int position, final float offset) {
		if (position < adapter.getCount()) {
			adapter.getPalette(position,  new Palette.PaletteAsyncListener() {
				@Override
				public void onGenerated(final Palette paletteLeft) {
					if (position+1 < adapter.getCount()) {
						adapter.getPalette(position+1,  new Palette.PaletteAsyncListener() { 
							@Override
							public void onGenerated(Palette paletteRight) {
								Palette.Swatch vibrantL = paletteLeft.getLightVibrantSwatch();
								Palette.Swatch vibrantR = paletteRight.getLightVibrantSwatch();
								if (vibrantL != null && vibrantR != null) {
									// If we have a vibrant color update the title TextView
									int textColor = transitionColor(vibrantL.getTitleTextColor(), vibrantR.getTitleTextColor(), offset) ;
									int bgColor = transitionColor(vibrantL.getRgb(), vibrantR.getRgb(), offset);
									mToolbar.setBackgroundColor(bgColor);
									mToolbar.setTitleTextColor(textColor );
									mToolbar.setSubtitleTextColor(textColor);
									mIndicator.setSelectedColor(bgColor);
								}
								Palette.Swatch mutedL = paletteLeft.getDarkMutedSwatch();
								Palette.Swatch mutedR = paletteRight.getDarkMutedSwatch();
								if (mutedL != null && mutedR != null) {
									int muted = transitionColor(mutedL.getRgb(), mutedR.getRgb(), offset);
									contentView.setBackgroundColor(muted);
								}
								
							}
						});
					}
					else {
						Palette.Swatch vibrant = paletteLeft.getVibrantSwatch();
						if (vibrant != null) {
							// If we have a vibrant color update the title TextView
							mToolbar.setBackgroundColor(vibrant.getRgb());
							mToolbar.setTitleTextColor(vibrant.getTitleTextColor());
							mToolbar.setSubtitleTextColor(vibrant.getTitleTextColor());
							mIndicator.setSelectedColor(vibrant.getRgb());
						}
						Palette.Swatch muted = paletteLeft.getDarkMutedSwatch();
						if (muted != null) {
							contentView.setBackgroundColor(muted.getRgb());
						}
					}
				}
			});
		}
		
		int curPosition = position;
		if (offset > 0.5) {
			curPosition++;
		}
		if (captions != null && captions.size() > curPosition) {
			String text = (curPosition + 1) + "/" + captions.size();
			if (captions.get(curPosition) != null) {
				String caption = captions.get(curPosition).trim();
				if (caption.equals("") == false && caption.equals("todo") == false) {
					text += " - "+caption;
				}
			}
			mToolbar.setSubtitle(text);
		} else {
			mToolbar.setSubtitle("");
		}
	}
}
