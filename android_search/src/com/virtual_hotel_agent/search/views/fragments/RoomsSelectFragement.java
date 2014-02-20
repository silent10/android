package com.virtual_hotel_agent.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.models.expedia.ExpediaRequestParameters;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.RoomDetails;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;
import com.virtual_hotel_agent.search.views.adapters.RoomListAdapter;

@SuppressLint("ValidFragment")
public class RoomsSelectFragement extends RoboFragment {//implements OnItemClickListener {

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(HOTEL_INDEX,mHotelIndex);
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
	private Bitmap mEvaBmp = null;

	private Handler mHandlerFinish = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			mHotelImage.setImageBitmap((Bitmap)msg.obj);		
			super.handleMessage(msg);
		}};

		private void startImageDownload() {
			Thread imageDownloadThread = new Thread()
			{

				@Override
				public void run()
				{	
					Bitmap bmp = null;

					if(mHotelData.mDetails != null && mHotelData.mDetails.hotelImages[0]!=null)
					{
						if(mHotelData.mDetails.hotelImages[0].url!=null)
						{
							bmp = XpediaProtocolStatic.download_Image(mHotelData.mDetails.hotelImages[0].url);
						}							
					}

					if(bmp!=null)
					{
						Message message = mHandlerFinish.obtainMessage();
						message.obj = bmp;
						mHandlerFinish.sendMessage(message);
					}

				}
			};
			imageDownloadThread.start();
		}

		public RoomsSelectFragement()
		{}


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			Context context = RoomsSelectFragement.this.getActivity();
			Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
			if (defaultTracker != null) 
				defaultTracker.send(MapBuilder
					    .createAppView()
					    .set(Fields.SCREEN_NAME, "Rooms display")
					    .build()
					);

			
			mView = inflater.inflate(R.layout.select_hotel_room,container,false);

			mHotelImage = (ImageView)mView.findViewById(R.id.hotelThumbnail);

			mHotelName = (TextView)mView.findViewById(R.id.hotelName);
			mNoticeText = (TextView)mView.findViewById(R.id.noticeText);
			mNoticeText.setMovementMethod(ScrollingMovementMethod.getInstance());

			mLocation = (TextView)mView.findViewById(R.id.location);

			mStarRatingBar = (RatingBar)mView.findViewById(R.id.starRating);
			
			
			if(savedInstanceState!=null)
			{
				mHotelIndex=savedInstanceState.getInt(HOTEL_INDEX);
			}

			XpediaDatabase db = MyApplication.getDb();
			if (db != null && db.mHotelData != null &&  mHotelIndex < db.mHotelData.length) {
				mHotelData = db.mHotelData[mHotelIndex];
				
				Bitmap hotelBitmap = db.mImagesMap.get(mHotelData.mSummary.mThumbNailUrl);
				if(hotelBitmap!=null)
				{
					mHotelImage.setImageBitmap(hotelBitmap);
				}
	
				Spanned spannedName = Html.fromHtml(mHotelData.mSummary.mName);
	
				String name = spannedName.toString();
	
				Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	
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
	
				//mRoomListView.setOnItemClickListener(this);
	
				if(mAdapter.getGroupCount()==0)
				{
					Toast.makeText(getActivity(),"No rooms available for the selected dates",Toast.LENGTH_LONG).show();			
				}
	
				startImageDownload();

			}


			return mView;
		}

		private int mHotelIndex;

		public RoomsSelectFragement(int hotelIndex) {
			mHotelIndex = hotelIndex;
		}

		public static RoomsSelectFragement newInstance(int hotelIndex) {		
			return new RoomsSelectFragement(hotelIndex);
		}



}
