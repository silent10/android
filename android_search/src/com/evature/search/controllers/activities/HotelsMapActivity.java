package com.evature.search.controllers.activities;

import roboguice.activity.RoboMapActivity;
import android.os.Bundle;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.models.EvaDatabase;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

// From Arik's app

public class HotelsMapActivity extends RoboMapActivity  {
	private final String TAG = "HotelMapActivity";

	GoogleMap mMap;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hotel_map);
		
		MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
		mMap = mapFragment.getMap();
//        mapView.setBuiltInZoomControls(true);
        addHotelsToMap();			
	}

	
	
	public void addHotelsToMap()
	{	    
//        List<Overlay> mapOverlays = mapView.getOverlays();
//        Drawable drawable = this.getResources().getDrawable(R.drawable.hotel_ico);
        BitmapDescriptor hotelIcon = BitmapDescriptorFactory.fromResource(R.drawable.hotel_ico);
//        HotelItemizedOverlay itemizedoverlay = new HotelItemizedOverlay(drawable, this);
//    
//        MapController mapController = mapView.getController();
//    
//        mapOverlays.removeAll(mapOverlays);
       
        EvaDatabase evaDb = MyApplication.getDb();
        
        int length=evaDb.mHotelData.length;
        if(length>30) length=30;
        
        int x[]=new int[length];
        int y[]=new int[length];
        
        
        for(int i=0;i<length;i++)
        {
        	y[i]=(int)(evaDb.mHotelData[i].mSummary.mLongitude*1000000.);
	        x[i]=(int)(evaDb.mHotelData[i].mSummary.mLatitude*1000000.);
	      	        
	        String city = evaDb.mHotelData[i].mSummary.mCity;
	        String name = evaDb.mHotelData[i].mSummary.mName;
	        LatLng point = new LatLng(x[i],y[i]);
//	        OverlayItem overlayitem = new OverlayItem(point, city,name);
//	        itemizedoverlay.addOverlay(overlayitem);
//	        mapOverlays.add(itemizedoverlay);
	        GroundOverlay groundOverlay = mMap.addGroundOverlay(new GroundOverlayOptions()
            	.image(hotelIcon).anchor(0, 1)
            	.position(point, 100f));
        }
        
        int sumX=0;
        int sumY=0;
        
        int maxX=Integer.MIN_VALUE;
        int minX=Integer.MAX_VALUE;
        int maxY=Integer.MIN_VALUE;
        int minY=Integer.MAX_VALUE;
        
        for(int i=0;i<length;i++)
        {
        	sumX+=x[i]/1000;
        	sumY+=y[i]/1000;        	
        	
        	if(x[i]>maxX)
        	{
        		maxX=x[i];
        	}
        	
        	if(x[i]<minX)
        	{
        		minX=x[i];
        	}
        	
        	if(y[i]>maxY)
        	{
        		maxY=y[i];
        	}
        	
        	if(y[i]<minY)
        	{
        		minY=y[i];
        	}
        }

        int midX=sumX/x.length;
        int midY=sumY/y.length;
        
        
//        LatLng point = new LatLng(midX*1000,midY*1000);
//        mapController.animateTo(point);
//        int spanLong = maxX-minX;
//        int spanLat = maxY-minY;
//        
//        if(spanLong<10000) spanLong =10000;
//        if(spanLat<10000) spanLat = 10000;
//        mapController.zoomToSpan(spanLong, spanLat) ;

	}

	
//	public static HotelsMapActivity newInstance() {
//		HotelsMapActivity result = new HotelsMapActivity();
//		return result;
//	}


	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	
	
}
