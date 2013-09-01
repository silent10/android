package com.evature.search.controllers.web_services;

public interface EvaDownloaderTaskInterface {

	enum DownloaderStatus {
		NotStarted,
		Started,
		Finished,
		FinishedWithError
	}

	void endProgressDialog(int id, String result);

	void startProgressDialog(int id);

	void endProgressDialogWithError(int id, String result);

	void updateProgress(int id, DownloaderStatus mProgress);

}
