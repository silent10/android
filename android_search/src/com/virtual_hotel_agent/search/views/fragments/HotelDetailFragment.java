package com.virtual_hotel_agent.search.views.fragments;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.evature.util.Log;
import com.google.inject.Inject;
import com.virtual_hotel_agent.search.ImageGalleryActivity;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.activities.HotelMapActivity;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.controllers.events.HotelSelected;
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelDetails.HotelImage;
import com.virtual_hotel_agent.search.models.expedia.HotelSummary;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.views.adapters.ImageAdapter;
import com.virtual_hotel_agent.search.views.adapters.PhotoGalleryAdapter;

@SuppressLint("ValidFragment")
public class HotelDetailFragment extends RoboFragment implements OnItemClickListener {

	protected static final String TAG = HotelDetailFragment.class.getSimpleName();

	private static final String HOTEL_INDEX = "hotelIndex";
	int mHotelIndex = -1;

	private static final int WIFI_AMENITY_CODE = 8;
	private static final int PARKING_AMENITY_CODE = 16384;
	private static final int POOL_AMENITY_CODE = 128;
	private static final int BREAKFEST_AMENITY_CODE = 2048;
	private Gallery mHotelGallery;
	private TextView mHotelName;
	private WebView mPropertyDescription;
//	private RatingBar mTripAdvisorRatingBar;
	private RatingBar mStarRatingBar;
	private GridView mAmenitiesGridView;
	private Button mBookButton;
	private Button mMapButton;
	private ScrollView mScrollView;
	HotelData mHotelData = null;
	
	@Inject protected EventManager eventManager;

	private View mView = null;
	private PhotoGalleryAdapter mHotelGalleryAdapter;
	private Bitmap mVhaBmp;
	private Bitmap mEvaBmpCached;


	static boolean mViewingHotelData = false;

	static class DownloadedImg extends Handler {
		private WeakReference<HotelDetailFragment> fragmentRef;

		public DownloadedImg(WeakReference<HotelDetailFragment> fragmentRef) {
			this.fragmentRef = fragmentRef;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (fragmentRef != null) {
				HotelDetailFragment hdf = fragmentRef.get();
				if (hdf != null) {
					if (hdf.mVhaBmp != null) {
						hdf.mHotelGalleryAdapter.removeBitmap(hdf.mVhaBmp);
						hdf.mVhaBmp = null;
					}
					
					hdf.mHotelGalleryAdapter.addBitmap((Bitmap) msg.obj);
				}
			}
			super.handleMessage(msg);
		}
	}
	
	
	private Handler mHandlerFinish;
	private ImageDownloader imageDownloader;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (mView != null) {
			((ViewGroup) mView.getParent()).removeView(mView);
			Log.w(TAG, "Fragment create view twice");
			return mView;
		}

//		int orientation = getResources().getConfiguration().orientation;
//		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			mView = inflater.inflate(R.layout.hotel_details_landscape, container, false);
//		} else {
		mView = inflater.inflate(R.layout.fragment_hotel_details_portrait, container, false);
//		}

		mBookButton = (Button) mView.findViewById(R.id.selectButton);
		mMapButton = (Button) mView.findViewById(R.id.mapButton);
		mScrollView = (ScrollView) mView.findViewById(R.id.scrollView1);
		
		mHotelGallery = (Gallery) mView.findViewById(R.id.hotelGallery);
		mHotelName = (TextView) mView.findViewById(R.id.hotelName);
		mPropertyDescription = (WebView) mView.findViewById(R.id.propertyDescription);
//		mTripAdvisorRatingBar;
		mStarRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarStar);
		mAmenitiesGridView = (GridView) mView.findViewById(R.id.amenitiesGridview);
		
		WeakReference<HotelDetailFragment> _this = new WeakReference<HotelDetailFragment>(this);
		mHandlerFinish = new DownloadedImg(_this);

		mHotelGalleryAdapter = new PhotoGalleryAdapter(getActivity());

		mHotelGallery.setOnItemClickListener(this);
		
		mEvaBmpCached = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.hotel72);


		XpediaDatabase db = MyApplication.getDb();
		if (db == null) {
			MainActivity.LogError(TAG, "onCreateView - no DB");
		}
		else {
			ExpediaAppState rp = MyApplication.getExpediaAppState();
			if (rp == null) {
				MainActivity.LogError(TAG, "onCreateView - no RequestParams");
			}
			else {
				changeHotelId(rp.getHotelId());
			}
		}

		return mView;
	}

	public HotelDetailFragment() {

	}
	
	public void changeHotelId(int hotelIndex) {
		if (hotelIndex == -1)
			return;
		
		Log.i(TAG, "Setting hotelId to "+hotelIndex+", was "+mHotelIndex);
		if (mHotelIndex == hotelIndex) {
			return;
		}
		mHotelIndex = hotelIndex;
		fillData();
	}

	@Override
	public void onDestroy() {
		if (imageDownloader != null)
			imageDownloader.stopDownload();
		if (mPropertyDescription != null) {
			mPropertyDescription.destroy();
		}
		super.onDestroy();
	}
	
	void fillData() {
		Log.i(TAG, "Filling data for hotel "+mHotelIndex);
		mPropertyDescription.loadData("","text/html", "utf-8");
		mScrollView.setScrollY(0);
		
		XpediaDatabase db = MyApplication.getDb();
		if (db == null || db.mHotelData == null || db.mHotelData.length <= mHotelIndex) {
			return;
		}

		mHotelData = db.mHotelData[mHotelIndex];
		Spanned spannedName = Html.fromHtml(mHotelData.mSummary.mName);
		String name = spannedName.toString();

		Log.d(TAG, "Filling hotel data: "+mHotelName.getText()+ "  --> "+name);

//		Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

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
		Log.i(TAG, "hotel name = " + name);

		Log.i(TAG, "1)mHotelData.mSummary.mName:" + mHotelData.mSummary.mName);
		
		String description = "";
		if(mHotelData.mDetails!=null)
		{
			description += mHotelData.mDetails.propertyDescription;
		}
		
		description += "&lt;p&gt;\n&lt;b&gt;Note:&lt;/b&gt; It is the responsibility of the hotel chain and/or the"
			+ " individual property to ensure the accuracy of the photos displayed. \"Virtual Hotel Agent\" is"
			+ " not responsible for any inaccuracies in the photos. &lt;/p&gt;";
		Spanned marked_up = Html.fromHtml("<html><body>" + description + "</body></html>");

		mPropertyDescription
			.loadData("<font color=\"black\">" + marked_up.toString() + "</font>", "text/html", "utf-8");
		
		mPropertyDescription.setBackgroundColor(Color.rgb(0xe3, 0xe3, 0xe3));

		//mTripAdvisorRatingBar.setRating((float) mHotelData.mSummary.mTripAdvisorRating);

		mStarRatingBar.setRating((float) mHotelData.mSummary.mHotelRating);

		if (imageDownloader != null) {
			imageDownloader.stopDownload();
		}
		
		mVhaBmp = mEvaBmpCached;
		mHotelGalleryAdapter.clear();
		mHotelGalleryAdapter.addBitmap(mVhaBmp);

		mHotelGallery.setAdapter(mHotelGalleryAdapter);


		imageDownloader = new ImageDownloader(db.getImagesCache(), mHandlerFinish);
		
		if (mHotelData != null && mHotelData.mDetails != null && mHotelData.mDetails.hotelImages != null) {
			ArrayList<String> urls = new ArrayList<String>();
			for (HotelImage hotel : mHotelData.mDetails.hotelImages) {
				if (hotel.url != null)
					urls.add(hotel.url);
			}
			Log.i(TAG, "gallery showing "+urls.size()+" imgs for hotel "+mHotelIndex);
			imageDownloader.startDownload(urls);
		}

		
		boolean wifiAvailable = false;
		boolean poolAvailable = false;
		boolean breakfestAvailable = false;
		boolean parkingAvailable = false;

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

		ArrayList<Integer> thumbIds = new ArrayList<Integer>();

		if (wifiAvailable)
			thumbIds.add(R.drawable.internet);
		if (parkingAvailable)
			thumbIds.add(R.drawable.parking);
		if (poolAvailable)
			thumbIds.add(R.drawable.pool);
		if (breakfestAvailable)
			thumbIds.add(R.drawable.breakfast);

		if (thumbIds.size() == 0) {
			mAmenitiesGridView.setVisibility(View.GONE);
		}
		else {
			ImageAdapter amenitiesImageAdapter = new ImageAdapter(getActivity(), thumbIds);
			mAmenitiesGridView.setAdapter(amenitiesImageAdapter);
		}
		mBookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				//Log.e(TAG, "PLEASE IMPLEMENT CHECKOUT");
//				Intent intent = new Intent(getActivity(), SelectRoomActivity.class);
//				intent.putExtra(SelectRoomActivity.HOTEL_INDEX, mHotelIndex);
//				getActivity().startActivityForResult(intent, 0);
//				
				
				eventManager.fire(new HotelSelected(mHotelIndex));
			}
		});
		
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if (isAdded()) {
			Intent intent = new Intent(this.getActivity(), ImageGalleryActivity.class);
			intent.putExtra(ImageGalleryActivity.PHOTO_INDEX, position);
			intent.putExtra(ImageGalleryActivity.HOTEL_ID, mHotelIndex);
			startActivity(intent);
		}
	}
}