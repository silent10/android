package com.evature.search;

import java.util.Date;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evature.search.vayant.BookingSolution;
import com.evature.search.vayant.Flight;
import com.evature.search.vayant.Segment;
import com.evature.search.vayant.VayantJourneys;

public class FlightListAdapter extends BaseAdapter {
	// This is the VAYANT version of the flight list adapter.
	// It is currently not in use

	private static final String TAG = "FlightListAdapter";

	private LayoutInflater mInflater;
	// private FlightsFragment mParent;
	private VayantJourneys journeys = null;

	FlightListAdapter(FlightsFragment parent, VayantJourneys journeys) {
		Log.d(TAG, "CTOR");
		mInflater = LayoutInflater.from(parent.getActivity());
		// mParent = parent;
		this.journeys = journeys;
	}

	public void setJourneys(VayantJourneys journeys) {
		Log.d(TAG, "setJourneys()");
		this.journeys = journeys;
	}

	@Override
	public int getCount() {
		if (journeys != null) {
			return journeys.mJourneys.length;
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return journeys.mJourneys[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView " + String.valueOf(position));
		BookingSolution solution = journeys.mJourneys[position].mBookingSolutions[0];
		View row = mInflater.inflate(R.layout.flight_list_item, parent, false);

		// TextView label = (TextView) row.findViewById(R.id.price);
		// label.setText("$" + String.valueOf(solution.mPrice) + " ");

		LinearLayout linearLayout = (LinearLayout) row.findViewById(R.id.flight_list_item_layout);
		for (Segment segment : solution.mSegments) {
			View child = mInflater.inflate(R.layout.flight_list_item2, null);

			// TextView stops_count = (TextView) child.findViewById(R.id.itinerary_view_stops_count);
			// stops_count.setText(segment.isDirect() ? "Direct" : "Not Direct");

			TextView departure_time = (TextView) child.findViewById(R.id.itinerary_view_departure_time);
			Date departureDateTime = segment.flights.get(0).departureDateTime;
			String departureTime = String.valueOf(departureDateTime.getHours()) + ":"
					+ String.valueOf(departureDateTime.getMinutes());
			departure_time.setText(departureTime + " " + segment.flights.get(0).org);

			TextView arrival_time = (TextView) child.findViewById(R.id.itinerary_view_arrival_time);
			Flight flight = segment.flights.get(segment.flights.size() - 1);
			Date arrivalDateTime = flight.arrivalDateTime;
			String ArrivalTime = String.valueOf(arrivalDateTime.getHours()) + ":"
					+ String.valueOf(arrivalDateTime.getMinutes());
			arrival_time.setText(ArrivalTime + " " + flight.dst);

			TextView airline = (TextView) child.findViewById(R.id.itinerary_view_airline);
			airline.setText(segment.flights.get(0).mcxr);

			linearLayout.addView(child);
			Log.d(TAG, "getView addView");
		}
		return (row);

		// ViewHolder holder;
		// if (convertView == null) {
		// convertView = mInflater.inflate(R.layout.flight_list_item, null);
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
