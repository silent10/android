package com.softskills.evasearch;

import android.os.AsyncTask;
import android.util.Log;

abstract public class EvaDownloaderTask  extends AsyncTask<Void, Integer, Void>{
	
	static private final String TAG = "EvaDownloaderTask";

	EvaDownloaderTaskInterface mListener = null;
	
	void attach(EvaDownloaderTaskInterface listener) 
	{
		mListener = listener;
	}
	
	void detach()
	{
		mListener= null;
	}
	
	int mProgress = 0;
		
	
	@Override
	protected Void doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}
	
		
	@Override
	protected void onPostExecute(Void result) {
		
		Log.i(TAG,"onPostExecute");
		
		if(mListener==null) return;
		
		if(mProgress == EvaDownloaderTaskInterface.PROGRESS_FINISH)
		{
			mListener.endProgressDialog();
		}
		else
		{
			mListener.endProgressDialogWithError();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		Log.i(TAG,"onPreExecute");
		
		mListener.startProgressDialog();
		
		super.onPreExecute();
	}
	
	@Override
	protected void onProgressUpdate(Integer... values) {
		
		Log.i(TAG,"onProgressUpdate:"+mProgress);
		
		if(mListener!=null)
		{
			Log.i(TAG,"updating progress");
			mListener.updateProgress(mProgress);
		}
		super.onProgressUpdate(values);
	}

	public Object getProgress() {
		return new Integer(mProgress);
	}

}
