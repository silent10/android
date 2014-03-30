package com.virtual_hotel_agent.search.views.fragments;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.List;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ean.mobile.hotel.Hotel;
import com.evature.util.Log;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLngBounds.Builder;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.inject.Inject;
import com.virtual_hotel_agent.search.MyApplication;
import com.virtual_hotel_agent.search.R;
import com.virtual_hotel_agent.search.SettingsAPI;
import com.virtual_hotel_agent.search.controllers.events.HotelItemClicked;


public class HotelsMapFragment extends RoboFragment implements OnInfoWindowClickListener  {
	private final String TAG = "HotelsMapFragment";

	private GoogleMap mMap = null;
	private View mView;
	private Marker selectedMarker=null;
	private String mCurrency;
	private WeakReference<Hotel>  mLastPresentedHotel;
	private int mLastPresentLength;
	
	private final int MAX_HOTELS = 30; 

	@Inject protected EventManager eventManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLastPresentedHotel = new WeakReference<Hotel>(null);
		mLastPresentLength = -1;
		Context context = getActivity();
		mCurrency = SettingsAPI.getCurrencySymbol(context);
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "HotelsMap Screen")
				    .build()
				);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if (mView != null) {
			Log.w(TAG, "Fragment initialized twice");
			((ViewGroup) mView.getParent()).removeView(mView);
			return mView;
		}
		
		mView = inflater.inflate(R.layout.hotels_map, container, false);
		return mView;
	}


	private void setUpMapIfNeeded() {
		if (mMap == null) {
			FragmentManager fm = getChildFragmentManager();
			SupportMapFragment mapFragment = (SupportMapFragment)  fm.findFragmentByTag("the_map");
			if (mapFragment == null || mapFragment.isAdded() == false) {
				mapFragment = SupportMapFragment.newInstance();
		        fm.beginTransaction().replace(R.id.map_container, mapFragment, "the_map").commit();
		        fm.executePendingTransactions();
			}
		
			mMap = mapFragment.getMap();
        }
		
		// Check if we were successful in obtaining the map.
        if (mMap != null && mView != null) {
        	
        	List<Hotel> currentHotels = MyApplication.FOUND_HOTELS;
            if (currentHotels.size() == 0) {
            	// no need to update map - nothing is present
            	return;
            }
            int startFrom = getFirstIndexToDisplay();
            if (mLastPresentLength == currentHotels.size() && mLastPresentedHotel.get() == currentHotels.get(startFrom)) {
            	// previously presented the same hotels - no need to do again
            	return;
            }
        	mLastPresentedHotel = new WeakReference<Hotel>(currentHotels.get(startFrom));
        	mLastPresentLength = currentHotels.size();
        	
        	mView.post(new Runnable(){
				@Override
				public void run() {
					addHotelsToMap();
				}
        	});
        }
	}


	@Override
    public void onResume() {
        super.onResume();
        FragmentActivity activity = this.getActivity();
		mCurrency = SettingsAPI.getCurrencySymbol(activity);
		Log.i(TAG, "Map resumed - checking google play");
        int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
		if (errCode != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errCode, activity, 0);
			errorDialog.show();
		}
		else {
			Log.i(TAG, "Map resumed - setting map");
			setUpMapIfNeeded();
		}
    }
	

	
	public void onHotelsListUpdated() {
		Log.d(TAG, "Updating map because hotels list was updated");
		mLastPresentLength = -1; // force refresh of map
		setUpMapIfNeeded();
	}
	
	private int getFirstIndexToDisplay() {
		int length= MyApplication.FOUND_HOTELS.size();
        if (length == 0) {
        	return 0;
        }
    	
        if(length > MAX_HOTELS) 
        	return length - MAX_HOTELS; // show only the last MAX_HOTELS in map
        return 0;
	}
	
	private void addHotelsToMap()
	{	   
		Log.d(TAG, "Adding hotels to map");
		mMap.clear();
		
		int startFrom = getFirstIndexToDisplay();
        
        BitmapDescriptor hotelIcon = BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag);
        BitmapDescriptor hotelIconSelected = BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag_selected);
        Builder boundsBuilder = new LatLngBounds.Builder();
        
        selectedMarker = null;
        Hotel selectedHotel = MyApplication.selectedHotel;
        
        for(int i=startFrom;i<MyApplication.FOUND_HOTELS.size();i++)
        {
	        Hotel hotelData = MyApplication.FOUND_HOTELS.get(i);
	        if (hotelData != selectedHotel) {
	        	addMapPoint(hotelData, hotelIcon, boundsBuilder);
	        }
        }
        mMap.setOnInfoWindowClickListener(this);
        
        if (selectedHotel == null) {
	        LatLngBounds bounds = boundsBuilder.build();
	        
	        try{
	        	//This line will cause the exception first times when map is still not "inflated"
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
	        	Log.d(TAG, "Camera moved successfully");
	        } catch(IllegalStateException e) {
	        	Log.w(TAG, "Camera move exception", e);
	            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,400,400,10));
	            Log.d(TAG, "Camera moved to hardcoded width height");
	        }
        }
        else {
        	selectedMarker = addMapPoint(selectedHotel, hotelIconSelected, boundsBuilder);
            selectedMarker.showInfoWindow();
            mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));
        }
	}

	private Marker addMapPoint(Hotel hotelSummary, BitmapDescriptor icon, Builder boundsBuilder) {
		String name = hotelSummary.name;
		
		double rating = hotelSummary.starRating.doubleValue();
		String formattedRating = Integer.toString((int)rating);
		if (Math.round(rating) != Math.floor(rating)) {
			formattedRating += "½";
		}
		

		DecimalFormat rateFormat = new DecimalFormat("#.00");
		String formattedRate = rateFormat.format(hotelSummary.lowPrice);
		String rate = formattedRate+" "+mCurrency;
		
		LatLng point = new LatLng(hotelSummary.address.latitude.doubleValue(), hotelSummary.address.longitude.doubleValue());
		Marker marker = mMap.addMarker(new MarkerOptions()
			            .position(point)
			            .title(name)
			            .anchor(0, 1)
			            .snippet(formattedRating+" stars, "+rate)
			            .icon(icon));
		if (boundsBuilder != null) {
			boundsBuilder.include(point);
		}
		return marker;
	}


	@Override
	public void onInfoWindowClick(Marker marker) {
        int startFrom = getFirstIndexToDisplay();

        if (selectedMarker != null) {
			selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag));
			selectedMarker = null;
		}
        selectedMarker = marker;
        selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag_selected));
        selectedMarker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));

        
        for(int i=startFrom; i<MyApplication.FOUND_HOTELS.size(); i++)
        {
	        Hotel hotel = MyApplication.FOUND_HOTELS.get(i);
			String name = hotel.name;
	        if (name.equals(marker.getTitle())){ 
	        	LatLng position = marker.getPosition();
	        	if (Math.abs(position.latitude - hotel.address.latitude.doubleValue()) < 0.001
	        		&& Math.abs(position.longitude - hotel.address.longitude.doubleValue()) < 0.001) {
	        		
	        		Log.d(TAG, "Hotel "+i+" clicked in map");
	        		Tracker defaultTracker = GoogleAnalytics.getInstance(getActivity()).getDefaultTracker();
	        		if (defaultTracker != null) 
	        			defaultTracker.send(MapBuilder
	        				    .createEvent("ui_action", "hotel_click", "hotels_map", (long) i)
	        				    .build()
	        				   );
	        		eventManager.fire(new HotelItemClicked(i) );
	        	}
	        }
        }
	}

	
}