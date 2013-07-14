package com.evature.search.views.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.models.travelport.EvaTravelportDatabase;
import com.evature.search.models.travelport.RailJourney;
import com.evature.search.models.travelport.RailPricingSolution;
import com.evature.search.models.vayant.EvaVayantDatabase;
import com.evature.search.views.fragments.TrainsFragment;

public class TrainListAdapter extends BaseAdapter {
	private static final String TAG = "TrainListAdapter";

	private LayoutInflater mInflater;
	// private TrainsFragment mParent;
	private EvaTravelportDatabase evaDatabase = null;

	public TrainListAdapter(TrainsFragment parent) {
		Log.d(TAG, "CTOR");
		mInflater = LayoutInflater.from(parent.getActivity());
		// mParent = parent;
		this.evaDatabase = MyApplication.getFlightsDb();
	}

	public void setData(EvaTravelportDatabase evaDatabase) {
		Log.d(TAG, "setData()");
		this.evaDatabase = evaDatabase;
	}

	@Override
	public int getCount() {
		evaDatabase = MyApplication.getFlightsDb();
		if (evaDatabase != null) {
			if (evaDatabase.airLowFareSearchRsp != null && evaDatabase.airLowFareSearchRsp.railJourneys != null) {
				return evaDatabase.airLowFareSearchRsp.railJourneys.size();
			}
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return evaDatabase.airLowFareSearchRsp.railJourneys.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView " + String.valueOf(position));
		View row = mInflater.inflate(R.layout.train_list_item, parent, false);
		RailJourney journey = evaDatabase.airLowFareSearchRsp.railJourneyList.get(position);

		// TextView label = (TextView) row.findViewById(R.id.price);
		// label.setText("$" + String.valueOf(solution.mPrice) + " ");

		LinearLayout linearLayout = (LinearLayout) row.findViewById(R.id.train_list_item_layout);
		// for (Segment segment : solution.mSegments) {
		View child = mInflater.inflate(R.layout.train_list_item2, null);

		TextView OriginStationName = (TextView) child.findViewById(R.id.OriginStationName);
		OriginStationName.setText("From: " + journey.OriginStationName);

		TextView DestinationStationName = (TextView) child.findViewById(R.id.DestinationStationName);
		DestinationStationName.setText("To: " + journey.DestinationStationName);

		SimpleDateFormat sdf = new SimpleDateFormat("E, MMM d yyyy 'at' K:mm aa", Locale.US);
		// System.out.println(sdf.format(DateTime));

		TextView DepartureTime = (TextView) child.findViewById(R.id.DepartureTime);
		DepartureTime.setText("Departing: " + sdf.format(journey.departureTime));

		TextView ArrivalTime = (TextView) child.findViewById(R.id.ArrivalTime);
		ArrivalTime.setText("Arriving: " + sdf.format(journey.arrivalTime));

		TextView OperatingCompany = (TextView) child.findViewById(R.id.OperatingCompany);
		OperatingCompany.setText(journey.railsegment.OperatingCompany);

		List<Double> prices = new ArrayList<Double>();
		for (RailPricingSolution price : journey.prices) {
			System.out.println(price.totalPrice);
			prices.add(price.totalPrice);
		}
		TextView PriceRange = (TextView) child.findViewById(R.id.PriceRange);
		if (prices.isEmpty()) {
			PriceRange.setText("No seats are available - sorry!");
		} else {
			String min_price = "€" + String.valueOf(Collections.min(prices));
			String max_price = "€" + String.valueOf(Collections.max(prices));
			PriceRange.setText(min_price + " to " + max_price);
		}

		// Date departureDateTime = segment.trains.get(0).departureDateTime;
		// String departureTime = String.valueOf(departureDateTime.getHours()) + ":"
		// + String.valueOf(departureDateTime.getMinutes());
		// departure_time.setText(departureTime + " " + segment.trains.get(0).org);
		//
		// TextView arrival_time = (TextView) child.findViewById(R.id.itinerary_view_arrival_time);
		// Train train = segment.trains.get(segment.trains.size() - 1);
		// Date arrivalDateTime = train.arrivalDateTime;
		// String ArrivalTime = String.valueOf(arrivalDateTime.getHours()) + ":"
		// + String.valueOf(arrivalDateTime.getMinutes());
		// arrival_time.setText(ArrivalTime + " " + train.dst);

		linearLayout.addView(child);
		Log.d(TAG, "getView addView");
		// }
		return (row);

		// ViewHolder holder;
		// if (convertView == null) {
		// convertView = mInflater.inflate(R.layout.train_list_item, null);
		// holder = new ViewHolder();
		// holder.origin = (TextView) convertView.findViewById(R.id.vayant_list_origin);
		// } else {
		// holder = (ViewHolder) convertView.getTag();
		// }
		//
		// holder.index = position;
		// Spanned spannedName = Html.fromHtml("123123");
		// String name = spannedName.toString();
		// ((WindowManager) mParent.getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		// holder.name.setText(name);
		// holder.origin.setText("123");
		// // Format price
		// DecimalFormat rateFormat = new DecimalFormat("#.00");
		// if (position < journeys.mJourneys.length) {
		// BookingSolution booking_solution = journeys.mJourneys[position].mBookingSolutions[0];
		// String formattedRate = rateFormat.format(booking_solution.mPrice);
		// String rate = "$" + formattedRate;
		// holder.rate.setText(rate);
		// }
		// // S3DrawableBackgroundLoader.getInstance().loadDrawable(
		// // "http://images.travelnow.com" + holder.hotel.mSummary.mThumbNailUrl, holder.image, mEvaHotelIcon);
		// // holder.layout.setOnClickListener(mParent);
		// return convertView;
	}

	public static class ViewHolder {
		public LinearLayout layout;
		// ImageView image;
		TextView name;
		TextView origin;
		TextView rate;
		// TextView distance;
		// RatingBar rating;
		int index;
	}

}
