package com.evature.search.controllers.activities;

import roboguice.activity.RoboMapActivity;
import roboguice.inject.InjectFragment;
import roboguice.inject.InjectResource;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.evature.search.R;
import com.evature.search.views.HotelItemizedOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.MapFragment;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.OverlayItem;

public class HotelMapActivity extends RoboMapActivity {
	
    public static String HOTEL_NAME="SUMMARY";
    public static String HOTEL_CITY="CITY";
    public static String HOTEL_LATITUDE="LATITUDE";
    public static String HOTEL_LONGITUDE="LONGITUDE";
	private MapController myMapController;
	
	@InjectResource(R.drawable.hotel_ico)  Drawable hotelIcon;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_map);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
//        mapView.setBuiltInZoomControls(true);
//        List<Overlay> mapOverlays = mapView.getOverlays();
        
        HotelItemizedOverlay itemizedoverlay = new HotelItemizedOverlay(hotelIcon, this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        
        String name = extras.getString(HOTEL_NAME);
        String longitudeString = extras.getString(HOTEL_LONGITUDE);
        String latitudeString = extras.getString(HOTEL_LATITUDE);
        String city = extras.getString(HOTEL_CITY);
        
        int longitude= Integer.valueOf(longitudeString);
        int latitude= Integer.valueOf(latitudeString);
        
        GeoPoint point = new GeoPoint(latitude,longitude);
        OverlayItem overlayitem = new OverlayItem(point, city,name);
        itemizedoverlay.addOverlay(overlayitem);
//        mapOverlays.add(itemizedoverlay);
//        myMapController = mapView.getController();
//        myMapController.animateTo(point);
//        int spanLong = 10000;
//        int spanLat = 10000;
//        myMapController.zoomToSpan(spanLong, spanLat) ;
    }
	
	@Override
	public void onResume() {
		int errCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (errCode != ConnectionResult.SUCCESS) {
			Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errCode, this, 0);
			errorDialog.show();
		}
	}

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}


