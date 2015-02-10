package com.virtual_hotel_agent.search.views.adapters;

import java.text.DecimalFormat;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.Swatch;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.ean.mobile.hotel.Hotel;
import com.evature.util.DLog;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader.LoadedCallback;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.VHAApplication;
import com.virtual_hotel_agent.search.views.fragments.HotelListFragment;

public class HotelListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

//	private static final double TRIP_ADVISOR_GOOD_RATING = 4.0;
	private static final double DISTANCE_DELTA = 200;
	private static final String TAG = "HotelListAdapter";

	private static final int VIEW_TYPE_FILLER = 0;
	private static final int VIEW_TYPE_HOTEL = 1;
	
	private LayoutInflater mInflater;
	private HotelListFragment mParent;
	static BitmapDrawable mHotelIcon;
	private Location targetLocation;
//	static Drawable mTripadvisorPlaceHolder;

	public interface OnHotelClickListener {
		void onHotelClick(int position, View view);
	}

	public HotelListAdapter(HotelListFragment parent) {

		mInflater = LayoutInflater.from(parent.getActivity());
		mParent = parent;
		if (mHotelIcon == null)
			mHotelIcon = (BitmapDrawable) parent.getActivity().getResources().getDrawable(R.drawable.slanted_icon_128);
//		if (mTripadvisorPlaceHolder == null)
//			mTripadvisorPlaceHolder = parent.getActivity().getResources().getDrawable(R.drawable.transparent_overlay);
	}
	
	private List<Hotel>  getHotels() {
		return VHAApplication.FOUND_HOTELS;
	}
	
	public void setTargetLocation(Location location) {
		targetLocation = location;
	}

	@Override
	public int getItemCount() {
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
			return VIEW_TYPE_FILLER;
		}
		return VIEW_TYPE_HOTEL; 
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	// Replace the contents of a view. This is invoked by the layout manager.
	@Override
	public void onBindViewHolder(ViewHolder itemHolder, int position) {

		List<Hotel> hotels = getHotels();
		if (hotels == null || position >= hotels.size()) {
			return; // nothing to do for filler view
		}
		
		final Hotel hotel = hotels.get(position);
		if (hotel == null) {
			DLog.w(TAG, "No hotel info for adapter position "+position);
			return;
		}
		final HotelViewHolder holder = (HotelViewHolder)itemHolder;
		holder.position = position;

		Spanned spannedName = Html.fromHtml(hotel.name);
		String name = spannedName.toString();
		DLog.d(TAG, "binding View to hotel "+name+",   holder had "+holder.name.getText());

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
			holder.name.setText(name);
	
			if (targetLocation != null) {
				// Calculate hotel distance
				double distance = hotel.getDistanceFromLocation(targetLocation);
				if (distance > 0 && distance < DISTANCE_DELTA) {
					DecimalFormat distanceFormat = new DecimalFormat("#.#");
					String formattedDistance = distanceFormat.format(distance);
					holder.distance.setText(formattedDistance + "km");
				} else {
					holder.distance.setVisibility(View.GONE);
				}
			}
			else {
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
	
			holder.name.setBackgroundColor(0xffffffff);
			holder.name.setTextColor(0xff222222);
			
			loader.loadDrawable(
					hotel.mainHotelImageTuple, true, holder.image, mHotelIcon, new LoadedCallback() {
						
						@Override
						public void drawableLoaded(boolean success, BitmapDrawable drawable) {
							if (!success) {
								DLog.w(TAG, "Failed download img for hotel "+holder.name.getText());
								return;
							}
							Bitmap bmp = ((BitmapDrawable) drawable).getBitmap();
							
							//Palette.generateAsync(((BitmapDrawable) drawable).getBitmap(), new PaletteAsyncListener() {
							Palette palette = Palette.generate(bmp);
								
							Swatch swatch = palette.getDarkVibrantSwatch();
							if (swatch== null) {
								swatch = palette.getLightVibrantSwatch();
								if (swatch== null) {
									List<Swatch> swatches = palette.getSwatches();
									if (swatches.size() > 1) {
										swatch = swatches.get(0);
									}
								}
							}
							
							if (swatch != null) {
								holder.name.setBackgroundColor(swatch.getRgb());
								holder.name.setTextColor(swatch.getTitleTextColor());
							}
							else {
								DLog.w(TAG, "No swatch made for photo?  hotel: "+holder.name.getText() );
							}

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
	
//			if (hotel.tripAdvisorRatingUrl == null) {//hotel.tripAdvisorRating < TRIP_ADVISOR_GOOD_RATING) {
//				holder.tripAdvisorStrip.setVisibility(View.GONE);
//			} else {
//				holder.tripAdvisorStrip.setVisibility(View.VISIBLE);
//	//			holder.tripAdvisorRating.setText(String.valueOf(hotel.tripAdvisorRating) +" out of 5");
//				holder.reviews.setText("("+hotel.tripAdvisorReviewCount+")");
//				loader.loadDrawable(hotel.tripAdvisorRatingUrl, holder.tripAdvisorStrip, null, null);
//			}
		}
		
	}

	private class FillerHolder extends RecyclerView.ViewHolder {

		public FillerHolder(View itemView) {
			super(itemView);
		}
	}
	
	static class HotelViewHolder extends RecyclerView.ViewHolder implements OnClickListener {
		public int position;
		public TextView reviews;
		public ViewGroup layout;
//		public ImageView tripAdvisorStrip;
//		TextView tripAdvisorRating;
		CardView cardView;
		ImageView image;
		TextView name;
		TextView rate;
		TextView distance;
		TextView location;
		RatingBar rating;
		OnHotelClickListener listener;

		public HotelViewHolder(View itemView, OnHotelClickListener listener) {
			super(itemView);
			this.listener = listener;
			image = (ImageView) itemView.findViewById(R.id.hotelImage);
//			reviews = (TextView) itemView.findViewById(R.id.tripAdvisorReviews);
//			tripAdvisorStrip = (ImageView) itemView.findViewById(R.id.tripAdvisorStrip);
////			tripAdvisorRating = (TextView)itemView.findViewById(R.id.tripAdvisorRating);
			name = (TextView) itemView.findViewById(R.id.hotelName);
			rate = (TextView) itemView.findViewById(R.id.pricePerNight);
			layout = (ViewGroup) itemView.findViewById(R.id.hotel_list_item_layout);
			distance = (TextView) itemView.findViewById(R.id.hotelDistance);
			location = (TextView) itemView.findViewById(R.id.hotelLocation);
			cardView = (CardView) itemView.findViewById(R.id.card_view);
			rating = (RatingBar) itemView.findViewById(R.id.rating);
			cardView.setOnClickListener(this);
		}

		@Override
		public void onClick(View v) {
			listener.onHotelClick(position, cardView);
		}
	}

		
	private FillerHolder fillerHolder = null;
	
	// Create new views. This is invoked by the layout manager.
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == VIEW_TYPE_FILLER) {
			if (fillerHolder == null) {
				View view = mInflater.inflate(R.layout.row_filler, parent, false);
				view.setClickable(false);
				view.setEnabled(false);
				fillerHolder = new FillerHolder(view);
			}
			return fillerHolder; // not going to be in use, but sadly required
		}
		
		View view = mInflater.inflate(R.layout.hotel_list_item, parent, false);
		ViewHolder holder = new HotelViewHolder(view, mParent);
		return holder;
	}


//	public static void stopBackgroundLoader() {
//		S3DrawableBackgroundLoader.getInstance().Reset();
//	}

}
