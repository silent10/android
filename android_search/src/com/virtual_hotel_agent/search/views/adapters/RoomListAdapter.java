package com.virtual_hotel_agent.search.views.adapters;

import java.text.DecimalFormat;
import java.util.List;

import roboguice.event.EventManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
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

import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.HotelRoom.ValueAdd;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.virtual_hotel_agent.search.ImageGalleryActivity;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.events.RoomSelectedEvent;

public class RoomListAdapter extends BaseExpandableListAdapter {

	final List<HotelRoom> mRooms;
	private LayoutInflater mInflater;
	private long hotelId;
	private Context mParent;
	static final DecimalFormat formatter = new DecimalFormat("#.##");
	private String disclaimer;
	private Bitmap mEvaBmpCached;
	protected static final String TAG = "RoomListAdapter";
	private int selectedColor;
	private int selectedNonRefundColor;
	private EventManager mEventManager;
		
	public RoomListAdapter(Context context, long hotelId, List<HotelRoom> rooms, EventManager eventManager)
	{	
		mInflater = LayoutInflater.from(context);
	    mParent = context;
		mRooms = rooms;
		this.hotelId = hotelId; 
		Resources resources = context.getResources();
		selectedColor = resources.getColor(R.color.selected_room_list_item);
		selectedNonRefundColor = resources.getColor(R.color.selected_non_refundable_room_list_item);
		disclaimer = "";
		mEventManager = eventManager;
		mEvaBmpCached = BitmapFactory.decodeResource(resources, R.drawable.slanted_icon_72);
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
		 
		 HotelRoom roomDetails = mRooms.get(position);
		 if (roomDetails.description != null) {
			 Spanned spannedName = Html.fromHtml(roomDetails.description);
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
			 
			 if (roomDetails.imageUrls != null && roomDetails.imageUrls.length > 0 && roomDetails.imageUrls[0] != null) {
				 LruCache<String, Bitmap> cache = MyApplication.HOTEL_PHOTOS;
				 Bitmap cachedPhoto = cache.get(roomDetails.imageUrls[0]);
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

		 
		 if(roomDetails.rate != null)
		 {
			 if (roomDetails.rate.nonRefundable) {
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
			 double fullRate = roomDetails.rate.chargeable.getAverageBaseRate().doubleValue();
			 double promoRate = roomDetails.rate.chargeable.getAverageRate().doubleValue();
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
			 
			 if (roomDetails.rate.promoDescription != null && roomDetails.rate.promoDescription.equals("") == false) {
				 holder.details.setText(roomDetails.rate.promoDescription);
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
		return mRooms.get(groupPosition);
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

		final HotelRoom room = mRooms.get(groupPosition);
		boolean nonRefundable = (room.rate != null && room.rate.nonRefundable);
		
		
		View photoContainer = convertView.findViewById(R.id.roomImage_container);
		ImageView photoHolder = (ImageView) convertView.findViewById(R.id.roomImage);
		if (room.imageUrls != null && room.imageUrls.length > 0) {
			 LruCache<String, Bitmap> cache = MyApplication.HOTEL_PHOTOS;
			 Bitmap cachedPhoto = cache.get(room.imageUrls[0]);
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
					intent.putExtra(ImageGalleryActivity.PHOTO_URLS, room.imageUrls);
					Spanned spannedName = Html.fromHtml(room.description);
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
		if (room.rate != null && room.rate.promoDetailText != null) {
			text.append("&lt;p&gt; "+TextUtils.htmlEncode(room.rate.promoDetailText) + "&lt;/p&gt; ");
		}
		if (room.longDescription != null) {
			text.append("&lt;p&gt; &lt;b&gt;Room Description &lt;/b&gt; &lt;br&gt;");
			text.append(TextUtils.htmlEncode(room.longDescription) + "&lt;/p&gt; ");
		}
		if (room.valueAdds != null && room.valueAdds.length > 0) {
			text.append("&lt;b&gt;You also get:&lt;/b&gt; &lt;ul&gt;");
			for (ValueAdd va: room.valueAdds) {
				text.append("&lt;li&gt;")
					.append(va.description)
					.append("&lt;/li&gt;");
			}
			text.append("&lt;/ul&gt;");
		}
		if (room.rate != null && room.rate.chargeable != null  &&
				room.rate.chargeable.surcharges != null && room.rate.chargeable.surcharges.size() > 0) {
			String dollar = " "+SettingsAPI.getCurrencySymbol(mParent); // TODO: change same as booking page room.rate.chargeable.currencyCode
			text.append("&lt;b&gt;Surcharges&lt;/b&gt; &lt;ul&gt;");
			for (String surchargeType: room.rate.chargeable.surcharges.keySet()) {
				text.append("&lt;li&gt;")
					.append(surchargeType)
					.append(": ")
					.append(room.rate.chargeable.surcharges.get(surchargeType)) // TODO: formatting
					.append(dollar)
					.append("&lt;/li&gt;");
			}
			text.append("&lt;/ul&gt;");
		}
		if (room.checkInInstructions != null && room.checkInInstructions.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Check In Instructions &lt;/b&gt; &lt;br&gt;")
				.append(room.checkInInstructions)
				.append("&lt;/p&gt;");
		}
		if (room.rate != null &&  room.rate.cancelllationPolicy != null && room.rate.cancelllationPolicy.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Cancelation Policy&lt;/b&gt; &lt;br&gt;")
				.append(room.rate.cancelllationPolicy)
				.append("&lt;/p&gt;");
		}
		if (room.policy != null && room.policy.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Policy&lt;/b&gt; &lt;br&gt;")
				.append(room.policy)
				.append("&lt;/p&gt;");
		}
		if (room.otherInformation != null && room.otherInformation.equals("") == false) {
			text.append("&lt;p&gt; &lt;b&gt;Other Information&lt;/b&gt; &lt;br&gt;");
			text.append(room.otherInformation)
				.append("&lt;br&gt;");
		}
		
		text.append("&lt;p&gt; &lt;b&gt; Refundable: &lt;/b&gt; ").append(nonRefundable ? "No" : "Yes").append("&lt;br&gt;");
		text.append("&lt;p&gt; &lt;b&gt; Smoking Policy: &lt;/b&gt; ").append(room.getSmokingPreferences()).append("&lt;br&gt;");
		if (room.rate!= null) {
			text.append("&lt;p&gt; &lt;b&gt; Guarantee Required: &lt;/b&gt; ").append(room.rate.guaranteeRequired ? "Yes" : "No").append("&lt;br&gt;");
			text.append("&lt;p&gt; &lt;b&gt; Deposit Required: &lt;/b&gt; ").append(room.rate.depositRequired ? "Yes" : "No").append("&lt;br&gt;");
		}
		text.append("&lt;/p&gt;");
		
		text.append("&lt;/font&gt;&lt;/body&gt;&lt;/html&gt;");
		
		Spanned marked_up = Html.fromHtml(text.toString());

		desc.loadData(marked_up.toString(), "text/html; charset=UTF-8", "utf-8");
		
		Tracker defaultTracker = GoogleAnalytics.getInstance(mParent).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createEvent("ui_action", "room_expanded", String.valueOf(hotelId), (long)groupPosition)
				    .build()
				   );
		
		Button bookButton = (Button) convertView.findViewById(R.id.buttonChooseRoom);
		bookButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mEventManager.fire(new RoomSelectedEvent(room, hotelId));
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
		return mRooms.get(groupPosition);
	}



	@Override
	public int getGroupCount() {
		if (mRooms == null || mRooms.size() == 0) {
			return 0;
		}
		return mRooms.size() + 1;
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
		if (groupPosition >= mRooms.size()) {
			// filler row
			return 1;
		}
		return 0;
	}

}
