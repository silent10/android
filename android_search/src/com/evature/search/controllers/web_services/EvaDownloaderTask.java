package com.evature.search.controllers.web_services;

import android.os.AsyncTask;
import android.util.Log;

abstract public class EvaDownloaderTask extends AsyncTask<Void, Integer, String> {

	static private final String TAG = EvaDownloaderTask.class.getSimpleName();

	EvaDownloaderTaskInterface mListener = null;

	public void attach(EvaDownloaderTaskInterface listener) {
		mListener = listener;
	}

	public void detach() {
		mListener = null;
	}

	int mProgress = 0;

	public int getId() {
		return 0;
	}

	@Override
	protected String doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void onPostExecute(String result) {

		Log.d(TAG, "onPostExecute");

		if (mListener == null)
			return;

		if (mProgress == EvaDownloaderTaskInterface.PROGRESS_FINISH) {
			mListener.endProgressDialog(getId(), result);
		} else {
			mListener.endProgressDialogWithError(getId(), result);
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "onPreExecute");

		mListener.startProgressDialog(getId());

		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		Log.d(TAG, "onProgressUpdate:" + mProgress);

		if (mListener != null) {
			Log.d(TAG, "updating progress");
			mListener.updateProgress(getId(), mProgress);
		}
		super.onProgressUpdate(values);
	}

	public Object getProgress() {
		return Integer.valueOf(mProgress);
	}

}
