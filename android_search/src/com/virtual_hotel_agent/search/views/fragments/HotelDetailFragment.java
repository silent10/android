package com.virtual_hotel_agent.search.views.fragments;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ean.mobile.exception.EanWsError;
import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelImageTuple;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.hotel.HotelRoom;
import com.evature.util.DLog;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader.LoadedCallback;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.controllers.activities.HotelMapActivity;
import com.virtual_hotel_agent.search.controllers.activities.ImageGalleryActivity;
import com.virtual_hotel_agent.search.controllers.events.HotelSelected;
import com.virtual_hotel_agent.search.controllers.events.RatingClickedEvent;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListener;
import com.virtual_hotel_agent.search.controllers.web_services.RoomsUpdaterTask;
import com.virtual_hotel_agent.search.views.adapters.ImageAdapter;
import com.virtual_hotel_agent.search.views.adapters.PhotoGalleryAdapter;

import de.greenrobot.event.EventBus;

@SuppressLint("NewApi")
public class HotelDetailFragment extends Fragment implements  OnItemClickListener {

	protected static final String TAG = HotelDetailFragment.class.getSimpleName();

//	private static final String HOTEL_INDEX = "hotelIndex";
	private long mHotelId = -1;

	private static final int WIFI_AMENITY_CODE = 8;
	private static final int PARKING_AMENITY_CODE = 16384;
	private static final int POOL_AMENITY_CODE = 128;
	private static final int BREAKFEST_AMENITY_CODE = 2048;
	private Gallery mHotelGallery;
	private TextView mHotelName;
	private WebView mPropertyDescription;
	private View mTripAdvisorRatingBar;
	private ImageView mTripAdvisorRatingBar_image;
	private TextView mTripAdvisorRatingBar_text;
	private RatingBar mStarRatingBar;
	private GridView mAmenitiesGridView;
	private Button mBookButton;
	private Button mMapButton;
	private ScrollView mScrollView;
	

	private EventBus eventBus;
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
	
	static class AllDoneHandler extends Handler {
		private WeakReference<HotelDetailFragment> fragmentRef;

		public AllDoneHandler(WeakReference<HotelDetailFragment> fragmentRef) {
			this.fragmentRef = fragmentRef;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (fragmentRef != null) {
				HotelDetailFragment hdf = fragmentRef.get();
				if (hdf != null) {
					long id = hdf.mHotelGallery.getSelectedItemId();
					int realSize = hdf.mHotelGalleryAdapter.getRealSize();
					if (realSize > 3) {
						hdf.mHotelGalleryAdapter.setCyclic(true);
						hdf.mHotelGalleryAdapter.notifyDataSetChanged();
						int center = realSize * (int)((Integer.MAX_VALUE / 2) / realSize);
						hdf.mHotelGallery.setSelection((int)(center+id), false);
						hdf.mHotelGalleryAdapter.notifyDataSetChanged();
					}
					else {
						hdf.mHotelGalleryAdapter.setCyclic(false);
					}
				}
			}
			super.handleMessage(msg);
		}
		
	}
	

	private AllDoneHandler mAllDoneHandler;
	//private DownloadedImg mHandlerFinish;
	//private ImageDownloader imageDownloader;

	private OnClickListener mRatingClickHandler = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			eventBus.post(new RatingClickedEvent());
		}
	};


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (mView != null) {
			((ViewGroup) mView.getParent()).removeView(mView);
			DLog.w(TAG, "Fragment create view twice");
			return mView;
		}
		
		eventBus = EventBus.getDefault(); 

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
		mTripAdvisorRatingBar = mView.findViewById(R.id.tripAdvisor_ratingBar);
		mTripAdvisorRatingBar_image = (ImageView) mView.findViewById(R.id.tripadvisor_ratingBar_image);
		mTripAdvisorRatingBar_text = (TextView) mView.findViewById(R.id.tripadvisor_ratingBar_text);
		mStarRatingBar = (RatingBar) mView.findViewById(R.id.ratingBarStar);
		mAmenitiesGridView = (GridView) mView.findViewById(R.id.amenitiesGridview);
		
		mTripAdvisorRatingBar.setOnClickListener(mRatingClickHandler);
		mTripAdvisorRatingBar_image.setOnClickListener(mRatingClickHandler);
		WeakReference<HotelDetailFragment> _this = new WeakReference<HotelDetailFragment>(this);
		//mHandlerFinish = new DownloadedImg(_this);
		mAllDoneHandler = new AllDoneHandler(_this);
		
		mHotelGalleryAdapter = new PhotoGalleryAdapter(getActivity());

		mHotelGallery.setOnItemClickListener(this);
		
		mEvaBmpCached = BitmapFactory.decodeResource(getActivity().getResources(), R.drawable.slanted_icon_512);


		if (VHAApplication.selectedHotel == null) {
			DLog.e(TAG, "onCreateView - null selectedHotel");
		}
		else {
			changeHotelId(VHAApplication.selectedHotel.hotelId);
		}

		if (mHotelId != -1) {
			fillData();
		}
		
		return mView;
	}

	public HotelDetailFragment() {

	}
	
	public void changeHotelId(long hotelId) {
		if (hotelId == -1)
			return;
		
		DLog.i(TAG, "Setting hotelId to "+hotelId+", was "+mHotelId);
		if (mHotelId == hotelId) {
			return;
		}
		mHotelId = hotelId;
		startRoomSearch();
		VHAApplication.selectedRoom = null;
		fillData();
	}

	@Override
	public void onDestroy() {
//		if (imageDownloader != null)
//			imageDownloader.stopDownload();
		
		if (mPropertyDescription != null) {
			mPropertyDescription.destroy();
		}
		super.onDestroy();
	}
	
	private final OnClickListener mBookButtonListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
//				//Log.e(TAG, "PLEASE IMPLEMENT CHECKOUT");
//				Intent intent = new Intent(getActivity(), SelectRoomActivity.class);
//				intent.putExtra(SelectRoomActivity.HOTEL_INDEX, mHotelIndex);
//				getActivity().startActivityForResult(intent, 0);
//			
			final Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(mHotelId);
			if (hotel == null) {
				DLog.e(TAG, "selecting hotel id "+mHotelId +" but not found");
				return;
			}
			
			eventBus.post(new HotelSelected(hotel.hotelId));
		}
	};
	

	static private RoomsUpdaterTask mRoomUpdater = null;

	@Override public void onPause() {
		if (mRoomUpdater != null) {
			mRoomUpdater.cancel(true);
			mRoomUpdater = null;
		}
		super.onPause();
	};
	

	private void startRoomSearch() {
		if (mRoomUpdater != null) {
			if (false == mRoomUpdater.cancel(true)) {
				DLog.d(TAG, "false == mRoomUpdater.cancel(true)");
				mRoomUpdater = null;
				// return;
			}
		}
		
		// asdf mainView.removeTab(mainView.getRoomsTabName());
		// asdf mainView.removeTab(mainView.getBookingTabName());
		// asdf mainView.removeTab(mainView.getReservationsTabName());
		mRoomUpdater = new RoomsUpdaterTask(mHotelId);
		mRoomUpdater.attach(mRoomUpdaterListener);
		List<HotelRoom> rooms = VHAApplication.HOTEL_ROOMS.get(mHotelId);
		if (rooms != null) {
			// restored from cache - fake downloader progress
			mRoomUpdaterListener.endProgressDialog(-1, null);
		}
		else {
			//mainView.showStatus("Getting Rooms info for hotel");
			mRoomUpdater.execute();
		}
	}

	/**
	 * Listener to Room-information request complete
	 */
	private class RoomTaskListener extends DownloaderTaskListener {
		private boolean mSwitchToTab = false;

		@Override
		public void endProgressDialog(int id, Object result) { // we got the hotel rooms reply successfully
			DLog.d(TAG, "endProgressDialog() for hotel rooms for hotel "+mRoomUpdater.hotelId);
			//mainView.hideStatus();
			
			
			// asdf int index = mainView.getRoomsTabIndex();
			// asdf if (index == -1) {
			// asdf mainView.addTab(mainView.getRoomsTabName());
			// asdf index = mTabTitles.size()-1;
			// asdf }
			// asdf RoomsSelectFragement fragment = mainView.getRoomsFragment();
			// asdf if (fragment != null) // could be null if not instantiated yet
			// asdf {
			// asdf 	fragment.changeHotelId(mRoomUpdater.hotelId);
			// asdf 	if (mSwitchToTab) {
					// asdf mainView.setCurrentItem(index);
			// asdf mSwitchToTab  = false;
			// asdf 				}
			// asdf }
			mRoomUpdater = null;
		}
		
		@Override
		public void endProgressDialogWithError(int id, Object result) {
//			setDebugData(DebugTextType.ExpediaDebug, result);
			//mainView.hideStatus();
			if (mRoomUpdater.eanWsError != null) {
				EanWsError err = mRoomUpdater.eanWsError;
				if ("SOLD_OUT".equals(err.category)) {
					// asdf int index = mainView.getHotelTabIndex();
					// asdf if (index != -1) {
					// asdf HotelDetailFragment fragment = mainView.getHotelFragment();
					// asdf if (fragment != null) // could be null if not instantiated yet
					// asdf {
					// asdf 							fragment.hotelSoldOut();
					// asdf }
					// asdf }
					if (err.presentationMessage.equals("") == false)
						Toast.makeText(getActivity(), err.presentationMessage, Toast.LENGTH_LONG).show();
				}
			}
			mRoomUpdater = null;
		}

		public void switchToTab() {
			mSwitchToTab  = true;
		}
	};
	
	private RoomTaskListener mRoomUpdaterListener = new RoomTaskListener();
	
	private final OnClickListener mMapButtonLisener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(mHotelId);
			if (hotel == null) {
				DLog.e(TAG, "showing map for hotel id "+mHotelId +" but not found");
				return;
			}

			Intent intent = new Intent(getActivity(),HotelMapActivity.class);
			intent.putExtra(HotelMapActivity.HOTEL_NAME, hotel.name);	
			intent.putExtra(HotelMapActivity.HOTEL_LATITUDE,""+(hotel.address.latitude));
			intent.putExtra(HotelMapActivity.HOTEL_LONGITUDE,""+(hotel.address.longitude));
			
			double rating = hotel.starRating.doubleValue();
			String formattedRating = Integer.toString((int) rating);
			if (Math.round(rating) != Math.floor(rating)) {
				formattedRating += "½";
			}

			String snippet = formattedRating + " stars";
			
			DecimalFormat rateFormat = new DecimalFormat("#.00");
			String formattedRate = rateFormat.format(hotel.lowPrice.doubleValue());
			if (getActivity() != null) {
				String rate = formattedRate + " " +  SettingsAPI.getCurrencySymbol(getActivity());
				snippet += ", " + rate;
			}
			intent.putExtra(HotelMapActivity.HOTEL_SNIPPET, snippet); 
			
			getActivity().startActivity(intent);
		}
	};

	private S3DrawableBackgroundLoader mPhotoLoader;

	
	void fillData() {
		DLog.i(TAG, "Filling data for hotel "+mHotelId);
		if (mPropertyDescription == null) {
			return;
		}
		mPropertyDescription.loadData("","text/html", "utf-8");
		mScrollView.setScrollY(0);
		
		final Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(mHotelId);
		if (hotel == null) {
			DLog.e(TAG, "showing hotel id "+mHotelId +" but not found");
			return;
		}
		String name = hotel.name;

		DLog.d(TAG, "Filling hotel data: "+mHotelName.getText()+ "  --> "+name);

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
		DLog.i(TAG, "hotel name = " + name);
		HotelInformation info = VHAApplication.EXTENDED_INFOS.get(VHAApplication.selectedHotel.hotelId);
		if (info == null) {
			DLog.e(TAG, "No extended info for hotel "+VHAApplication.selectedHotel.name);
			return;
		}

		
		final String photoWarning = "&lt;p&gt;\n&lt;b&gt;Note:&lt;/b&gt; It is the responsibility of the hotel chain and/or the"
			+ " individual property to ensure the accuracy of the photos displayed. \"Virtual Hotel Agent\" is"
			+ " not responsible for any inaccuracies in the photos. &lt;/p&gt;";
		Spanned marked_up = Html.fromHtml("<html><body>" + info.longDescription + photoWarning + "</body></html>");

		mPropertyDescription
			.loadData("<font color=\"black\">" + marked_up.toString() + "</font>", "text/html", "utf-8");
		
		mPropertyDescription.setBackgroundColor(Color.rgb(0xe3, 0xe3, 0xe3));

		if (hotel.tripAdvisorRatingUrl == null) {
			mTripAdvisorRatingBar.setVisibility(View.GONE);
		}
		else {
			S3DrawableBackgroundLoader loader = VHAApplication.thumbnailLoader;
			//BitmapDrawable placeHolder = (BitmapDrawable) getActivity().getResources().getDrawable(R.drawable.transparent_overlay);
			loader.loadDrawable(hotel.tripAdvisorRatingUrl, mTripAdvisorRatingBar_image, null, null);
			if (hotel.tripAdvisorReviewCount > 0) {
				mTripAdvisorRatingBar_text.setText("Based on "+hotel.tripAdvisorReviewCount+" ratings");
				mTripAdvisorRatingBar_text.setVisibility(View.VISIBLE);
			}
			else {
				mTripAdvisorRatingBar_text.setVisibility(View.GONE);
			}
			mTripAdvisorRatingBar.setVisibility(View.VISIBLE);
		}

		mStarRatingBar.setRating(hotel.starRating.floatValue());

		if (mPhotoLoader != null) {
			mPhotoLoader.Reset();
		}
		
		mVhaBmp = mEvaBmpCached;
		mHotelGalleryAdapter.clear();
		mHotelGalleryAdapter.setCyclic(false);
		mHotelGalleryAdapter.addBitmap(mVhaBmp);

		mHotelGallery.setAdapter(mHotelGalleryAdapter);


		//imageDownloader = new ImageDownloader(VHAApplication.HOTEL_PHOTOS, mHandlerFinish, mAllDoneHandler);
		mPhotoLoader = new S3DrawableBackgroundLoader(3, VHAApplication.HOTEL_PHOTOS);
		
		if (info.images.size() > 0 ) {
			DLog.i(TAG, "gallery showing "+info.images.size()+" imgs for hotel "+mHotelId);
			for (HotelImageTuple image : info.images) {
				mPhotoLoader.loadDrawable(image, false, null, null, new LoadedCallback() {
					
					@Override
					public void drawableLoaded(boolean success, BitmapDrawable drawable) {
						if (mVhaBmp != null) {
							mHotelGalleryAdapter.removeBitmap(mVhaBmp);
							mVhaBmp = null;
						}
						
						mHotelGalleryAdapter.addBitmap(drawable.getBitmap());
					}
				});
			}
		}

		
		boolean wifiAvailable = false;
		boolean poolAvailable = false;
		boolean breakfestAvailable = false;
		boolean parkingAvailable = false;

		if ((hotel.amenityMask & WIFI_AMENITY_CODE) == WIFI_AMENITY_CODE) {
			wifiAvailable = true;
		}

		if ((hotel.amenityMask & PARKING_AMENITY_CODE) == PARKING_AMENITY_CODE) {
			parkingAvailable = true;
		}

		if ((hotel.amenityMask & BREAKFEST_AMENITY_CODE) == BREAKFEST_AMENITY_CODE) {
			breakfestAvailable = true;
		}

		if ((hotel.amenityMask & POOL_AMENITY_CODE) == POOL_AMENITY_CODE) {
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
		mBookButton.setOnClickListener(mBookButtonListener);
		mBookButton.setEnabled(true);
		mBookButton.setText(R.string.select);
//		
		mMapButton.setOnClickListener(mMapButtonLisener);
		
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		
		if (isAdded()) {

			Intent intent = new Intent(this.getActivity(), ImageGalleryActivity.class);
			intent.putExtra(ImageGalleryActivity.PHOTO_INDEX, (int) id);
			intent.putExtra(ImageGalleryActivity.HOTEL_ID, mHotelId);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(), view, "fullscreen_image");
				startActivity(intent, options.toBundle());
			}
			else {
				startActivity(intent);
			}
		}
	}

	public void hotelSoldOut() {
		mBookButton.setText(R.string.sold_out);
		mBookButton.setEnabled(false);
	}
}