package com.virtual_hotel_agent.search.views.adapters;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.evaapis.android.EvatureLocationUpdater;
import com.evature.util.Log;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.models.chat.ChatItem;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

public class HotelListAdapter extends BaseAdapter {

	private static final double TRIP_ADVISOR_GOOD_RATING = 4.0;
	private static final double DISTANCE_DELTA = 200;
	private static final String TAG = "HotelListAdapter";
	private LayoutInflater mInflater;
	private HotelListFragment mParent;
	static Drawable mHotelIcon;

	public HotelListAdapter(HotelListFragment parent) {

		mInflater = LayoutInflater.from(parent.getActivity());
		mParent = parent;
		mHotelIcon = parent.getActivity().getResources().getDrawable(R.drawable.hotel72);
	}
	
	private HotelData[]  getHotels() {
		XpediaDatabase db = MyApplication.getDb();
		if (db != null) {
			return db.mHotelData;
		}
		return null;
	}

	@Override
	public int getCount() {
		HotelData[] hotels = getHotels();
		if (hotels != null && hotels.length > 0) {
			return hotels.length+1;
		}
		return 0;
	}
	
	@Override
	public int getItemViewType(int position){
		HotelData[] hotels = getHotels();
		if (hotels == null || position >= hotels.length) {
			return 1;
		}
		return 0; 
	}
	
	
	@Override
	public int getViewTypeCount(){
	  return 2;
	}

	@Override
	public Object getItem(int position) {
		HotelData[] hotels = getHotels();
		if (hotels != null && position < hotels.length) {
			return hotels[position];
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
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
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		HotelData[] hotels = getHotels();
		if (hotels == null || position >= hotels.length) {
			return fillerView(convertView, parent);
		}
		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(R.layout.hotel_list_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.hotelImage);
			//holder.tripAdvisorStrip = (ImageView) convertView.findViewById(R.id.tripAdvisorStrip);
			holder.name = (TextView) convertView.findViewById(R.id.hotelName);
			holder.rate = (TextView) convertView.findViewById(R.id.pricePerNight);
			holder.layout = (LinearLayout) convertView.findViewById(R.id.hotel_list_item_layout);
			holder.distance = (TextView) convertView.findViewById(R.id.hotelDistance);
			holder.location = (TextView) convertView.findViewById(R.id.hotelLocation);
			holder.rating = (RatingBar) convertView.findViewById(R.id.rating);
			holder.layout.setTag(holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		
		HotelData hotel = (HotelData) hotels[position];
		if (hotel == null) {
			Log.w(TAG, "No hotel info for adapter position "+position);
			return convertView;
		}
		holder.setHotelIndex(position);

		Spanned spannedName = Html.fromHtml(hotel.mSummary.mName);
		String name = spannedName.toString();

//		((WindowManager) mParent.getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		/* Now we can retrieve all display-related infos */
		// int width = display.getWidth();
		// int height = display.getHeight();
		//
		// int maxNameLength = (width-90)/18-3;

		// if(name.length()>maxNameLength)
		// {
		// name = (name.subSequence(0, maxNameLength)).toString();
		// name+="...";
		// }

		holder.name.setText(name);

		// Calculate hotel distance
		double distance = hotel.getDistanceFromMe();
		if (distance > 0 && distance < DISTANCE_DELTA) {
			DecimalFormat distanceFormat = new DecimalFormat("#.#");
			String formattedDistance = distanceFormat.format(distance);
			holder.distance.setText(formattedDistance + "km");
			holder.distance.setVisibility(View.VISIBLE);
		} else {
			holder.distance.setVisibility(View.GONE);
		}
		
		String location = hotel.mSummary.mLocationDescription;
		if (location != null && location.equals("") == false) {
			location = Html.fromHtml(location).toString();
			holder.location.setText(location);
			holder.location.setVisibility(View.VISIBLE);
		}
		else {
			holder.location.setVisibility(View.GONE);
		}

		// Format price
		DecimalFormat rateFormat = new DecimalFormat("#.00");
		String formattedRate = rateFormat.format(hotel.mSummary.mLowRate);
		String rate = SettingsAPI.getCurrencySymbol(mParent.getActivity()) + " " + formattedRate;

		holder.rate.setText(rate);

		holder.rating.setRating((float) hotel.mSummary.mHotelRating);

		S3DrawableBackgroundLoader.getInstance().loadDrawable(
				"http://images.travelnow.com" + hotel.mSummary.mThumbNailUrl, holder.image, mHotelIcon);

//		double trRating = hotel.mSummary.mTripAdvisorRating;
//
//		if (trRating < TRIP_ADVISOR_GOOD_RATING) {
//			holder.tripAdvisorStrip.setVisibility(View.GONE);
//		} else {
//			// TODO: show rating instead of just strip?
//			holder.tripAdvisorStrip.setVisibility(View.VISIBLE);
//		}

		
		return convertView;
	}

	public static class ViewHolder {
		public LinearLayout layout;
//		public ImageView tripAdvisorStrip;
		ImageView image;
		TextView name;
		TextView rate;
		TextView distance;
		TextView location;
		RatingBar rating;
		private int hotelIndex;
		
		public int getHotelIndex() {
			return hotelIndex;
		}
		public void setHotelIndex(int hotelIndex) {
			this.hotelIndex = hotelIndex;
		}
	}

	public void stopBackgroundLoader() {
		S3DrawableBackgroundLoader.getInstance().Reset();
	}

}
