package com.virtual_hotel_agent.search;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.evature.util.Log;
import com.viewpagerindicator.UnderlinePageIndicator;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelDetails.HotelImage;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.views.adapters.BitmapAdapter;

public class ImageGalleryActivity extends Activity implements OnPageChangeListener {

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


	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_gallery);


		Intent intent = getIntent();
		
		String[] urlsArray = intent.getStringArrayExtra(PHOTO_URLS);
		
		int hotelId = intent.getIntExtra(HOTEL_ID, -1);
		if (hotelId == -1 && urlsArray == null) {
			MainActivity.LogError(TAG, "No hotel ID and no urls");
			this.finish();
			return;
		}

		XpediaDatabase db = MyApplication.getDb();
		HotelData hotelData = null;
		if (hotelId != -1) {
			if (db != null && db.mHotelData != null && db.mHotelData.length > hotelId) {
				hotelData = db.mHotelData[hotelId];
			}
			else {
				MainActivity.LogError(TAG, "No DB - hotelId = "+hotelId);
				this.finish();
				return;
			}
			
			mTitle = hotelData.mSummary.mName;
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
		
		imageDownloader = new ImageDownloader(db.getImagesCache(), mHandlerImgDownloaded);//, mHandlerAllDone);
		
		ArrayList<String> urls;
		if (urlsArray != null) {
			captions = null;
			urls = new ArrayList<String>(Arrays.asList(urlsArray));
		}
		else {
			captions = new ArrayList<String>();
			urls = new ArrayList<String>();
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
		}

		if (urls.size() == 0) {
			MainActivity.LogError(TAG, "No images");
			this.finish();
			return;
		}
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
		captionView.setVisibility(View.GONE);
		if (captions != null && captions.size() > position && captions.get(position) != null) {
			 String text = captions.get(position).trim();
			 if (text.equals("Exterior")==false && text.equals("") == false) {
				captionView.setText(text);
				captionView.setVisibility(View.VISIBLE);
			 }
			 setTitle((position+1)+"/"+captions.size() + " - "+ mTitle);
		}
		else {
			setTitle(mTitle);
		}
	}
}
