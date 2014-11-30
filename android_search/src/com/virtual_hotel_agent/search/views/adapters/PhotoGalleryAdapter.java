package com.virtual_hotel_agent.search.views.adapters;

import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class PhotoGalleryAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Bitmap> mBitmaps;
	private int mGalleryItemBackground;
	
	private boolean isCyclic = false;

	public int getCount() {
		if (isCyclic) {
			return Integer.MAX_VALUE;
		}
		else {
			return mBitmaps.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return mBitmaps.get(position % mBitmaps.size());
	}

	public long getItemId(final int position) {
		return position  % mBitmaps.size();
	}

	@SuppressLint("NewApi")
	public View getView(final int position, final View contentView, final ViewGroup viewGroup) {
		ImageView myView;

		if (contentView == null) {
			myView = new ImageView(mContext);
		} else {
			myView = (ImageView) contentView;
		}

		myView.setImageBitmap((Bitmap)getItem(position));

		myView.setLayoutParams(new Gallery.LayoutParams(300, 200));
		myView.setScaleType(ImageView.ScaleType.FIT_XY);
		myView.setBackgroundResource(mGalleryItemBackground);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			myView.setTransitionName("fullscreen_image");
		}

		return myView;
	}

	public void addBitmap(Bitmap bmp) {
		mBitmaps.add(bmp);
		notifyDataSetChanged();
	}

	public void removeBitmap(Bitmap bmp) {
		int i = mBitmaps.indexOf(bmp);
		if (i != -1) {
			mBitmaps.remove(i);
			notifyDataSetChanged();
		}
	}
	
	public PhotoGalleryAdapter(Context ctx) {
		mContext = ctx;
		mBitmaps = new ArrayList<Bitmap>();
		mGalleryItemBackground = android.R.drawable.picture_frame;
	}

	public int getRealSize() {
		return mBitmaps.size();
	}

	public void clear() {
		mBitmaps.clear();
		notifyDataSetChanged();
	}

	public void setCyclic(boolean b) {
		isCyclic = b;
	}

}