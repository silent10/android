package com.softskills.evasearch;

public interface EvaDownloaderTaskInterface {

	int PROGRESS_FINISH = 1;
	int PROGRESS_CREATE_HOTEL_DATA = 2;
	int PROGRESS_EXPEDIA_HOTEL_FETCH = 3;
	int PROGRESS_FINISH_WITH_ERROR = 4;

	void endProgressDialog();

	void startProgressDialog();

	void endProgressDialogWithError();

	void updateProgress(int mProgress);

}
