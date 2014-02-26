package com.virtual_hotel_agent.search.views.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/****
 * Adapter for image resource ids
 */
public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<Integer> mThumbIds;
  
    public ImageAdapter(Context c, ArrayList<Integer> thumbIds) {
    	 mThumbIds = thumbIds;
    	 mContext = c;
	}

	public int getCount() {
        return mThumbIds.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds.get(position));
        return imageView;
    }

    
}