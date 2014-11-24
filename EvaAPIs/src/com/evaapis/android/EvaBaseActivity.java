package com.evaapis.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;




abstract public class EvaBaseActivity extends Activity implements EvaSearchReplyListener { 

	protected EvaComponent eva;
	
	public EvaBaseActivity() {
	}


	@Override
	public void onDestroy() {
		eva.onDestroy();
		super.onDestroy();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		eva.onPause();
	}
	
	
	// Request updates at startupResults
	@Override
	protected void onResume() {
		super.onResume();
		eva.onResume();
	}
	
// Handle the results from the speech recognition activity
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		eva.onActivityResult(requestCode, resultCode, data);
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	@Override
	protected void onCreate(Bundle arg0) {
		eva = new EvaComponent(this, this);
		eva.onCreate(arg0);
		super.onCreate(arg0);
	}
	
	
	public void searchWithVoice(Object cookie, boolean editLastUtterance)
	{
		eva.searchWithVoice(cookie, editLastUtterance);
	}

	public void searchWithText(String searchString, Object cookie, boolean editLastUtterance) {
		eva.searchWithText(searchString, cookie, editLastUtterance);
	}
	
	public void replyToDialog(int replyIndex) {
		eva.replyToDialog(replyIndex);
	}
	
	public void replyToDialog(int replyIndex, Object cookie) {
		eva.replyToDialog(replyIndex, cookie);
	}
	
	public boolean isNewSession() {
		return eva.isNewSession();
	}
	
	public void resetSession() {
		eva.resetSession();
	}
	
}
