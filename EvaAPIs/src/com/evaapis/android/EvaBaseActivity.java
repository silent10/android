package com.evaapis.android;



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

abstract public class EvaBaseActivity extends FragmentActivity implements EvaSearchReplyListener { 

	protected EvaComponent eva;
	
	public EvaBaseActivity() {
		eva = new EvaComponent(this, this);
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
		eva.onCreate(arg0);
		super.onCreate(arg0);
	}
	
	
	public void searchWithVoice()
	{
		eva.searchWithVoice();
	}
	
	public void searchWithVoice(Object cookie)
	{
		eva.searchWithVoice(cookie);
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
