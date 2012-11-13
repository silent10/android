package com.evaapis;

import android.app.Dialog;

public interface SpeechRecognitionInterface
{
	public void startVoiceRecognitionActivity(String mPreferedLanguage) ;
	Dialog getListeningDialog();
	void prepareDialog();
}