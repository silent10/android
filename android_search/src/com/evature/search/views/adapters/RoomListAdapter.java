package com.evature.search.views.adapters;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Paint;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.evature.search.EvaSettingsAPI;
import com.evature.search.R;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.models.expedia.RoomDetails;

public class RoomListAdapter extends BaseAdapter {

	HotelData mHotel;
	private LayoutInflater mInflater;
	private Context mParent;
	
	static final DecimalFormat formatter = new DecimalFormat("#.##");
		
	public RoomListAdapter(Context context, HotelData hotel)
	{	
		mInflater = LayoutInflater.from(context);
	    mParent = context;
		mHotel = hotel;
	}
	
	

	@Override
	public int getCount() {		
		if (mHotel.mSummary.roomDetails == null) {
			return 0;
		}
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
			 holder.full_rate_nights = (TextView)convertView.findViewById(R.id.per_night_full);
			 holder.full_rate = (TextView)convertView.findViewById(R.id.full_rate);
			 holder.full_rate.setPaintFlags(holder.full_rate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			 holder.promo_rate = (TextView)convertView.findViewById(R.id.promo_rate);
			 holder.details = (TextView)convertView.findViewById(R.id.details);
			 convertView.setTag(holder);				 						 				 				 
		 } else {
			 holder = (ViewHolder) convertView.getTag();
		 }
		 
		 holder.roomIndex = position;
		 
		 RoomDetails roomDetails = mHotel.mSummary.roomDetails[position];
		 if (roomDetails.mRoomTypeDescription != null) {
			 Spanned spannedName = Html.fromHtml(roomDetails.mRoomTypeDescription);
			 
			 String name = spannedName.toString();
			  
	
			 /* Now we can retrieve all display-related infos */
	//		 Display display = ((WindowManager) mParent.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
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
		 }

		 
		 if(roomDetails.mRateInfo!=null)
		 {
			 double fullRate = roomDetails.mRateInfo.mChargableRateInfo.mAverageBaseRate;
			 double promoRate = roomDetails.mRateInfo.mChargableRateInfo.mAverageRate;
			 String fullRateStr = formatter.format(fullRate);
			 String promoRateStr = formatter.format(promoRate);
			 
			 String dollar = EvaSettingsAPI.getCurrencySymbol(mParent)+" ";
			 if (promoRate > 0 && promoRate < fullRate) {
				 holder.full_rate.setText(dollar + fullRateStr);
				 holder.full_rate.setVisibility(View.VISIBLE);
				 holder.full_rate_nights.setVisibility(View.VISIBLE);
				 holder.promo_rate.setText(dollar + promoRateStr);
			 }
			 else {
				 holder.full_rate.setVisibility(View.GONE);
				 holder.full_rate_nights.setVisibility(View.GONE);
				 holder.promo_rate.setText(dollar+fullRateStr);
				 
			 }
		 }
		 else
		 {
			 holder.full_rate.setVisibility(View.GONE);
			 holder.promo_rate.setText("NA");
		 }
		 
		 holder.details.setText(roomDetails.mPromoDescription);
		
		 return convertView;
	}

	

	static class ViewHolder
	{
		TextView full_rate_nights;
		TextView promo;
		TextView full_rate;
		TextView promo_rate;
		TextView details;
		int roomIndex;
	}

}
