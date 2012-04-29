package com.softskills.evasearch;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SearchAdapter extends BaseAdapter implements Filterable {

	private String[] mStringsOrig;
	private String[] mStrings;
	private LayoutInflater mInflater;
	private OnClickListener mPasteclick;
	
	SearchAdapter(Context context, ArrayList<String> matches, OnClickListener pasteClick) {
		super();
		mInflater = LayoutInflater.from(context);
		mStringsOrig = new String[matches.size()];
		for (int i=0; i<matches.size(); i++)
			mStringsOrig[i] = matches.get(i);
		mStrings = mStringsOrig;
		mPasteclick = pasteClick;
	}
	
	@Override
	public Filter getFilter() {
		return new Filter() {
			
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults oReturn = new FilterResults();
				ArrayList<String> results = new ArrayList<String>();
				for (String s : mStringsOrig)
				{
					results.add(s);
				}
				oReturn.values = results;
				oReturn.count = 1;
				return oReturn;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				mStrings = mStringsOrig;
				notifyDataSetChanged();
			}
		};
	}
	
	@Override
	public int getCount() {		
		return mStrings.length;
	}

	@Override
	public Object getItem(int position) {		
		return mStrings[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		 ViewHolder holder;
		 if (convertView == null) 
		 {
			 convertView = mInflater.inflate(R.layout.search_item, null);
			 holder = new ViewHolder();
			 
			 holder.searchString = (TextView)convertView.findViewById(R.id.searchString);
			 holder.pasteButton = (ImageButton)convertView.findViewById(R.id.pasteButton);
			 			 
			 convertView.setTag(holder);				 						 				 				 
		 } else {
			 holder = (ViewHolder) convertView.getTag();
		 }
		 
		 holder.pasteButton.setFocusableInTouchMode(true);
		 holder.pasteButton.setOnClickListener(mPasteclick);
		 holder.pasteButton.setTag(mStrings[position]);
		 holder.searchString.setText(mStrings[position]);
		 
		 return convertView;
	}
	
	static class ViewHolder
	{
		TextView searchString;
		RelativeLayout searchLayout;
		ImageButton pasteButton;
	}

}