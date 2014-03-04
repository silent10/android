package com.virtual_hotel_agent.search.controllers.web_services;

import java.util.ArrayList;

import org.json.JSONObject;

import android.os.AsyncTask;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskListenerInterface.DownloaderStatus;

abstract public class DownloaderTask extends AsyncTask<Void, Integer, JSONObject> {

	static private final String TAG = DownloaderTask.class.getSimpleName();
	
	protected int id;
	ArrayList<DownloaderTaskListenerInterface> mListeners = new ArrayList<DownloaderTaskListenerInterface>();

	public DownloaderTask(int id) {
		this.id = id;
	}
	
	public void attach(DownloaderTaskListenerInterface listener) {
		mListeners.add(listener);
	}

	public void detach() {
		mListeners = new ArrayList<DownloaderTaskListenerInterface>();
	}

	DownloaderStatus mProgress = DownloaderStatus.NotStarted;

	public int getId() {
		return id;
	}

	@Override
	protected JSONObject doInBackground(Void... params) {
		// TODO Auto-generated method stub
		return null;
	}
	

//	public void setCachedResults(String cachedResult) {
//		Log.i(TAG, "Using cached Result");
//		onPostExecute(cachedResult);
//	}
	

	@Override
	protected void onPostExecute(JSONObject result) {

		Log.d(TAG, "onPostExecute");


		for (DownloaderTaskListenerInterface listener : mListeners) {
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
		for (DownloaderTaskListenerInterface listener : mListeners) {
			listener.startProgressDialog(getId());
		}

		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		Log.d(TAG, "onProgressUpdate:" + mProgress);

		for (DownloaderTaskListenerInterface listener : mListeners) {
			listener.updateProgress(getId(), mProgress);
		}
		super.onProgressUpdate(values);
	}
	
	public Object getProgress() {
		return Integer.valueOf(mProgress.ordinal());
	}

}
