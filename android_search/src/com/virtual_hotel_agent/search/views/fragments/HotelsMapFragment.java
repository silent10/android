package com.virtual_hotel_agent.search.views.fragments;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;
import com.virtual_hotel_agent.search.models.expedia.HotelData;
import com.virtual_hotel_agent.search.models.expedia.HotelSummary;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;


public class HotelsMapFragment extends RoboFragment implements OnInfoWindowClickListener  {
	private final String TAG = "HotelsMapFragment";

	private GoogleMap mMap = null;
	private View mView;
	private Marker selectedMarker=null;
	private String mCurrency;
	private WeakReference<HotelData[]>  mLastPresentedHotels;
	private int mLastPresentLength;
	
	private final int MAX_HOTELS = 30; 

	@Inject protected EventManager eventManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mLastPresentedHotels = new WeakReference<HotelData[]>(null);
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
        	
        	HotelData[] currentHotels = null;
        	XpediaDatabase evaDb = MyApplication.getDb();
            if (evaDb != null)
            	currentHotels = evaDb.mHotelData;
            if (currentHotels == null) {
            	// no need to update map - nothing is present
            	return;
            }
            if (mLastPresentLength == currentHotels.length && mLastPresentedHotels.get() == currentHotels) {
            	// previously presented the same hotels - no need to do again
            	return;
            }
        	mLastPresentedHotels = new WeakReference<HotelData[]>(currentHotels);
        	mLastPresentLength = currentHotels.length;
        	
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
	
	private void addHotelsToMap()
	{	   
		Log.d(TAG, "Adding hotels to map");
		mMap.clear();
		
        XpediaDatabase evaDb = MyApplication.getDb();
        
        int length= (evaDb != null && evaDb.mHotelData != null) ? evaDb.mHotelData.length : 0;
        if (length == 0) {
        	return;
        }
    	
        int startFrom = 0;
        if(length > MAX_HOTELS) 
        	startFrom = length - MAX_HOTELS; // show the last MAX_HOTELS in map
        
        BitmapDescriptor hotelIcon = BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag);
        BitmapDescriptor hotelIconSelected = BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag_selected);
        Builder boundsBuilder = new LatLngBounds.Builder();
        
        selectedMarker = null;
        HotelData selectedHotel = null;
        
        int selectedIndex = -1;
        ExpediaAppState rp = MyApplication.getExpediaAppState();
		if (rp != null) {
			selectedIndex = rp.getHotelId();
			if (selectedIndex != -1) {
				selectedHotel = evaDb.mHotelData[selectedIndex];
			}
		}
        
        for(int i=startFrom;i<length;i++)
        {
	        HotelData hotelData = evaDb.mHotelData[i];
	        if (i != selectedIndex) {
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

	private Marker addMapPoint(HotelData hotelData, BitmapDescriptor icon, Builder boundsBuilder) {
		HotelSummary hotelSummary = hotelData.mSummary;
		String name = hotelSummary.mName;
		name = Html.fromHtml(name).toString();
		
		double rating = hotelSummary.mHotelRating;
		String formattedRating = Integer.toString((int)rating);
		if (Math.round(rating) != Math.floor(rating)) {
			formattedRating += "½";
		}
		

		DecimalFormat rateFormat = new DecimalFormat("#.00");
		String formattedRate = rateFormat.format(hotelSummary.mLowRate);
		String rate = formattedRate+" "+mCurrency;
		
		LatLng point = new LatLng(hotelSummary.mLatitude, hotelSummary.mLongitude);
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
		XpediaDatabase evaDb = MyApplication.getDb();
		int length= (evaDb != null && evaDb.mHotelData != null) ? evaDb.mHotelData.length : 0;
        if (length == 0) {
        	return;
        }
        int startFrom = 0;
        if(length > MAX_HOTELS) 
        	startFrom = length - MAX_HOTELS;

        if (selectedMarker != null) {
			selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag));
			selectedMarker = null;
		}
        selectedMarker = marker;
        selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.hotel_small_flag_selected));
        selectedMarker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));

        
        for(int i=startFrom; i<length; i++)
        {
	        HotelSummary hotel = evaDb.mHotelData[i].mSummary;
			String name = Html.fromHtml(hotel.mName).toString();
	        if (name.equals(marker.getTitle())){ 
	        	LatLng position = marker.getPosition();
	        	if (Math.abs(position.latitude - hotel.mLatitude) < 0.001
	        		&& Math.abs(position.longitude - hotel.mLongitude) < 0.001) {
	        		
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