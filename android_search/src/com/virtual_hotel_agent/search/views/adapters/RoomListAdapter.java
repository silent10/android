package com.virtual_hotel_agent.search.views.adapters;

import java.text.DecimalFormat;

import roboguice.event.EventManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.ImageGalleryActivity;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.events.RoomSelectedEvent;
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.RoomDetails;
import com.virtual_hotel_agent.search.models.expedia.Surcharge;
import com.virtual_hotel_agent.search.models.expedia.ValueAdd;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;

public class RoomListAdapter extends BaseExpandableListAdapter {

	HotelData mHotel;
	private LayoutInflater mInflater;
	private Context mParent;
	static final DecimalFormat formatter = new DecimalFormat("#.##");
	private String disclaimer;
	private Bitmap mEvaBmpCached;
	protected static final String TAG = "RoomListAdapter";
	private int selectedColor;
	private int selectedNonRefundColor;
	private EventManager mEventManager;
		
	public RoomListAdapter(Context context, HotelData hotel, EventManager eventManager)
	{	
		mInflater = LayoutInflater.from(context);
	    mParent = context;
		mHotel = hotel;
		Resources resources = context.getResources();
		selectedColor = resources.getColor(R.color.selected_room_list_item);
		selectedNonRefundColor = resources.getColor(R.color.selected_non_refundable_room_list_item);
		disclaimer = "";
		mEventManager = eventManager;
		mEvaBmpCached = BitmapFactory.decodeResource(resources, R.drawable.hotel72);
	}
	
	public void setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
	}

	private View fillerView(View view, ViewGroup parent) {
		if (view == null) {
			view = mInflater.inflate(R.layout.row_filler, parent, false);
			view.setClickable(false);
			view.setEnabled(false);
		}
		return view;
	}

	
	@Override
	public View getGroupView(int position, boolean isExpanded, View convertView, ViewGroup parent) {
		if (getGroupType(position) == 1) {
			return fillerView(convertView, parent);
		}
		
		 ViewHolder holder;
		 if (convertView == null) 
		 {
			 convertView = mInflater.inflate(R.layout.room_list_item, null);
			 holder = new ViewHolder();
			 
			 holder.photo = (ImageView)convertView.findViewById(R.id.roomImage);
			 holder.promo = (TextView)convertView.findViewById(R.id.promo);
			 holder.full_rate_nights = (TextView)convertView.findViewById(R.id.per_night_full);
			 holder.full_rate = (TextView)convertView.findViewById(R.id.full_rate);
			 holder.full_rate.setPaintFlags(holder.full_rate.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			 holder.promo_rate = (TextView)convertView.findViewById(R.id.promo_rate);
			 holder.details = (TextView)convertView.findViewById(R.id.details);
			 holder.container = convertView.findViewById(R.id.roomListItem);
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
			 
			 if (roomDetails.mImageUrls != null && roomDetails.mImageUrls.length > 0) {
				 XpediaDatabase db = MyApplication.getDb();
				 LruCache<String, Bitmap> cache = db != null ? db.getImagesCache() : null;
				 Bitmap cachedPhoto = cache != null ? cache.get(roomDetails.mImageUrls[0]) : null;
				 if (cachedPhoto != null) {
					 holder.photo.setImageBitmap(cachedPhoto);
				 }
				 else {
					 holder.photo.setImageBitmap(mEvaBmpCached);
				 }
				 holder.photo.setVisibility(View.VISIBLE);
			 }
			 else {
				 holder.photo.setVisibility(View.GONE);
			 }
		 }

		 
		 if(roomDetails.mRateInfo!=null)
		 {
			 if (roomDetails.mRateInfo.mNonRefundable) {
				 if (isExpanded) {
					 holder.container.setBackgroundResource(R.drawable.non_refundable_background_selected);
					 holder.photo.setVisibility(View.GONE);
				 }
				 else {
					 holder.container.setBackgroundResource(R.drawable.non_refundable_background);
				 }
			 }
			 else {
				 if (isExpanded) {
					 holder.container.setBackgroundResource(R.drawable.hotel_background_selected);
					 holder.photo.setVisibility(View.GONE);
				 }
				 else {
					 holder.container.setBackgroundResource(R.drawable.hotel_background);
				 }
			 }
			 double fullRate = roomDetails.mRateInfo.mChargableRateInfo.mAverageBaseRate;
			 double promoRate = roomDetails.mRateInfo.mChargableRateInfo.mAverageRate;
			 String fullRateStr = formatter.format(fullRate);
			 String promoRateStr = formatter.format(promoRate);
			 
			 String dollar = SettingsAPI.getCurrencySymbol(mParent)+" ";
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
			 
			 if (roomDetails.mRateInfo.mPromoDescription != null && roomDetails.mRateInfo.mPromoDescription.equals("") == false) {
				 holder.details.setText(roomDetails.mRateInfo.mPromoDescription);
				 holder.details.setVisibility(View.VISIBLE);
			 }
			 else {
				 holder.details.setVisibility(View.GONE);
			 }
		 }
		 else
		 {
			 holder.full_rate.setVisibility(View.GONE);
			 holder.promo_rate.setText("NA");
			 holder.details.setText("");
			 holder.details.setVisibility(View.GONE);
			 holder.container.setBackgroundResource(R.drawable.hotel_background);
		 }
		 
		
		 return convertView;
	}

	

	static class ViewHolder
	{
		TextView full_rate_nights;
		ImageView photo;
		TextView promo;
		TextView full_rate;
		TextView promo_rate;
		TextView details;
		View container;
		int roomIndex;
	}



	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mHotel.mSummary.roomDetails[groupPosition];
	}



	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return groupPosition;
	}



	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		if (convertView == null) 
		{
			convertView = mInflater.inflate(R.layout.room_list_item_expanded, null);
		}

		final RoomDetails room = mHotel.mSummary.roomDetails[groupPosition];
		boolean nonRefundable = (room.mRateInfo != null && room.mRateInfo.mNonRefundable);
		
		
		View photoContainer = convertView.findViewById(R.id.roomImage_container);
		ImageView photoHolder = (ImageView) convertView.findViewById(R.id.roomImage);
		if (room.mImageUrls != null && room.mImageUrls.length > 0) {
			 XpediaDatabase db = MyApplication.getDb();
			 LruCache<String, Bitmap> cache = db != null ? db.getImagesCache() : null;
			 Bitmap cachedPhoto = cache != null ? cache.get(room.mImageUrls[0]) : null;
			 if (cachedPhoto != null) {
				 photoHolder.setImageBitmap(cachedPhoto);
			 }
			 else {
				 photoHolder.setImageBitmap(mEvaBmpCached);
			 }
			 photoContainer.setVisibility(View.VISIBLE);
			 photoHolder.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(mParent, ImageGalleryActivity.class);
					intent.putExtra(ImageGalleryActivity.PHOTO_URLS, room.mImageUrls);
					Spanned spannedName = Html.fromHtml(room.mRoomTypeDescription);
					String name = spannedName.toString();
					 
					intent.putExtra(ImageGalleryActivity.TITLE, name);
					mParent.startActivity(intent);
				}
			});
		 }
		 else {
			 photoContainer.setVisibility(View.GONE);
		 }
		
		
		if (nonRefundable) {
			convertView.setBackgroundColor(selectedNonRefundColor);
		}
		else {
			convertView.setBackgroundColor(selectedColor);
		}
		WebView desc = (WebView) convertView.findViewById(R.id.roomDescription);
		StringBuilder text  = new StringBuilder("&lt;html&gt;&lt;head&gt;&lt;meta http-equiv=\"Content-type\" content=\"text/html;charset=UTF-8\"&gt;"
				+ "&lt;meta charset=\"UTF-8\"&gt;&lt;/head&gt;&lt;body&gt;&lt;font color=\"black\"&gt;");
		if (disclaimer.equals("") == false)
			text.append("&lt;p&gt;"+disclaimer+"&lt;/p&gt;");
		if (room.mRateInfo != null && room.mRateInfo.mPromoDetailText != null) {
			text.append("&lt;p&gt; "+TextUtils.htmlEncode(room.mRateInfo.mPromoDetailText) + "&lt;/p&gt; ");
		}
		if (room.mRoomDescription != null) {
			text.append("&lt;p&gt; &lt;b&gt;Room Description &lt;/b&gt; &lt;br&gt;");
			text.append(TextUtils.htmlEncode(room.mRoomDescription) + "&lt;/p&gt; ");
		}
		if (room.mValueAdds != null && room.mValueAdds.length > 0) {
			text.append("&lt;b&gt;You also get:&lt;/b&gt; &lt;ul&gt;");
			for (ValueAdd va: room.mValueAdds) {
				text.append("&lt;li&gt;")
					.append(va.mDescription)
					.append("&lt;/li&gt;");
			}
			text.append("&lt;/ul&gt;");
		}
		if (room.mRateInfo != null && room.mRateInfo.mChargableRateInfo != null  &&
				room.mRateInfo.mChargableRateInfo.mSurcharges != null && room.mRateInfo.mChargableRateInfo.mSurcharges.length > 0) {
			String dollar = " "+SettingsAPI.getCurrencySymbol(mParent);
			text.append("&lt;b&gt;Surcharges&lt;/b&gt; &lt;ul&gt;");
			for (Surcharge surcharge: room.mRateInfo.mChargableRateInfo.mSurcharges) {
				text.append("&lt;li&gt;")
					.append(surcharge.mType)
					.append(": ")
					.append(surcharge.mAmount)
					.append(dollar)
					.append("&lt;/li&gt;");
			}
			text.append("&lt;/ul&gt;");
		}
		if (room.mCheckInInstructions != null && room.mCheckInInstructions.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Check In Instructions &lt;/b&gt; &lt;br&gt;")
				.append(room.mCheckInInstructions)
				.append("&lt;/p&gt;");
		}
		if (room.mRateInfo != null &&  room.mRateInfo.mCancelllationPolicy != null && room.mRateInfo.mCancelllationPolicy.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Cancelation Policy&lt;/b&gt; &lt;br&gt;")
				.append(room.mRateInfo.mCancelllationPolicy)
				.append("&lt;/p&gt;");
		}
		if (room.mPolicy != null && room.mPolicy.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Policy&lt;/b&gt; &lt;br&gt;")
				.append(room.mPolicy)
				.append("&lt;/p&gt;");
		}
		if (room.mOtherInformation != null && room.mOtherInformation.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Other Information&lt;/b&gt; &lt;br&gt;");
			text.append(room.mOtherInformation)
				.append("&lt;br&gt;");
		}
		
		text.append("&lt;p&gt; &lt;b&gt; Refundable: &lt;/b&gt; ").append(nonRefundable ? "No" : "Yes").append("&lt;br&gt;");
		text.append("&lt;p&gt; &lt;b&gt; Smoking Policy: &lt;/b&gt; ").append(room.mSmoking).append("&lt;br&gt;");
		if (room.mRateInfo != null) {
			text.append("&lt;p&gt; &lt;b&gt; Guarantee Required: &lt;/b&gt; ").append(room.mRateInfo.mGuaranteeRequired ? "Yes" : "No").append("&lt;br&gt;");
			text.append("&lt;p&gt; &lt;b&gt; Deposit Required: &lt;/b&gt; ").append(room.mRateInfo.mDepositRequired ? "Yes" : "No").append("&lt;br&gt;");
		}
		text.append("&lt;/p&gt;");
		
		text.append("&lt;/font&gt;&lt;/body&gt;&lt;/html&gt;");
		
		Spanned marked_up = Html.fromHtml(text.toString());

		desc.loadData(marked_up.toString(), "text/html; charset=UTF-8", "utf-8");
		
		Tracker defaultTracker = GoogleAnalytics.getInstance(mParent).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("ui_action", "room_expanded", String.valueOf(mHotel.mSummary.mHotelId), (long)groupPosition)
				    .build()
				   );
		
		Button bookButton = (Button) convertView.findViewById(R.id.buttonChooseRoom);
		bookButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mEventManager.fire(new RoomSelectedEvent(room, mHotel));
			}
		});
		
		return convertView;
	}



	@Override
	public int getChildrenCount(int groupPosition) {
		if (getGroupType(groupPosition) == 1) {
			// this is a filler row
			return 0;
		}
		return 1;
	}

	



	@Override
	public boolean hasStableIds() {
		return true;
	}



	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}
	


	@Override
	public Object getGroup(int groupPosition) {
		return mHotel.mSummary.roomDetails[groupPosition];
	}



	@Override
	public int getGroupCount() {
		if (mHotel.mSummary.roomDetails == null) {
			return 0;
		}
		return mHotel.mSummary.roomDetails.length + 1;
	}



	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public int getGroupTypeCount() {
		return 2;
	}
	
	@Override
	public int getGroupType(int groupPosition) {
		if (groupPosition >= mHotel.mSummary.roomDetails.length) {
			// filler row
			return 1;
		}
		return 0;
	}

}
