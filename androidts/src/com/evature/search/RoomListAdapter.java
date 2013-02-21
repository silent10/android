package com.evature.search;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evature.search.expedia.HotelData;

public class RoomListAdapter extends BaseAdapter {

	HotelData mHotel;
	private LayoutInflater mInflater;
	private Context mParent;
	
		
	RoomListAdapter(Context context, HotelData hotel)
	{	
		mInflater = LayoutInflater.from(context);
	    mParent = context;
		mHotel = hotel;
	}
	
	

	@Override
	public int getCount() {		
		return mHotel.mSummary.roomDetails.length;
	}

	@Override
	public Object getItem(int position) {		
		return mHotel.mSummary.roomDetails[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder holder;
		 if (convertView == null) 
		 {
			 convertView = mInflater.inflate(R.layout.room, null);
			 holder = new ViewHolder();
			 
			 holder.promo = (TextView)convertView.findViewById(R.id.promo);
			 holder.rate = (TextView)convertView.findViewById(R.id.rate);
			 holder.details = (TextView)convertView.findViewById(R.id.details);
			 convertView.setTag(holder);				 						 				 				 
		 } else {
			 holder = (ViewHolder) convertView.getTag();
		 }
		 
		 holder.roomIndex = position;
		 
		 Spanned spannedName = Html.fromHtml(mHotel.mSummary.roomDetails[position].mRoomTypeDescription);
		 
		 String name = spannedName.toString();
		  
		 Display display = ((WindowManager) mParent.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		 /* Now we can retrieve all display-related infos */
//		 int width = display.getWidth();
//		 int height = display.getHeight();		 
//		 
//		 int maxNameLength = width/12-3;
//		 
//		 if(name.length()>maxNameLength)
//		 {
//			 name = (name.subSequence(0, maxNameLength)).toString();
//			 name+="...";
//		 }
//		 		 		 		 
		 holder.promo.setText(name);

		 
		 if(mHotel.mSummary.roomDetails[holder.roomIndex].mRateInfo!=null)
		 {
			 double rateInfo = mHotel.mSummary.roomDetails[holder.roomIndex].mRateInfo.mChargableRateInfo.mAverageBaseRate;
			 int wholePart = (int)rateInfo;
			 int fraction = (int)((rateInfo-wholePart)*100.);
		 
			 double twoDigitsRate = (double)wholePart+((double)fraction)/100.;
		 
			 String rate = EvaSettingsAPI.getCurrencySymbol(mParent) + " " + twoDigitsRate;
		 
			 holder.rate.setText(rate);
		 }
		 else
		 {
			 holder.rate.setText("NA");
		 }
		 
		 holder.details.setText(mHotel.mSummary.roomDetails[holder.roomIndex].mPromoDescription);
		
		 return convertView;
	}

	

	static class ViewHolder
	{
		TextView promo;
		TextView rate;
		TextView details;
		int roomIndex;
	}

}
