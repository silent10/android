package com.virtual_hotel_agent.search.controllers.web_services;


public interface DownloaderTaskListenerInterface {

	enum DownloaderStatus {
		NotStarted,
		Started,
		MadeSomeProgress,
		Finished,
		FinishedWithError
	}

	void endProgressDialog(int id, Object result);

	void startProgressDialog(int id);

	void endProgressDialogWithError(int id, Object result);

	void updateProgress(int id, DownloaderStatus mProgress);

}
