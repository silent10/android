package com.virtual_hotel_agent.search.controllers.activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.virtual_hotel_agent.search.R;

public class HotelMapActivity extends FragmentActivity {
	
    public static String HOTEL_NAME="SUMMARY";
    public static String HOTEL_LATITUDE="LATITUDE";
    public static String HOTEL_LONGITUDE="LONGITUDE";
    public static String HOTEL_SNIPPET="SNIPPET";
    private GoogleMap mMap = null;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_map);

        
    }

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			FragmentManager fm = getSupportFragmentManager();
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
						addHotelToMap();
					}
            	});
            }
        }
	}

	private void addHotelToMap() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		String name = extras.getString(HOTEL_NAME);
		String longitudeString = extras.getString(HOTEL_LONGITUDE);
		String latitudeString = extras.getString(HOTEL_LATITUDE);
		String snippet = extras.getString(HOTEL_SNIPPET);
		// String city = extras.getString(HOTEL_CITY);
		
		if (name == null || longitudeString == null || latitudeString == null) {
			return;
		}
		if (snippet == null) {
			snippet = "";
		}

		Double longitude = Double.valueOf(longitudeString);
		Double latitude = Double.valueOf(latitudeString);

		LatLng point = new LatLng(latitude, longitude);

		BitmapDescriptor hotelIcon = BitmapDescriptorFactory
				.fromResource(R.drawable.hotel_small_flag_selected);

		mMap.addMarker(new MarkerOptions().position(point).title(name)
				.anchor(0, 1).snippet(snippet)
				.icon(hotelIcon));

		CameraPosition position = new CameraPosition.Builder().target(point)
				.zoom(14.5f).bearing(0).tilt(15).build();

		mMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
	}

	@Override
    protected void onResume() {
        super.onResume();
        int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (errCode != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errCode, this, 0);
			errorDialog.show();
		}
		else {
			setUpMapIfNeeded();
		}
    }
	
	
	

}


