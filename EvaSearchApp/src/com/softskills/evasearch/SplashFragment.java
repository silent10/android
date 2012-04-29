package com.softskills.evasearch;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SplashFragment extends Fragment {
	
	View mView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		int orientation = getResources().getConfiguration().orientation;
		
		if(orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			mView = inflater.inflate(R.layout.splash_landscape, container, false);
		}
		else
		{
			mView = inflater.inflate(R.layout.splash_portrait, container, false);
		}
		
		
		TextView tv = (TextView)mView.findViewById(R.id.splashMessage);
		
		tv.setText(mSplashMessage);

		return mView;
	}

	String mSplashMessage;

	public static Fragment newInstance(String message) {
	
		SplashFragment result = new SplashFragment();
		
		result.mSplashMessage = message;
		
		return result;
	}

}
