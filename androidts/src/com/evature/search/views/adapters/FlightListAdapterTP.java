package com.evature.search.views.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.R.id;
import com.evature.search.R.layout;
import com.evature.search.models.EvaDatabase;
import com.evature.search.models.travelport.AirPricingSolution;
import com.evature.search.models.travelport.AirSegment;
import com.evature.search.views.fragments.FlightsFragment;

public class FlightListAdapterTP extends BaseAdapter {
	private static final String TAG = "FlightListAdapterTP";

	private LayoutInflater mInflater;

	private EvaDatabase evaDatabase;

	// private FlightsFragment mParent;

	public FlightListAdapterTP(FlightsFragment parent, EvaDatabase evaDatabase) {
		Log.d(TAG, "CTOR");
		mInflater = LayoutInflater.from(parent.getActivity());
		// mParent = parent;
		this.evaDatabase = evaDatabase;
	}

	public void setData(EvaDatabase evaDatabase) {
		Log.d(TAG, "setData()");
		this.evaDatabase = evaDatabase;
	}

	@Override
	public int getCount() {
		int count = 0;
		evaDatabase = MyApplication.getDb();
		if (evaDatabase != null) {
			if (evaDatabase.airLowFareSearchRsp != null) {
				if (evaDatabase.airLowFareSearchRsp.airPricingSolutions != null) {
					count = evaDatabase.airLowFareSearchRsp.airPricingSolutions.size();
				} else
					Log.w(TAG, "evaDatabase.airLowFareSearchRsp.airPricingSolutions = null ");
			} else
				Log.w(TAG, "evaDatabase.airLowFareSearchRsp = null ");
		} else
			Log.w(TAG, "evaDatabase = null ");
		if (MyApplication.getDb() == null)
			Log.w(TAG, "MyApplication.getDb() = null");
		else
			Log.d(TAG, "MyApplication.getDb() != null");
		Log.d(TAG, "count = " + String.valueOf(count));
		return count;
	}

	@Override
	public Object getItem(int position) {
		return evaDatabase.airLowFareSearchRsp.airPricingSolutions.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "getView " + String.valueOf(position));
		AirPricingSolution priceSolution = evaDatabase.airLowFareSearchRsp.airPricingSolutions.get(position);
		View row = mInflater.inflate(R.layout.flight_list_item, parent, false);

		// TextView label = (TextView) row.findViewById(R.id.price);
		// label.setText(priceSolution.Currency + String.valueOf(priceSolution.totalPrice) + " ");

		LinearLayout linearLayout = (LinearLayout) row.findViewById(R.id.flight_list_item_layout);
		for (AirSegment segment : priceSolution.segments) {
			View child = mInflater.inflate(R.layout.flight_list_item2, null);

			TextView departure_time = (TextView) child.findViewById(R.id.itinerary_view_departure_time);
			String departureTime = segment.DepartureTime.substring(11, 16);
			departure_time.setText(departureTime + " " + segment.Origin);

			TextView arrival_time = (TextView) child.findViewById(R.id.itinerary_view_arrival_time);
			String ArrivalTime = segment.ArrivalTime.substring(11, 16);
			arrival_time.setText(ArrivalTime + " " + segment.Destination);

			TextView duration = (TextView) child.findViewById(R.id.itinerary_view_journey_duration);
			duration.setText(segment.FlightTime + " minutes");

			TextView airline = (TextView) child.findViewById(R.id.itinerary_view_airline);
			airline.setText(segment.Carrier);

			linearLayout.addView(child);
			Log.d(TAG, "getView addView");
		}
		Context parentContext = parent.getContext();
		DisplayMetrics displayMetrics = parentContext.getResources().getDisplayMetrics();
		int pixels;
		// View blank = new View(parentContext);
		// pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, displayMetrics);
		// blank.setMinimumWidth(pixels);
		// linearLayout.addView(blank);

		TextView carrier = new TextView(parentContext);
		carrier.setText(priceSolution.segments.get(0).Carrier); // patch, of course
		pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, displayMetrics);
		carrier.setWidth(pixels);
		carrier.setTextColor(Color.BLACK);
		linearLayout.addView(carrier);

		TextView price = new TextView(parentContext);
		String Currency = priceSolution.Currency;
		if ("USD".equals(Currency)) {
			Currency = "$";
		}
		if ("GBP".equals(Currency)) {
			Currency = "£";
		}
		price.setText(Currency + String.valueOf(priceSolution.totalPrice));
		pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, displayMetrics);
		price.setWidth(pixels);
		price.setTextColor(Color.BLACK);

		linearLayout.addView(price);
		return (row);

	}

	public static class ViewHolder {
		public LinearLayout layout;
		TextView name;
		TextView origin;
		TextView rate;
		int index;
	}

}
