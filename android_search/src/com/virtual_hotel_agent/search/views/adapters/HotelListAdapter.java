package com.virtual_hotel_agent.search.views.adapters;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Pattern;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
import android.support.v7.graphics.Palette.Swatch;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ean.mobile.hotel.Hotel;
import com.evature.util.Log;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader.LoadedCallback;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

public class HotelListAdapter extends BaseAdapter {

//	private static final double TRIP_ADVISOR_GOOD_RATING = 4.0;
	private static final double DISTANCE_DELTA = 200;
	private static final String TAG = "HotelListAdapter";
	private LayoutInflater mInflater;
	private HotelListFragment mParent;
	static BitmapDrawable mHotelIcon;
//	static Drawable mTripadvisorPlaceHolder;

	public HotelListAdapter(HotelListFragment parent) {

		mInflater = LayoutInflater.from(parent.getActivity());
		mParent = parent;
		if (mHotelIcon == null)
			mHotelIcon = (BitmapDrawable) parent.getActivity().getResources().getDrawable(R.drawable.slanted_icon_72);
//		if (mTripadvisorPlaceHolder == null)
//			mTripadvisorPlaceHolder = parent.getActivity().getResources().getDrawable(R.drawable.transparent_overlay);
	}
	
	private List<Hotel>  getHotels() {
		return VHAApplication.FOUND_HOTELS;
	}

	@Override
	public int getCount() {
		List<Hotel> hotels = getHotels();
		if (hotels != null && hotels.size() > 0) {
			return hotels.size()+1;
		}
		return 0;
	}
	
	@Override
	public int getItemViewType(int position){
		List<Hotel> hotels = getHotels();
		if (hotels == null || position >= hotels.size()) {
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
		List<Hotel> hotels = getHotels();
		if (hotels != null && position < hotels.size()) {
			return hotels.get(position);
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
	
	static private Pattern thumbnailToLandscape = Pattern.compile("_n\\.(jpg|png|gif|bmp)$", Pattern.CASE_INSENSITIVE);

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		
		List<Hotel> hotels = getHotels();
		if (hotels == null || position >= hotels.size()) {
			return fillerView(convertView, parent);
		}
		if (convertView == null || convertView.getTag() == null) {
			convertView = mInflater.inflate(R.layout.hotel_list_item, null);
			holder = new ViewHolder();
			holder.image = (ImageView) convertView.findViewById(R.id.hotelImage);
			holder.tripAdvisorStrip = (ImageView) convertView.findViewById(R.id.tripAdvisorStrip);
//			holder.tripAdvisorRating = (TextView)convertView.findViewById(R.id.tripAdvisorRating);
			holder.name = (TextView) convertView.findViewById(R.id.hotelName);
			holder.rate = (TextView) convertView.findViewById(R.id.pricePerNight);
			holder.layout = (ViewGroup) convertView.findViewById(R.id.hotel_list_item_layout);
			holder.distance = (TextView) convertView.findViewById(R.id.hotelDistance);
			holder.location = (TextView) convertView.findViewById(R.id.hotelLocation);
			holder.cardView = (CardView) convertView.findViewById(R.id.card_view);
			holder.reviews = (TextView) convertView.findViewById(R.id.tripAdvisorReviews);
			holder.rating = (RatingBar) convertView.findViewById(R.id.rating);
			holder.layout.setTag(holder);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		
		final Hotel hotel = hotels.get(position);
		if (hotel == null) {
			Log.w(TAG, "No hotel info for adapter position "+position);
			return convertView;
		}

		Spanned spannedName = Html.fromHtml(hotel.name);
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

		if (holder.name.getText().equals(name) == false) {
			holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
			holder.name.setText(name);
			if (holder.name.getHeight() > 100) {
				holder.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
			}
		
	
			// Calculate hotel distance
			double distance = hotel.getDistanceFromMe();
			if (distance > 0 && distance < DISTANCE_DELTA) {
				DecimalFormat distanceFormat = new DecimalFormat("#.#");
				String formattedDistance = distanceFormat.format(distance);
				holder.distance.setText(formattedDistance + "km");
			} else {
				holder.distance.setVisibility(View.GONE);
			}
			
			String location = hotel.locationDescription;
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
			String formattedRate = rateFormat.format(hotel.lowPrice.doubleValue());
			String rate = SettingsAPI.getCurrencySymbol(mParent.getActivity()) + " " + formattedRate;
	
			holder.rate.setText(rate);
	
			holder.rating.setRating(hotel.starRating.floatValue());
	
			final S3DrawableBackgroundLoader loader = VHAApplication.thumbnailLoader;
	
			loader.loadDrawable(
					hotel.mainHotelImageTuple, true, holder.image, mHotelIcon, new LoadedCallback() {
						
						@Override
						public void drawableLoaded(boolean success, BitmapDrawable drawable) {
							Palette.generateAsync(((BitmapDrawable) drawable).getBitmap(), new PaletteAsyncListener() {
								
								@Override
								public void onGenerated(Palette palette) {
									int bgCol = palette.getLightVibrantColor(0xff444444);
									holder.image.setBackgroundColor(bgCol);
									holder.name.setBackgroundColor(bgCol);
									Swatch swatch = palette.getLightVibrantSwatch();
									if (swatch != null) {
										holder.name.setTextColor(swatch.getTitleTextColor());
										holder.distance.setTextColor(swatch.getBodyTextColor());
									}
									swatch = palette.getLightMutedSwatch();
									int lightMuted = palette.getLightMutedColor(0xffdddddd);
									holder.distance.setBackgroundColor(lightMuted);
									holder.cardView.setCardBackgroundColor(lightMuted);
									if (swatch != null) {
										holder.distance.setTextColor(swatch.getTitleTextColor());
									}
									HotelListAdapter.this.notifyDataSetChanged();
								}
							});
							// load a high resolution bitmap
//							final String highResUrl = thumbnailToLandscape.matcher(hotel.mainHotelImageTuple.thumbnailUrl.toString()).replaceAll("_l.$1");
//							loader.loadDrawable(
//									highResUrl, holder.image, null, new LoadedCallback() {
//										
//										@Override
//										public void drawableLoaded(boolean success, Drawable drawable) {
//											if (success) {
//												//VHAApplication.HOTEL_PHOTOS.put(highResUrl, ((BitmapDrawable)drawable).getBitmap());
//	//											ViewGroup.LayoutParams params = (ViewGroup.LayoutParams) holder.image.getLayoutParams(); 
//	//										    params.width *= 1.5;
//	//										    holder.image.setLayoutParams(params);
//	//											    
//												HotelListAdapter.this.notifyDataSetChanged();
//											}
//										}
//									});
						}
					});
	
			if (hotel.tripAdvisorRatingUrl == null) {//hotel.tripAdvisorRating < TRIP_ADVISOR_GOOD_RATING) {
				holder.tripAdvisorStrip.setVisibility(View.GONE);
			} else {
				// TODO: show rating instead of just strip?
				holder.tripAdvisorStrip.setVisibility(View.VISIBLE);
	//			holder.tripAdvisorRating.setText(String.valueOf(hotel.tripAdvisorRating) +" out of 5");
				holder.reviews.setText("("+hotel.tripAdvisorReviewCount+")");
				loader.loadDrawable(hotel.tripAdvisorRatingUrl, holder.tripAdvisorStrip, null, null);
			}
		}
		
		return convertView;
	}

	private static class ViewHolder {
		public TextView reviews;
		public ViewGroup layout;
		public ImageView tripAdvisorStrip;
//		TextView tripAdvisorRating;
		CardView cardView;
		ImageView image;
		TextView name;
		TextView rate;
		TextView distance;
		TextView location;
		RatingBar rating;
	}

//	public static void stopBackgroundLoader() {
//		S3DrawableBackgroundLoader.getInstance().Reset();
//	}

}
