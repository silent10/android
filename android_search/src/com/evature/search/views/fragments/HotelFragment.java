package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.controllers.activities.EvaCheckoutActivity;
import com.evature.search.controllers.activities.HotelMapActivity;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.XpediaProtocolStatic;
import com.evature.search.views.adapters.HotelGalleryAdapter;
import com.evature.search.views.adapters.ImageAdapter;

@SuppressLint("ValidFragment")
public class HotelFragment extends RoboFragment {

	protected static final String TAG = HotelFragment.class.getSimpleName();

	private static final String HOTEL_INDEX = "hotelIndex";
	int mHotelIndex;

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(HOTEL_INDEX, mHotelIndex);
		super.onSaveInstanceState(outState);
	}

	private static final int WIFI_AMENITY_CODE = 8;
	private static final int PARKING_AMENITY_CODE = 16384;
	private static final int POOL_AMENITY_CODE = 128;
	private static final int BREAKFEST_AMENITY_CODE = 2048;
	protected static final int FINISHED_DATA_DOWNLOAD = 1;
	protected static final int UPDATE_ACTIVITY = 2;
	protected static final int IMAGE_DOWNLOADED = 3;
	protected static final int ROOMS_UPDATED = 4;
	public static final int FINISHED_DATA_DOWNLOAD_ERROR = 0xFF;
	private Gallery mHotelGallery;

	private TextView mHotelName;
	private WebView mPropertyDescription;
	private RatingBar mTripAdvisorRatingBar;
	private RatingBar mStarRatingBar;
	private GridView mAmenitiesGridView;
	private Button mBookButton;

	HotelData mHotelData = null;

	private View mView;
	private HotelGalleryAdapter mHotelGalleryAdapter;
	private Bitmap mEvaBmp;
	private Thread mImageDownloadThread;

	private Button mMapButton;

	static boolean mViewingHotelData = false;

	HotelFragment(int hotelIndex) {
		mHotelIndex = hotelIndex;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// if (savedInstanceState != null) {
		// mHotelIndex = savedInstanceState.getInt(HOTEL_INDEX, mHotelIndex);
		// }

		Log.i(TAG, "onCreateView for hotel index " + mHotelIndex);

		if (MyApplication.getDb() == null) {
			return super.onCreateView(inflater, container, savedInstanceState);
		}

		int orientation = getResources().getConfiguration().orientation;

		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			mView = inflater.inflate(R.layout.hotel_details_landscape, container, false);
		} else {
			mView = inflater.inflate(R.layout.hotel_details_portrait, container, false);
		}

		mHotelGallery = (Gallery) mView.findViewById(R.id.hotelGallery);

		mHotelName = (TextView) mView.findViewById(R.id.hotelName);

		mPropertyDescription = (WebView) mView.findViewById(R.id.propertyDescription);

		mTripAdvisorRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarTripAdvisor);
		mStarRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarStar);

		mAmenitiesGridView = (GridView) mView.findViewById(R.id.amenitiesGridview);

		mHotelData = MyApplication.getDb().mHotelData[mHotelIndex];

		mHotelGalleryAdapter = new HotelGalleryAdapter(getActivity());

		mEvaBmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.eva_hotel_icon);

		mHotelGalleryAdapter.addBitmap(mEvaBmp);

		mHotelGallery.setAdapter(mHotelGalleryAdapter);

		fillData();

		mRunThreads = true;

		startImageDownload();

		return mView;
	}

	public HotelFragment() {

	}

	@Override
	public void onDestroy() {
		mRunThreads = false;
		super.onDestroy();
	}

	@SuppressLint("ValidFragment")
	void fillData() {
		mHotelGallery = (Gallery) mView.findViewById(R.id.hotelGallery);

		mHotelName = (TextView) mView.findViewById(R.id.hotelName);

		mPropertyDescription = (WebView) mView.findViewById(R.id.propertyDescription);

		mTripAdvisorRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarTripAdvisor);
		mStarRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarStar);

		mAmenitiesGridView = (GridView) mView.findViewById(R.id.amenitiesGridview);

		mHotelGallery.setAdapter(mHotelGalleryAdapter);

		Spanned spannedName = Html.fromHtml(mHotelData.mSummary.mName);

		String name = spannedName.toString();

		Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		/* Now we can retrieve all display-related infos */
		// int width = display.getWidth();
		// int height = display.getHeight();
		//
		// int maxNameLength = width/18-3;
		//
		// if(name.length()>maxNameLength)
		// {
		// name = (name.subSequence(0, maxNameLength)).toString();
		// name+="...";
		// }

		mHotelName.setText(name);
		Log.i(TAG, "fillData hotel name = " + name);

		Log.i(TAG, "1)mHotelData.mSummary.mName:" + mHotelData.mSummary.mName);
		
		String description = "";
		if(mHotelData.mDetails!=null)
		{
			Log.i(TAG, "2)mHotelData.mDetails.propertyDescription:" + mHotelData.mDetails.propertyDescription);
			description += mHotelData.mDetails.propertyDescription;
		}

		description += "<br>\n <br>\n Note: It is the responsibility of the hotel chain and/or the"
			+ " individual property to ensure the accuracy of the photos displayed. \"Eva Travel Search\" is"
			+ " not responsible for any inaccuracies in the photos.";
		Spanned marked_up = Html.fromHtml("<html><body>" + description + "</body></html>");

		mPropertyDescription
			.loadData("<font color=\"black\">" + marked_up.toString() + "</font>", "text/html", "utf-8");
		
		mPropertyDescription.setBackgroundColor(Color.rgb(0xe3, 0xe3, 0xe3));

		mTripAdvisorRatingBar.setRating((float) mHotelData.mSummary.mTripAdvisorRating);

		mStarRatingBar.setRating((float) mHotelData.mSummary.mHotelRating);

		boolean wifiAvailable = false;
		boolean poolAvailable = false;
		boolean breakfestAvailable = false;
		boolean parkingAvailable = false;
		int count = 0;

		if ((mHotelData.mSummary.mAmenityMask & WIFI_AMENITY_CODE) == WIFI_AMENITY_CODE) {
			wifiAvailable = true;
		}

		if ((mHotelData.mSummary.mAmenityMask & PARKING_AMENITY_CODE) == PARKING_AMENITY_CODE) {
			parkingAvailable = true;
		}

		if ((mHotelData.mSummary.mAmenityMask & BREAKFEST_AMENITY_CODE) == BREAKFEST_AMENITY_CODE) {
			breakfestAvailable = true;
		}

		if ((mHotelData.mSummary.mAmenityMask & POOL_AMENITY_CODE) == POOL_AMENITY_CODE) {
			poolAvailable = true;
		}

		if (wifiAvailable)
			count++;
		if (parkingAvailable)
			count++;
		if (poolAvailable)
			count++;
		if (breakfestAvailable)
			count++;

		Integer thumbIds[] = new Integer[count];

		count = 0;

		if (wifiAvailable)
			thumbIds[count++] = R.drawable.internet;
		if (parkingAvailable)
			thumbIds[count++] = R.drawable.parking;
		if (poolAvailable)
			thumbIds[count++] = R.drawable.pool;
		if (breakfestAvailable)
			thumbIds[count++] = R.drawable.breakfest;

		ImageAdapter amenitiesImageAdapter = new ImageAdapter(getActivity(), thumbIds);

		mAmenitiesGridView.setAdapter(amenitiesImageAdapter);
		if (count == 0) {
			mAmenitiesGridView.setVisibility(View.GONE);
		}

		mBookButton = (Button) mView.findViewById(R.id.selectButton);

		mBookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e(TAG, "PLEASE IMPLEMENT CHECKOUT");
				Intent intent = new Intent(getActivity(), EvaCheckoutActivity.class);
				intent.putExtra(EvaCheckoutActivity.HOTEL_INDEX, mHotelIndex);
				getActivity().startActivityForResult(intent, 0);
			}
		});
		
		mMapButton = (Button) mView.findViewById(R.id.mapButton);

		mMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),HotelMapActivity.class);
				
				intent.putExtra(HotelMapActivity.HOTEL_NAME,mHotelData.mSummary.mName);	
				intent.putExtra(HotelMapActivity.HOTEL_LATITUDE,""+(mHotelData.mSummary.mLatitude));
				intent.putExtra(HotelMapActivity.HOTEL_LONGITUDE,""+(mHotelData.mSummary.mLongitude));
				intent.putExtra(HotelMapActivity.HOTEL_CITY,mHotelData.mSummary.mCity);
				getActivity().startActivity(intent);
			}
		});
		
		

	}

	public HotelFragment(String hotelInfo, int hotelIndex) {
		mHotelIndex = hotelIndex;
	}

	public static HotelFragment newInstance(int hotelIndex) {
		return new HotelFragment(hotelIndex);
	}

	private boolean mRunThreads;

	private Handler mHandlerFinish = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (mEvaBmp != null) {
				mHotelGalleryAdapter.removeBitmap(mEvaBmp);
				mEvaBmp = null;
			}
			mHotelGalleryAdapter.addBitmap((Bitmap) msg.obj);
			super.handleMessage(msg);
		}
	};

	private void startImageDownload() {
		mImageDownloadThread = new Thread() {

			@Override
			public void run() {
				Bitmap bmp;
				
				
				if(mHotelData.mDetails==null)
				{
					return;
				}
				
				if ((mHotelData.mDetails.hotelImages != null) && (mRunThreads)) {
					for (int i = 0; i < mHotelData.mDetails.hotelImages.length; i++) {
						bmp = null;

						if (mHotelData.mDetails.hotelImages[i] != null) {
							if (mHotelData.mDetails.hotelImages[i].url != null) {
								bmp = XpediaProtocolStatic.download_Image(mHotelData.mDetails.hotelImages[i].url);
							}
						}

						if (bmp != null) {
							Message message = mHandlerFinish.obtainMessage();
							message.obj = bmp;
							message.what = IMAGE_DOWNLOADED;
							mHandlerFinish.sendMessage(message);
						}

					}
				}
			}
		};
		mImageDownloadThread.start();
	}
}