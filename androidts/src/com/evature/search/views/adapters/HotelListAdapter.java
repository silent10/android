package com.evature.search.views.adapters;

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

import com.evaapis.EvatureLocationUpdater;
import com.evature.components.S3DrawableBackgroundLoader;
import com.evature.search.EvaSettingsAPI;
import com.evature.search.R;
import com.evature.search.models.expedia.EvaDatabase;
import com.evature.search.models.expedia.HotelData;
import com.evature.search.views.fragments.HotelsFragment;

public class HotelListAdapter extends BaseAdapter {

	private static final double TRIP_ADVISOR_GOOD_RATING = 4.0;
	private static final double DISTANCE_DELTA = 200;
	EvaDatabase mEvaDb;
	private LayoutInflater mInflater;
	private HotelsFragment mParent;
	static Drawable mEvaHotelIcon;

	public HotelListAdapter(HotelsFragment parent, EvaDatabase evaDb) {

		mInflater = LayoutInflater.from(parent.getActivity());
		mParent = parent;
		mEvaDb = evaDb;
		mEvaHotelIcon = parent.getActivity().getResources().getDrawable(R.drawable.eva_hotel_icon);
	}

	@Override
	public int getCount() {
		if (mEvaDb != null && mEvaDb.mHotelData != null) {
			return mEvaDb.mHotelData.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return mEvaDb.mHotelData[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.hotel_list_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.hotelImage);
			holder.tripAdvisorStrip = (ImageView) convertView.findViewById(R.id.tripAdvisorStrip);
			holder.name = (TextView) convertView.findViewById(R.id.hotelName);
			holder.rate = (TextView) convertView.findViewById(R.id.pricePerNight);
			holder.layout = (LinearLayout) convertView.findViewById(R.id.hotel_list_item_layout);
			holder.distance = (TextView) convertView.findViewById(R.id.hotelDistance);
			holder.rating = (RatingBar) convertView.findViewById(R.id.rating);
			holder.layout.setTag(holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.hotel = mEvaDb.mHotelData[position];

		holder.setHotelIndex(position);

		Spanned spannedName = Html.fromHtml(holder.hotel.mSummary.mName);

		String name = spannedName.toString();

		((WindowManager) mParent.getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

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
		double distance;
		double hotelLatitude = holder.hotel.mSummary.mLatitude;
		double hotelLongitude = holder.hotel.mSummary.mLongitude;
		double myLongitude, myLatitude;
		try {
			float[] results = new float[3];
			myLongitude = EvatureLocationUpdater.getLongitude();
			myLatitude = EvatureLocationUpdater.getLatitude();
			Location.distanceBetween(myLatitude, myLongitude, hotelLatitude, hotelLongitude, results);
			if (results != null && results.length > 0)
				distance = results[0] / 1000;
			else
				distance = -1;
		} catch (Exception e2) {
			distance = -1;
		}
		if (distance > 0 && distance < DISTANCE_DELTA) {
			DecimalFormat distanceFormat = new DecimalFormat("#.#");
			String formattedDistance = distanceFormat.format(distance);
			holder.distance.setText(formattedDistance + "km");
		} else {
			holder.distance.setText("");
		}

		// Format price
		DecimalFormat rateFormat = new DecimalFormat("#.00");
		String formattedRate = rateFormat.format(holder.hotel.mSummary.mLowRate);

		String rate = EvaSettingsAPI.getCurrencySymbol(mParent.getActivity()) + " " + formattedRate;

		holder.rate.setText(rate);

		holder.rating.setRating((float) holder.hotel.mSummary.mHotelRating);

		S3DrawableBackgroundLoader.getInstance().loadDrawable(
				"http://images.travelnow.com" + holder.hotel.mSummary.mThumbNailUrl, holder.image, mEvaHotelIcon);

		double trRating = holder.hotel.mSummary.mTripAdvisorRating;

		if (trRating < TRIP_ADVISOR_GOOD_RATING) {
			holder.tripAdvisorStrip.setVisibility(View.GONE);
		} else {
			holder.tripAdvisorStrip.setVisibility(View.VISIBLE);
		}

		holder.layout.setOnClickListener(mParent);
		/*
		 * holder.image.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { ViewHolder holder = (ViewHolder) ((View)v.getParent()).getTag();
		 * 
		 * String hotelInfo =
		 * XpediaProtocol.getExpediaHotelInformation(mEvaDb.mHotelData[holder.hotelIndex].mSummary.mHotelId);
		 * 
		 * Intent intent = new Intent(mParent,ShowHotel.class);
		 * 
		 * intent.putExtra("hotelString", hotelInfo); intent.putExtra("hotelIndex", holder.hotelIndex );
		 * 
		 * mParent.startActivity(intent);
		 * 
		 * } });
		 */

		/*
		 * holder.name.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { ViewHolder holder = (ViewHolder)
		 * ((View)v.getParent().getParent()).getTag();
		 * 
		 * String hotelInfo =
		 * XpediaProtocol.getExpediaHotelInformation(mEvaDb.mHotelData[holder.hotelIndex].mSummary.mHotelId);
		 * 
		 * Intent intent = new Intent(mParent,ShowHotel.class);
		 * 
		 * intent.putExtra("hotelString", hotelInfo); intent.putExtra("hotelIndex", holder.hotelIndex );
		 * 
		 * mParent.startActivity(intent);
		 * 
		 * } });
		 * 
		 * holder.rating.setOnClickListener(new View.OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { ViewHolder holder = (ViewHolder) ((View)v.getParent()).getTag();
		 * 
		 * String hotelInfo =
		 * XpediaProtocol.getExpediaHotelInformation(mEvaDb.mHotelData[holder.hotelIndex].mSummary.mHotelId);
		 * 
		 * Intent intent = new Intent(mParent,ShowHotel.class);
		 * 
		 * intent.putExtra("hotelString", hotelInfo); intent.putExtra("hotelIndex", holder.hotelIndex );
		 * 
		 * mParent.startActivity(intent);
		 * 
		 * } });
		 */
		return convertView;
	}

	public static class ViewHolder {
		public LinearLayout layout;
		public ImageView tripAdvisorStrip;
		HotelData hotel;
		ImageView image;
		TextView name;
		TextView rate;
		TextView distance;
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
