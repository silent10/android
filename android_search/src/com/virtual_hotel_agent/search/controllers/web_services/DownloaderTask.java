package com.virtual_hotel_agent.search.controllers.web_services;

import java.util.ArrayList;

import org.json.JSONObject;

import roboguice.util.Ln;
import android.os.AsyncTask;

import com.virtual_hotel_agent.search.controllers.web_services.DownloaderTaskInterface.DownloaderStatus;

abstract public class DownloaderTask extends AsyncTask<Void, Integer, JSONObject> {

	static private final String TAG = DownloaderTask.class.getSimpleName();
	
	protected int id;
	ArrayList<DownloaderTaskInterface> mListeners = new ArrayList<DownloaderTaskInterface>();

	public DownloaderTask(int id) {
		this.id = id;
	}
	
	public void attach(DownloaderTaskInterface listener) {
		mListeners.add(listener);
	}

	public void detach() {
		mListeners = new ArrayList<DownloaderTaskInterface>();
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

		Ln.d("onPostExecute");


		for (DownloaderTaskInterface listener : mListeners) {
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
		Ln.d("onPreExecute");

		mProgress = DownloaderStatus.Started;
		for (DownloaderTaskInterface listener : mListeners) {
			listener.startProgressDialog(getId());
		}

		super.onPreExecute();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {

		Ln.d("onProgressUpdate:" + mProgress);

		for (DownloaderTaskInterface listener : mListeners) {
			listener.updateProgress(getId(), mProgress);
		}
		super.onProgressUpdate(values);
	}
	
	public Object getProgress() {
		return Integer.valueOf(mProgress.ordinal());
	}

}
