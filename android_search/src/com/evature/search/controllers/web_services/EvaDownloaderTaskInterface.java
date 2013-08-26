package com.evature.search.controllers.web_services;

public interface EvaDownloaderTaskInterface {

	int PROGRESS_FINISH = 1;
	int PROGRESS_CREATE_HOTEL_DATA = 2;
	int PROGRESS_EXPEDIA_HOTEL_FETCH = 3;
	int PROGRESS_FINISH_WITH_ERROR = 4;

	void endProgressDialog(int id, String result);

	void startProgressDialog(int id);

	void endProgressDialogWithError(int id, String result);

	void updateProgress(int id, int mProgress);

}
