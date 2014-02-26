package com.virtual_hotel_agent.search.views.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

public class HotelGalleryAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<Bitmap> mBitmaps;
	private int mGalleryItemBackground;

	public int getCount() {
		return mBitmaps.size();
	}

	@Override
	public Object getItem(int position) {
		return mBitmaps.get(position);
	}

	public long getItemId(final int position) {
		return position;
	}

	public View getView(final int position, final View contentView, final ViewGroup viewGroup) {
		ImageView myView;

		if (contentView == null) {
			myView = new ImageView(mContext);
		} else {
			myView = (ImageView) contentView;
		}

		myView.setImageBitmap(mBitmaps.get(position));

		myView.setLayoutParams(new Gallery.LayoutParams(300, 200));
		myView.setScaleType(ImageView.ScaleType.FIT_XY);
		myView.setBackgroundResource(mGalleryItemBackground);

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
	
	public HotelGalleryAdapter(Context ctx) {
		mContext = ctx;
		mBitmaps = new ArrayList<Bitmap>();
		mGalleryItemBackground = android.R.drawable.picture_frame;
	}

	public void clear() {
		mBitmaps.clear();
		notifyDataSetChanged();
	}

}