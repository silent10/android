package com.virtual_hotel_agent.search.views.fragments;

import java.text.DecimalFormat;

import roboguice.event.EventManager;
import roboguice.fragment.RoboFragment;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
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
import com.virtual_hotel_agent.search.models.expedia.HotelSummary;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;


public class HotelsMapFragment extends RoboFragment implements OnInfoWindowClickListener  {
	private final String TAG = "HotelsMapFragment";

	private GoogleMap mMap = null;
	private View mView;

	@Inject protected EventManager eventManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Context context = getActivity();
		Tracker defaultTracker = GoogleAnalytics.getInstance(context).getDefaultTracker();
		if (defaultTracker != null) 
			defaultTracker.send(MapBuilder
				    .createAppView()
				    .set(Fields.SCREEN_NAME, "HotelsMap Screen")
				    .build()
				);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
            	mapFragment.getView().post(new Runnable(){
					@Override
					public void run() {
						addHotelsToMap();
					}
            	});
            }
        }
	}


	@Override
    public void onResume() {
        super.onResume();
        int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
		if (errCode != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errCode, this.getActivity(), 0);
			errorDialog.show();
		}
		else {
			setUpMapIfNeeded();
		}
    }
	

	
	public void onHotelsListUpdated() {
		Log.d(TAG, "Updating map because hotels list was updated");
		mMap = null;
		setUpMapIfNeeded();
	}
	
	
	private void addHotelsToMap()
	{	    
       
        XpediaDatabase evaDb = MyApplication.getDb();
        
        int length= (evaDb != null && evaDb.mHotelData != null) ? evaDb.mHotelData.length : 0;
        if (length == 0) {
        	return;
        }
        if(length>30) 
        	length=30;
        
        BitmapDescriptor hotelIcon = BitmapDescriptorFactory.fromResource(R.drawable.hotel_small);
        Builder boundsBuilder = new LatLngBounds.Builder();
        
        for(int i=0;i<length;i++)
        {
	        String name = evaDb.mHotelData[i].mSummary.mName;
	        name = name.replace("&amp;", "&");
			
	        double rating = evaDb.mHotelData[i].mSummary.mHotelRating;
	        String formattedRating = Integer.toString((int)rating);
	        if (Math.round(rating) != Math.floor(rating)) {
	        	formattedRating += "½";
	        }
	        

			DecimalFormat rateFormat = new DecimalFormat("#.00");
			String formattedRate = rateFormat.format(evaDb.mHotelData[i].mSummary.mLowRate);
			String rate = formattedRate+" "+SettingsAPI.getCurrencySymbol(this.getActivity());
			
	        
	        LatLng point = new LatLng(evaDb.mHotelData[i].mSummary.mLatitude, evaDb.mHotelData[i].mSummary.mLongitude);
	        Marker marker = mMap.addMarker(new MarkerOptions()
				            .position(point)
				            .title(name)
				            .snippet(formattedRating+" stars, "+rate)
				            .icon(hotelIcon));
            
	        boundsBuilder.include(point);
        }
        
        mMap.setOnInfoWindowClickListener(this);
        
        LatLngBounds bounds = boundsBuilder.build();
        
        try{
        	//This line will cause the exception first times when map is still not "inflated"
        	mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));
        	Log.d(TAG, "Camera moved successfully");
        } catch(IllegalStateException e) {
        	Log.e(TAG, "Camera move exception", e);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,400,400,10));
            Log.d(TAG, "Camera moved to hardcoded width height");
        }
	}


	@Override
	public void onInfoWindowClick(Marker marker) {
		XpediaDatabase evaDb = MyApplication.getDb();
		int length= (evaDb != null && evaDb.mHotelData != null) ? evaDb.mHotelData.length : 0;
        if (length == 0) {
        	return;
        }
        if(length>30) 
        	length=30;
        
        for(int i=0;i<length;i++)
        {
	        HotelSummary hotel = evaDb.mHotelData[i].mSummary;
			String name = hotel.mName.replace("&amp;", "&");
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