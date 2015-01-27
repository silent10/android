package com.virtual_hotel_agent.search.views.fragments;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
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

import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.SupplierType;
import com.evature.util.DLog;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.views.adapters.RoomListAdapter;

@SuppressLint("ValidFragment")
public class RoomsSelectFragement extends Fragment {//implements OnItemClickListener {

	private static final String TAG = "RoomsSelectFragement";
	private View mView = null;
	private ImageView mHotelImage;
	private TextView mHotelName;
	private TextView mNoticeText;
	private TextView mLocation;
	private RatingBar mStarRatingBar;
	private ExpandableListView mRoomListView;
	private RoomListAdapter mAdapter;
	private long mHotelId = -1;
	
//	static class DownloadedImg extends Handler {
//		private WeakReference<RoomsSelectFragement> fragmentRef;
//
//		public DownloadedImg(WeakReference<RoomsSelectFragement> fragmentRef) {
//			this.fragmentRef = fragmentRef;
//		}
//		
//		@Override
//		public void handleMessage(Message msg) {
//			if (fragmentRef != null) {
//				RoomsSelectFragement rsf = fragmentRef.get();
//				if (rsf != null) {
//					if (msg.arg1 == 0)
//						rsf.mHotelImage.setImageBitmap((Bitmap)msg.obj);
//					else {
//						rsf.mAdapter.notifyDataSetChanged();
//					}
//				}
//			}
//			super.handleMessage(msg);
//		}
//	}
//	
//	
//	private DownloadedImg mHandlerFinish; 
//	private ImageDownloader imageDownloader;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mView != null) {
			DLog.w(TAG, "Fragment initialized twice");
			((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		
		
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
		
		if (VHAApplication.selectedHotel == null) {
			DLog.e(TAG, "onCreateView - no selectedHotel");
		}
		else {
			changeHotelId(VHAApplication.selectedHotel.hotelId);
		}

		return mView;
	}

	private void fillData() {
		Hotel hotel = VHAApplication.HOTEL_ID_MAP.get(mHotelId);
		if (hotel == null) {
			DLog.e(TAG, "showing hotel id "+mHotelId +" but not found");
			return;
		}
		
		VHAApplication.fullResLoader.loadDrawable(hotel.mainHotelImageTuple, false, mHotelImage, null, null);
		
//		WeakReference<RoomsSelectFragement> fragmentRef = new WeakReference<RoomsSelectFragement>(this);
//		mHandlerFinish = new DownloadedImg(fragmentRef);
//		imageDownloader = new ImageDownloader(VHAApplication.HOTEL_PHOTOS, mHandlerFinish);
//		
		// if already loaded full image - no need for downloader thread
//		ArrayList<String> urls = new ArrayList<String>();
//		HotelInformation info = VHAApplication.EXTENDED_INFOS.get(hotel.hotelId);
//		if (info != null && info.images.size() > 0) {
//			for (HotelImageTuple photo : info.images) {
//				if (photo.mainUrl != null) {
//					urls.add(photo.mainUrl.toString());
//					break;
//				}
//			}
//		}
	
		List<HotelRoom> rooms = VHAApplication.HOTEL_ROOMS.get(hotel.hotelId);
		if (rooms != null && rooms.size() > 0) {
			for (HotelRoom rd : rooms) {
				if (rd.images != null && rd.images.length > 0) {
					//urls.add(rd.imageUrls[0]);
					VHAApplication.fullResLoader.loadDrawable(rd.images[0], false, mHotelImage, null, null);
				}
			}
		}
//		Bitmap fullImage = VHAApplication.HOTEL_PHOTOS.get(urls.get(0));
//		if (fullImage != null) {
//			Log.d(TAG, "Showing full Image from cache");
//			mHotelImage.setImageBitmap(fullImage);
//		}
		//imageDownloader.startDownload(urls);

		String name = hotel.name;

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
		
		
		mLocation.setText(hotel.address.city+", "+hotel.address.countryCode);
		
		String disclaimer = "";
		if (hotel.supplierType != null && hotel.supplierType == SupplierType.EXPEDIA) {
			disclaimer = getText(R.string.room_price_disclaimer).toString();
		}
		else {
			// http://developer.ean.com/docs/launch-requirements/agency-hotels/#roomratedisclaimer
			disclaimer = getText(R.string.room_price_disclaimer_hotel_collect).toString();
			if (VHAApplication.numberOfAdults > 2 || VHAApplication.childAges.size() > 0) {
				disclaimer += " Carefully review the room descriptions and rate rules to ensure the room you select can "+ 
								"accommodate your entire party.";
			}
		}
		
		boolean hasNoRefund = false;
		if (rooms != null) {
			for (HotelRoom room : rooms) {
				if (room.rate != null && room.rate.nonRefundable) {
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

		

		mStarRatingBar.setRating((float)hotel.starRating.floatValue());

		mRoomListView = (ExpandableListView)mView.findViewById(R.id.roomListView);
		

		mAdapter = new RoomListAdapter(getActivity(), hotel.hotelId, rooms);
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
			Toast.makeText(getActivity(), R.string.no_rooms ,Toast.LENGTH_LONG).show();			
		}
	}

	public void changeHotelId(long hotelId) {
		if (hotelId == -1)
			return;
		
		DLog.i(TAG, "Setting hotelId to "+hotelId+", was "+mHotelId);
		if (mHotelId == hotelId) {
			return;
		}
		VHAApplication.selectedRoom = null;
		mHotelId = hotelId;
		fillData();
	}


}
