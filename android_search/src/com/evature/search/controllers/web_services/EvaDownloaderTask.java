package com.evature.search.controllers.web_services;

import java.util.ArrayList;

import com.evature.search.controllers.web_services.EvaDownloaderTaskInterface.DownloaderStatus;

import android.os.AsyncTask;
import android.util.Log;

abstract public class EvaDownloaderTask extends AsyncTask<Void, Integer, String> {

	static private final String TAG = EvaDownloaderTask.class.getSimpleName();

	ArrayList<EvaDownloaderTaskInterface> mListeners = new ArrayList<EvaDownloaderTaskInterface>();

	public void attach(EvaDownloaderTaskInterface listener) {
		mListeners.add(listener);
	}

	public void detach() {
		mListeners = new ArrayList<EvaDownloaderTaskInterface>();
	}

	DownloaderStatus mProgress = DownloaderStatus.NotStarted;

	public int getId() {
		return 0;
	}

	@Override
	protected String doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void setCachedResults(String cachedResult) {
		Log.i(TAG, "Using cached Result");
		onPostExecute(cachedResult);
	}
	

	@Override
	protected void onPostExecute(String result) {

		Log.d(TAG, "onPostExecute");


		for (EvaDownloaderTaskInterface listener : mListeners) {
			if (mProgress == DownloaderStatus.Finished) {
				listener.endProgressDialog(getId(), result);
			} else {
				listener.endProgressDialogWithError(getId(), result);
			}
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		Log.d(TAG, "onPreExecute");

		mProgress = DownloaderStatus.Started;
		for (EvaDownloaderTaskInterface listener : mListeners) {
			listener.startProgressDialog(getId());
		}

		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		Log.d(TAG, "onProgressUpdate:" + mProgress);

		for (EvaDownloaderTaskInterface listener : mListeners) {
			listener.updateProgress(getId(), mProgress);
		}
		super.onProgressUpdate(values);
	}
	
	public Object getProgress() {
		return Integer.valueOf(mProgress.ordinal());
	}

}
