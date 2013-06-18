package com.evature.search.controllers.activities;

import java.util.List;

import roboguice.activity.RoboMapActivity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.evature.search.MyApplication;
import com.evature.search.R;
import com.evature.search.models.EvaDatabase;
import com.evature.search.views.HotelItemizedOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

// From Arik's app

public class HotelsMapActivity extends RoboMapActivity  {
	private final String TAG = "HotelMapActivity";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hotel_map);
		mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        addHotelsToMap();			
	}

	MapView mapView;
	

	
	
	public void addHotelsToMap()
	{	    
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.hotel_ico);
        HotelItemizedOverlay itemizedoverlay = new HotelItemizedOverlay(drawable, this);
    
        MapController mapController = mapView.getController();
    
        mapOverlays.removeAll(mapOverlays);
       
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
	        GeoPoint point = new GeoPoint(x[i],y[i]);
	        OverlayItem overlayitem = new OverlayItem(point, city,name);
	        itemizedoverlay.addOverlay(overlayitem);
	        mapOverlays.add(itemizedoverlay);
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
        
        
        GeoPoint point = new GeoPoint(midX*1000,midY*1000);
        mapController.animateTo(point);
        int spanLong = maxX-minX;
        int spanLat = maxY-minY;
        
        if(spanLong<10000) spanLong =10000;
        if(spanLat<10000) spanLat = 10000;
        mapController.zoomToSpan(spanLong, spanLat) ;

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
