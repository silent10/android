package com.virtual_hotel_agent.search.views.fragments;

import java.text.DecimalFormat;

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

import com.evature.util.Log;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.virtual_hotel_agent.search.ImageGalleryActivity;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.activities.SelectRoomActivity;
import com.virtual_hotel_agent.search.controllers.activities.HotelMapActivity;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelSummary;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;
import com.virtual_hotel_agent.search.views.adapters.HotelGalleryAdapter;
import com.virtual_hotel_agent.search.views.adapters.ImageAdapter;

@SuppressLint("ValidFragment")
public class HotelDetailFragment extends RoboFragment implements OnItemClickListener {

	protected static final String TAG = HotelDetailFragment.class.getSimpleName();

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
//	private RatingBar mTripAdvisorRatingBar;
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

	HotelDetailFragment(int hotelIndex) {
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

//		mTripAdvisorRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarTripAdvisor);
		mStarRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarStar);

		mAmenitiesGridView = (GridView) mView.findViewById(R.id.amenitiesGridview);

		mHotelData = MyApplication.getDb().mHotelData[mHotelIndex];

		mHotelGalleryAdapter = new HotelGalleryAdapter(getActivity());

		mEvaBmp = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.hotel72);

		mHotelGalleryAdapter.addBitmap(mEvaBmp);

		mHotelGallery.setAdapter(mHotelGalleryAdapter);
		
		mHotelGallery.setOnItemClickListener(this);

		fillData();

		mRunThreads = true;

		startImageDownload();

		return mView;
	}

	public HotelDetailFragment() {

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

//		mTripAdvisorRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarTripAdvisor);
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
		
		description += "&lt;p&gt;\n&lt;b&gt;Note:&lt;/b&gt; It is the responsibility of the hotel chain and/or the"
			+ " individual property to ensure the accuracy of the photos displayed. \"Eva Travel Search\" is"
			+ " not responsible for any inaccuracies in the photos. &lt;/p&gt;";
		Spanned marked_up = Html.fromHtml("<html><body>" + description + "</body></html>");

		mPropertyDescription
			.loadData("<font color=\"black\">" + marked_up.toString() + "</font>", "text/html", "utf-8");
		
		mPropertyDescription.setBackgroundColor(Color.rgb(0xe3, 0xe3, 0xe3));

		//mTripAdvisorRatingBar.setRating((float) mHotelData.mSummary.mTripAdvisorRating);

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
			thumbIds[count++] = R.drawable.breakfast;

		ImageAdapter amenitiesImageAdapter = new ImageAdapter(getActivity(), thumbIds);

		mAmenitiesGridView.setAdapter(amenitiesImageAdapter);
		if (count == 0) {
			mAmenitiesGridView.setVisibility(View.GONE);
		}

		mBookButton = (Button) mView.findViewById(R.id.selectButton);

		mBookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//Log.e(TAG, "PLEASE IMPLEMENT CHECKOUT");
				Intent intent = new Intent(getActivity(), SelectRoomActivity.class);
				intent.putExtra(SelectRoomActivity.HOTEL_INDEX, mHotelIndex);
				getActivity().startActivityForResult(intent, 0);
			}
		});
		
		mMapButton = (Button) mView.findViewById(R.id.mapButton);

		mMapButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(),HotelMapActivity.class);
				
				HotelSummary hotelSummary = mHotelData.mSummary;
				intent.putExtra(HotelMapActivity.HOTEL_NAME, hotelSummary.mName);	
				intent.putExtra(HotelMapActivity.HOTEL_LATITUDE,""+(hotelSummary.mLatitude));
				intent.putExtra(HotelMapActivity.HOTEL_LONGITUDE,""+(hotelSummary.mLongitude));
				
				double rating = hotelSummary.mHotelRating;
				String formattedRating = Integer.toString((int) rating);
				if (Math.round(rating) != Math.floor(rating)) {
					formattedRating += "½";
				}

				String snippet = formattedRating + " stars";
				
				DecimalFormat rateFormat = new DecimalFormat("#.00");
				String formattedRate = rateFormat.format(hotelSummary.mLowRate);
				if (getActivity() != null) {
					String rate = formattedRate + " " +  SettingsAPI.getCurrencySymbol(getActivity());
					snippet += ", " + rate;
				}
				intent.putExtra(HotelMapActivity.HOTEL_SNIPPET, snippet); 
				
				getActivity().startActivity(intent);
			}
		});
		
		

	}

	public HotelDetailFragment(String hotelInfo, int hotelIndex) {
		mHotelIndex = hotelIndex;
	}

	public static HotelDetailFragment newInstance(int hotelIndex) {
		return new HotelDetailFragment(hotelIndex);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if (isAdded()) {
			Intent intent = new Intent(this.getActivity(), ImageGalleryActivity.class);
			startActivity(intent);
		}
	}
}