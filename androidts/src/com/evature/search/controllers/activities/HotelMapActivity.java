package com.evature.search.controllers.activities;

import java.util.List;

import roboguice.activity.RoboMapActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.evature.search.R;
import com.evature.search.views.HotelItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class HotelMapActivity extends RoboMapActivity {
	
    public static String HOTEL_NAME="SUMMARY";
    public static String HOTEL_CITY="CITY";
    public static String HOTEL_LATITUDE="LATITUDE";
    public static String HOTEL_LONGITUDE="LONGITUDE";
	private MapController myMapController;
	
	@InjectView(R.id.mapview) MapView mapView;
	@InjectResource(R.drawable.hotel_ico)  Drawable hotelIcon;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hotel_map);
        mapView.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mapView.getOverlays();
        
        HotelItemizedOverlay itemizedoverlay = new HotelItemizedOverlay(hotelIcon, this);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        
        String name = extras.getString(HOTEL_NAME);
        String longitudeString = extras.getString(HOTEL_LONGITUDE);
        String latitudeString = extras.getString(HOTEL_LATITUDE);
        String city = extras.getString(HOTEL_CITY);
        
        int longitude=new Integer(longitudeString).intValue();
        int latitude=new Integer(latitudeString).intValue();
        
        GeoPoint point = new GeoPoint((int)latitude,(int)longitude);
        OverlayItem overlayitem = new OverlayItem(point, city,name);
        itemizedoverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedoverlay);
        myMapController = mapView.getController();
        myMapController.animateTo(point);
        int spanLong = 10000;
        int spanLat = 10000;
        myMapController.zoomToSpan(spanLong, spanLat) ;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}


