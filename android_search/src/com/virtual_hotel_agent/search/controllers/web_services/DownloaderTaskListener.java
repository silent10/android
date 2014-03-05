package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

public class DownloaderTaskListener implements DownloaderTaskListenerInterface {

	public void endProgressDialog(int id, JSONObject result) {	}

	public void startProgressDialog(int id) {}

	public void endProgressDialogWithError(int id, JSONObject result) {}

	public void updateProgress(int id, DownloaderStatus mProgress) {}
}
