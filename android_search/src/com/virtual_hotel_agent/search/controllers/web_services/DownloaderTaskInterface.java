package com.virtual_hotel_agent.search.controllers.web_services;

import org.json.JSONObject;

public interface DownloaderTaskInterface {

	enum DownloaderStatus {
		NotStarted,
		Started,
		MadeSomeProgress,
		Finished,
		FinishedWithError
	}

	void endProgressDialog(int id, JSONObject result);

	void startProgressDialog(int id);

	void endProgressDialogWithError(int id, JSONObject result);

	void updateProgress(int id, DownloaderStatus mProgress);

}
