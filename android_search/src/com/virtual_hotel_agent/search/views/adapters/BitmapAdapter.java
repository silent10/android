package com.virtual_hotel_agent.search.views.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.graphics.Palette.PaletteAsyncListener;
//import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.virtual_hotel_agent.search.R;

/***
 * Adapter for Bitmaps
 */
public class BitmapAdapter extends PagerAdapter {
	
	private Context context;
	private HashMap<Integer, Palette> palettes;
    private ArrayList<Bitmap> bitmaps;
    private LayoutInflater inflater;
 
    // constructor
    public BitmapAdapter(Context context) {
        this.context = context;
        palettes = new HashMap<Integer, Palette>();
        bitmaps = new ArrayList<Bitmap>();
    }
    
    public void addBitmap(Bitmap bmp) {
    	bitmaps.add(bmp);
    	notifyDataSetChanged();
    }
 
    @Override
    public int getCount() {
        return this.bitmaps.size();
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }
     
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
  
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fullscreen_image, container,
                false);
  
        ImageView imgDisplay = (ImageView) viewLayout;// viewLayout.findViewById(R.id.imgDisplay);
         
        Bitmap bitmap = bitmaps.get(position);
        imgDisplay.setImageBitmap(bitmap);
         
        ((ViewPager) container).addView(viewLayout);
  
        return viewLayout;
    }
     
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View)object);
    }
    
    public Bitmap getBitmap(int index) {
    	return bitmaps.get(index);
    }

	public void getPalette(final int position, final PaletteAsyncListener paletteAsyncListener) {
		if (palettes.containsKey(position)) {
			paletteAsyncListener.onGenerated(palettes.get(position));
		}
		else {
			Bitmap bitmap = getBitmap(position);
			Palette.generateAsync(bitmap, new PaletteAsyncListener() {
				@Override
				public void onGenerated(Palette palette) {
					palettes.put(position, palette);
					paletteAsyncListener.onGenerated(palette);
				}
			});
		}
	}
}