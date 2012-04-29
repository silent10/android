package com.softskills.evasearch;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher.ViewFactory;

import com.softskills.components.S3CyclicTextSwitcher;

public class HotelListFragment extends Fragment  implements OnClickListener, OnItemClickListener, OnKeyListener, EvaDownloaderTaskInterface{

	@Override
	public void onDestroy() {
		Log.e(TAG,"onDestroy");
		
		mCheckQueryLength.removeMessages(0);
		
		if(mDownLoader!=null)
		{
			mDownLoader.detach();
		}
		
		if(mContinuationLoader!=null)
		{
			mContinuationLoader.detach();
		}
		
		super.onDestroy();
	}
	
	
	@Override
	public void onDestroyView() {
		Log.e(TAG,"onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onPause() {
		Log.e(TAG,"onPause");
	
		super.onPause();
	}

	@Override
	public void onResume() {
		Log.e(TAG,"onResume");				
		super.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		Log.e(TAG,"onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onStart() {
		Log.e(TAG,"onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		Log.i(TAG,"onStop");
		super.onStop();
	}

	static HotelListDownloaderTask mDownLoader = null;
	EvaListContinuationDownloaderTask mContinuationLoader = null;

	private LinearLayout	mFooterView;

	boolean mClickEnabled = true;
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_VOICE && resultCode == Activity.RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(
					RecognizerIntent.EXTRA_RESULTS);
			SearchAdapter adapter = new SearchAdapter(getActivity(), matches, new OnClickListener() {

				@Override
				public void onClick(View v) {
					mQueryText.setText((String)v.getTag());
				}
			});
			if (matches != null && !matches.isEmpty())
			{
				mQueryText.setText(matches.get(0));
				matches.remove(0);
			}
			else
			{
				mQueryText.setText("");
			}
			//   mQueryText.setShow(true);
			mQueryText.setAdapter(adapter);
			//   mQueryText.filter();
			mQueryText.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			
					startSearch(mQueryText.getText().toString());

				}
			});
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void hideKeyboard() {
		InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
				InputMethodManager.HIDE_NOT_ALWAYS);
	}


	private void startSearch(String query) {
		
		((EvaSearchMainScreen)getActivity()).speak("Searching for hotel:"+query);
		
		mDownLoader = new HotelListDownloaderTask(this, query,
				EvaSettingsAPI.getCurrencyCode(getActivity()));
		mDownLoader.execute();

		hideKeyboard();
		stopDemoQuery();
	}

	private static final int RESULT_VOICE = 1;
	ImageButton mSearchButton;
	View mView;
	AutoCompleteTextView mQueryText;
	TextSwitcher mDemoQueriesSwitcher;	
	ListView mHotelListView;
	HotelListAdapter mAdapter;
	private S3CyclicTextSwitcher mDemoQueriesUpdatehandler;

	private final String TAG = "HotelListFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		if (mDownLoader != null) {
				mProgressDialog = ProgressDialog.show(getActivity(),
						"Getting Hotel Information", "Contacting search server", true, false);

				mDownLoader.attach(this);
		}				

		super.onCreate(savedInstanceState);
	}

	private void bindSearchButton() {
		
		Activity activity = getActivity();
		
		if(activity==null)
		{
			return;
		}
		
		mSearchButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.search_default));
		mSearchButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startSearch(mQueryText.getText().toString());
			}
		});
	}

	Handler mCheckQueryLength = new Handler()
	{
		boolean mFirstTime = true;
		
		@Override
		public void handleMessage(Message msg) 
		{
			int length = mQueryText.getText().toString().length();
			
			if(mFirstTime)
			{
				hideKeyboard();
				mFirstTime = false;
			}

			if (length == 0)
			{
				bindVoiceButton();				
			}
			else
			{
				bindSearchButton();	
			}

			if (length >= 2)
			{
				stopDemoQuery();
			}		
			
			sendEmptyMessageDelayed(0, 500);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mView = inflater.inflate(R.layout.hotel_list_portrait, container, false);

		mSearchButton = (ImageButton) mView.findViewById(R.id.microphone);

		bindVoiceButton();		

		mQueryText= (AutoCompleteTextView)mView.findViewById(R.id.searchQueryActv);

		mQueryText.setHint("Current Location...");

		mQueryText.requestFocus();

		mQueryText.setPadding(48, 0, 0, 0);

		mDemoQueriesSwitcher = (TextSwitcher)mView.findViewById(R.id.demoQuerySwitcher);
		setupDemoSwitcher();


		mHotelListView = (ListView) mView.findViewById(R.id.hotelListView);

		setAdapter();
		
		
		String[] demoQueries = getResources().getStringArray(R.array.demo_queries);
		mDemoQueriesUpdatehandler = new S3CyclicTextSwitcher(mDemoQueriesSwitcher, demoQueries);
		mDemoQueriesUpdatehandler.sendEmptyMessage(0);
		
		mCheckQueryLength.sendEmptyMessageDelayed(0, 500);				

		return mView;
	}
	
	void setAdapter()
	{
		if (EvaSearchApplication.getDb() != null) {

			if (EvaSearchApplication.getDb().mMoreResultsAvailable)
			{
				LayoutInflater li = getActivity().getLayoutInflater();
				mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
				mHotelListView.addFooterView(mFooterView);
				mHotelListView.setOnScrollListener(mListScroll);
				mEnabledPaging = true;
			}

			mAdapter = new HotelListAdapter(this, EvaSearchApplication.getDb());
			mHotelListView.setAdapter(mAdapter);
			mHotelListView.setOnItemClickListener(this);			
		}
	}


	private void setupDemoSwitcher() {
		mDemoQueriesSwitcher.setFactory(new ViewFactory() {

			@Override
			public View makeView() {
				final TextView t = new TextView(getActivity());
				t.setGravity(Gravity.TOP | Gravity.LEFT);
				t.setTextSize(16);
				t.setTextColor(Color.BLACK);
				t.setClickable(true);
				t.setBackgroundResource(R.drawable.suggestion);
				t.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mQueryText.setText(t.getText());
					}
				});
				return t;
			}
		});


	}

	private void stopDemoQuery() {
		mDemoQueriesUpdatehandler.removeMessages(0);
		mDemoQueriesSwitcher.setText("");
		mDemoQueriesSwitcher.setVisibility(View.GONE);
	}

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Eva speech");
		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,5);
		startActivityForResult(intent, RESULT_VOICE);
	}

	private void bindVoiceButton() {

		Activity activity = getActivity();
		
		if(activity==null) return;
		
		PackageManager pm = getActivity().getPackageManager();

		List<ResolveInfo> activities = pm.queryIntentActivities(
				new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0)
		{
			mSearchButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.microphone_default));

			mSearchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					startVoiceRecognitionActivity();
				}
			});

		}       		
		else
		{
			bindSearchButton();
		}
	}

	public static HotelListFragment newInstance()
	{
		HotelListFragment result = new HotelListFragment();
		return result;
	}


	@Override
	public void onClick(View v) {

		Log.i(TAG,"Stam");	
		
		onItemClick(null, v, 0, 0);
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) {
		Log.e(TAG,"onItemClick");
		
		HotelListAdapter.ViewHolder holder = (HotelListAdapter.ViewHolder)v.getTag();

		if(holder==null)
		{
			Log.e(TAG,"Got null holder");
			return;
		}
		
		
		((EvaSearchMainScreen)getActivity()).showHotelDetails(holder.hotelIndex);

	}

	private boolean	mPaging = false;
	private boolean	mEnabledPaging = false;

	private OnScrollListener	mListScroll	= new OnScrollListener() {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			if ((visibleItemCount > 0) && (firstVisibleItem + visibleItemCount == totalItemCount) &&
					!mPaging && mEnabledPaging)
			{
				mPaging = true;
				Log.d(TAG,"-Last Scroll-");

				String nextQuery = EvaSearchApplication.getDb().getNextQuery();
				mContinuationLoader = new EvaListContinuationDownloaderTask(HotelListFragment.this, nextQuery,
						EvaSettingsAPI.getCurrencyCode(getActivity()));
				mContinuationLoader.execute();
			}
		}
	};

		@Override
	public void endProgressDialog() {
		mDownLoader = null;
		
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
		}
		
		mAdapter = new HotelListAdapter(this, EvaSearchApplication.getDb());

		if (mEnabledPaging && mFooterView != null)
			mHotelListView.removeFooterView(mFooterView);

		mEnabledPaging = false;
		mPaging = false;

		if (EvaSearchApplication.getDb().mMoreResultsAvailable)
		{
			LayoutInflater li = getActivity().getLayoutInflater();hideKeyboard();
			mFooterView = (LinearLayout) li.inflate(R.layout.listfoot, null);
			mHotelListView.addFooterView(mFooterView);
			mHotelListView.setOnScrollListener(mListScroll);
			mEnabledPaging = true;
		}

		mHotelListView.setAdapter(mAdapter);		
				
	}
		
  ProgressDialog mProgressDialog = null;		
		

	@Override
	public void endProgressDialogWithError() {		
		Toast.makeText(getActivity(),
				"Error getting hotel information, please try again", 3000)
				.show();
				
		if(mProgressDialog!=null)
		{
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		
		mDownLoader = null;

	}

	@Override
	public void startProgressDialog() {
		
		if(mDownLoader!=null)
		{
			mProgressDialog = ProgressDialog.show(getActivity(),
				"Getting Hotel Information", "Contacting search server", true,
				false);
		}
	}



	@Override
	public void updateProgress(int mProgress) {

		Log.i(TAG,"update progress");

		if(mAdapter!=null)
		{
			mAdapter.notifyDataSetChanged();
		}
		else
		{
			return;
		}
		
		if (!EvaSearchApplication.getDb().mMoreResultsAvailable)
		{
			mHotelListView.removeFooterView(mFooterView);
			mEnabledPaging = false;
		}
	}

	
	public void finishPaging() {		
		mPaging = false;		
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

}
