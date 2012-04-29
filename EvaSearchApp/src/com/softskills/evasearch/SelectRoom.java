package com.softskills.evasearch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.softskills.evasearch.database.HotelData;

public class SelectRoom extends Activity implements OnItemClickListener {

	private ImageView mHotelImage;
	private TextView mHotelName;
	private TextView mLocation;
	private RatingBar mStarRatingBar;	
	HotelData mHotelData;
	ListView mRoomListView;
	int mHotelIndex;
	RoomListAdapter mAdapter;


	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		if(EvaSearchApplication.getDb()==null)
		{
			super.onCreate(savedInstanceState);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.select_room);

		Intent intent = getIntent();

		Bundle extra = intent.getExtras();

		mHotelIndex = extra.getInt("hotelIndex");

		mHotelImage = (ImageView)findViewById(R.id.hotelThumbnail);

		mHotelName = (TextView)findViewById(R.id.hotelName);

		mLocation = (TextView)findViewById(R.id.location);

		mStarRatingBar = (RatingBar)findViewById(R.id.starRating);

		mHotelData = EvaSearchApplication.getDb().mHotelData[mHotelIndex];

		Bitmap hotelBitmap = EvaSearchApplication.getDb().mImagesMap.get(mHotelData.mSummary.mThumbNailUrl);
		if(hotelBitmap!=null)
		{
			mHotelImage.setImageBitmap(hotelBitmap);
		}

		Spanned spannedName = Html.fromHtml(mHotelData.mSummary.mName);

		String name = spannedName.toString();

		Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

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

		mRoomListView = (ListView)findViewById(R.id.roomListView);
		
		mAdapter = new RoomListAdapter(this,mHotelData);
		mRoomListView.setAdapter( mAdapter );
		
		mRoomListView.setOnItemClickListener(this);

		if(mAdapter.getCount()==0)
		{
			Toast.makeText(this,"No rooms available for the selected dates",3000).show();
			finish();
		}
		
		
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		String newUrl = mHotelData.mSummary.roomDetails[arg2].buildTravelUrl(mHotelData.mSummary.mHotelId, 
				mHotelData.mSummary.mCurrentRoomDetails.mArrivalDate, 
				mHotelData.mSummary.mCurrentRoomDetails.mDepartureDate, 
				EvaSearchApplication.getDb().mNumberOfAdultsParam,
				mHotelData.mSummary.mCurrentRoomDetails.mRateKey);
		//String url = mHotelData.mSummary.roomDetails[arg2].mDeepLink;
		String s = Html.fromHtml(newUrl).toString();
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(s));
		startActivity(i);
	}

}
