package com.virtual_hotel_agent.search.views.fragments;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import roboguice.fragment.RoboFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.models.expedia.ExpediaRequestParameters;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelDetails.HotelImage;
import com.virtual_hotel_agent.search.models.expedia.RoomDetails;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.util.ImageDownloader;
import com.virtual_hotel_agent.search.views.adapters.RoomListAdapter;

@SuppressLint("ValidFragment")
public class RoomsSelectFragement extends RoboFragment {//implements OnItemClickListener {

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(HOTEL_INDEX, mHotelIndex);
		super.onSaveInstanceState(outState);
	}

	private static final String HOTEL_INDEX = "HotelIndex";
	private static final String TAG = "RoomsSelectFragement";
	private View mView;
	private ImageView mHotelImage;
	private TextView mHotelName;
	private TextView mNoticeText;
	private TextView mLocation;
	private RatingBar mStarRatingBar;
	private HotelData mHotelData;
	private ExpandableListView mRoomListView;
	private RoomListAdapter mAdapter;

	static class DownloadedImg extends Handler {
		private WeakReference<RoomsSelectFragement> fragmentRef;

		public DownloadedImg(WeakReference<RoomsSelectFragement> fragmentRef) {
			this.fragmentRef = fragmentRef;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (fragmentRef != null) {
				RoomsSelectFragement rsf = fragmentRef.get();
				if (rsf != null) {
					if (msg.arg1 == 0)
						rsf.mHotelImage.setImageBitmap((Bitmap)msg.obj);
					else {
						rsf.mAdapter.notifyDataSetChanged();
					}
				}
			}
			super.handleMessage(msg);
		}
	}
	
	
	private DownloadedImg mHandlerFinish; 
	private ImageDownloader imageDownloader;



	public RoomsSelectFragement()
	{
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ExpediaRequestParameters rp = MyApplication.getExpediaRequestParams();
		if (rp == null) {
			MainActivity.LogError(TAG, "HotelDetailFragment onCreateView - no RequestParams");
			return super.onCreateView(inflater, container, savedInstanceState);
		}
		
		mHotelIndex = rp.getHotelId();


//		if(savedInstanceState!=null) {
//			mHotelIndex=savedInstanceState.getInt(HOTEL_INDEX);
//		}
		
		Context context = RoomsSelectFragement.this.getActivity();
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "Rooms display")
				    .build()
				);

		
		mView = inflater.inflate(R.layout.fragment_select_hotel_room,container,false);

		mHotelImage = (ImageView)mView.findViewById(R.id.hotelThumbnail);

		mHotelName = (TextView)mView.findViewById(R.id.hotelName);
		mNoticeText = (TextView)mView.findViewById(R.id.noticeText);
		mNoticeText.setMovementMethod(ScrollingMovementMethod.getInstance());

		mLocation = (TextView)mView.findViewById(R.id.location);

		mStarRatingBar = (RatingBar)mView.findViewById(R.id.starRating);
		

		fillData();
		

		return mView;
	}

	private void fillData() {
		XpediaDatabase db = MyApplication.getDb();
		if (db == null || db.mHotelData == null ||  mHotelIndex >= db.mHotelData.length) {
			MainActivity.LogError(TAG, "DB error fetching rooms for hotel "+mHotelIndex);
			return;
		}
		mHotelData = db.mHotelData[mHotelIndex];
		
		Drawable drawable = S3DrawableBackgroundLoader.getInstance().getDrawableFromCache(mHotelData.mSummary.mThumbNailUrl);
		if (drawable != null) {
			Log.d(TAG, "Showing thumbnail from cache");
			mHotelImage.setImageDrawable(drawable);
		}
		
		WeakReference<RoomsSelectFragement> fragmentRef = new WeakReference<RoomsSelectFragement>(this);
		mHandlerFinish = new DownloadedImg(fragmentRef);
		imageDownloader = new ImageDownloader(db.getImagesCache(), mHandlerFinish);
		
		// if already loaded full image - no need for downloader thread
		if (mHotelData != null) {
			ArrayList<String> urls = new ArrayList<String>();
			if (mHotelData.mDetails != null && mHotelData.mDetails.hotelImages != null) {
				for (HotelImage hotel : mHotelData.mDetails.hotelImages) {
					if (hotel.url != null) {
						urls.add(hotel.url);
						break;
					}
				}
			}
		
			if (mHotelData.mSummary != null && mHotelData.mSummary.roomDetails != null) {
				for (RoomDetails rd : mHotelData.mSummary.roomDetails) {
					if (rd.mImageUrls != null) {
						for (String url : rd.mImageUrls) {
							urls.add(url);
							break;
						}
					}
				}
			}
			Bitmap fullImage = db.getImagesCache().get(urls.get(0));
			if (fullImage != null) {
				Log.d(TAG, "Showing full Image from cache");
				mHotelImage.setImageBitmap(fullImage);
			}
			imageDownloader.startDownload(urls);
		}

		Spanned spannedName = Html.fromHtml(mHotelData.mSummary.mName);

		String name = spannedName.toString();

//				Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		/* Now we can retrieve all display-related infos */
		//		int width = display.getWidth();
		//		int height = display.getHeight();		 
		//
		//		int maxNameLength = (width-90)/18-3;
		//
		//		if(name.length()>maxNameLength)
		//		{
		//			name = (name.subSequence(0, maxNameLength)).toString();
		//			name+="...";
		//		}

		mHotelName.setText(name);
		
		
		mLocation.setText(mHotelData.mSummary.mCity+","+mHotelData.mSummary.mCountryCode);
		
		String disclaimer = "";
		if (mHotelData.mSummary.mSupplierType != null && mHotelData.mSummary.mSupplierType.equals("E")) {
			disclaimer = getText(R.string.room_price_disclaimer).toString();
		}
		else {
			// http://developer.ean.com/docs/launch-requirements/agency-hotels/#roomratedisclaimer
			disclaimer = getText(R.string.room_price_disclaimer_hotel_collect).toString();
			ExpediaRequestParameters rp = MyApplication.getExpediaRequestParams();
			if (rp.mNumberOfAdultsParam > 2 || rp.getNumberOfChildrenParam() > 0) {
				disclaimer += " Carefully review the room descriptions and rate rules to ensure the room you select can "+ 
								"accommodate your entire party.";
			}
		}
		
		boolean hasNoRefund = false;
		if (mHotelData.mSummary.roomDetails != null) {
			for (RoomDetails room : mHotelData.mSummary.roomDetails) {
				if (room.mRateInfo != null && room.mRateInfo.mNonRefundable) {
					hasNoRefund = true;
					break;
				}
			}
		}
		if (hasNoRefund) {
			mNoticeText.setText("Note: Highlighted rooms are Non Refundable");
			mNoticeText.setVisibility(View.VISIBLE);
		}
		else {
			mNoticeText.setText("");
			mNoticeText.setVisibility(View.GONE);
		}

		

		mStarRatingBar.setRating((float)mHotelData.mSummary.mHotelRating);

		mRoomListView = (ExpandableListView)mView.findViewById(R.id.roomListView);
		

		mAdapter = new RoomListAdapter(getActivity(),mHotelData);
		mAdapter.setDisclaimer(disclaimer);

		mRoomListView.setAdapter( mAdapter );
		
		mRoomListView.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
		        int len = mAdapter.getGroupCount();           
		        for(int i=0; i<len; i++)
		        {
		            if(i != groupPosition)
		            {
		            	mRoomListView.collapseGroup(i);
		            }
		        }
		        mRoomListView.setSelectionFromTop(groupPosition, 8);
		    }
		});

		// collapse all
		int len = mAdapter.getGroupCount();           
        for(int i=0; i<len; i++)
        {
        	mRoomListView.collapseGroup(i);
        }

		if(len==0)
		{
			Toast.makeText(getActivity(),"No rooms available for the selected dates",Toast.LENGTH_LONG).show();			
		}
	}

	private int mHotelIndex;

	public void changeHotelId(int hotelIndex) {
		mHotelIndex = hotelIndex;
		fillData();
	}


}
