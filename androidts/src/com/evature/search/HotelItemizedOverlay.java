package com.evature.search;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class HotelItemizedOverlay extends ItemizedOverlay {
	
	Context mContext;
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
	
	public HotelItemizedOverlay(Drawable defaultMarker) {
		 super(boundCenterBottom(defaultMarker));
	}

	public HotelItemizedOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
   }
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	@Override
	protected OverlayItem createItem(int i) {
		  return mOverlays.get(i);
	}
	
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();
	}
	

	@Override
	public int size() {
		return mOverlays.size();
	}

}
