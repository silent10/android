package com.evature.search.views.fragments;

import roboguice.fragment.RoboFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.R.id;
import com.evature.search.R.layout;
import com.evature.search.controllers.activities.EvaCheckoutActivity;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.XpediaProtocol;
import com.evature.search.views.adapters.RoomListAdapter;

@SuppressLint("ValidFragment")
public class RoomsSelectFragement extends RoboFragment implements OnItemClickListener {

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(HOTEL_INDEX,mHotelIndex);
		super.onSaveInstanceState(outState);
	}

	private static final String HOTEL_INDEX = "HotelIndex";
	private View mView;
	private ImageView mHotelImage;
	private TextView mHotelName;
	private TextView mLocation;
	private RatingBar mStarRatingBar;
	private HotelData mHotelData;
	private ListView mRoomListView;
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

					if(mHotelData.mDetails.hotelImages[0]!=null)
					{
						if(mHotelData.mDetails.hotelImages[0].url!=null)
						{
							bmp = XpediaProtocol.download_Image(mHotelData.mDetails.hotelImages[0].url);
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

			mView = inflater.inflate(R.layout.select_room,container,false);

			mHotelImage = (ImageView)mView.findViewById(R.id.hotelThumbnail);

			mHotelName = (TextView)mView.findViewById(R.id.hotelName);

			mLocation = (TextView)mView.findViewById(R.id.location);

			mStarRatingBar = (RatingBar)mView.findViewById(R.id.starRating);
			
			
			if(savedInstanceState!=null)
			{
				mHotelIndex=savedInstanceState.getInt(HOTEL_INDEX);
			}

			mHotelData = MyApplication.getDb().mHotelData[mHotelIndex];

			Bitmap hotelBitmap = MyApplication.getDb().mImagesMap.get(mHotelData.mSummary.mThumbNailUrl);
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

			mStarRatingBar.setRating((float)mHotelData.mSummary.mHotelRating);

			mRoomListView = (ListView)mView.findViewById(R.id.roomListView);

			mAdapter = new RoomListAdapter(getActivity(),mHotelData);
			mRoomListView.setAdapter( mAdapter );

			mRoomListView.setOnItemClickListener(this);

			if(mAdapter.getCount()==0)
			{
				Toast.makeText(getActivity(),"No rooms available for the selected dates",3000).show();			
			}

			startImageDownload();




			return mView;
		}

		EvaCheckoutActivity mEvaCheckoutActivity;
		private int mHotelIndex;

		public RoomsSelectFragement(EvaCheckoutActivity eca, int hotelIndex) {
			mEvaCheckoutActivity = eca;
			mHotelIndex = hotelIndex;
		}

		public static RoomsSelectFragement newInstance(EvaCheckoutActivity eca,int hotelIndex) {		
			return new RoomsSelectFragement(eca,hotelIndex);
		}

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			String newUrl = mHotelData.mSummary.roomDetails[arg2].buildTravelUrl(mHotelData.mSummary.mHotelId, 
					mHotelData.mSummary.mCurrentRoomDetails.mArrivalDate, 
					mHotelData.mSummary.mCurrentRoomDetails.mDepartureDate, 
					MyApplication.getDb().mNumberOfAdultsParam,
					mHotelData.mSummary.mCurrentRoomDetails.mRateKey);
			//String url = mHotelData.mSummary.roomDetails[arg2].mDeepLink;
			String s = Html.fromHtml(newUrl).toString();
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse(s));
			startActivity(i);

		}


}
