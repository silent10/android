package com.softskills.components;

import java.util.Random;

import android.os.Handler;
import android.os.Message;
import android.widget.TextSwitcher;

public class S3CyclicTextSwitcher extends Handler {

	private TextSwitcher	mTextSwitcher;
	private int				mCurrentIndex;
	private String[]		mStringsArray;
	
	public S3CyclicTextSwitcher(TextSwitcher textSwitcher, String[] stringsArray) {
		mTextSwitcher = textSwitcher;
		mStringsArray = stringsArray;

		Random r = new Random();
		mCurrentIndex = r.nextInt(mStringsArray.length);
	}

	@Override
	public void handleMessage(Message msg) {
		mTextSwitcher.setText(mStringsArray[mCurrentIndex]);
		mCurrentIndex = (mCurrentIndex + 1) % mStringsArray.length;
		sendMessageDelayed(Message.obtain(this, 0), 4000);
	}

}
