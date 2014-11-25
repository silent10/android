/*****
 * BugReport Dialog - based on ACRA's Crash Report Dialog  (org.acra.CrashReportDialog)
 */
package com.virtual_hotel_agent.search.controllers.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.virtual_hotel_agent.search.R;

public class AboutDialog extends BaseActivity implements  DialogInterface.OnDismissListener   {
	protected static final String TAG = "AboutDialog";
	AlertDialog mDialog;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.about);
		dialogBuilder.setView(buildCustomView(savedInstanceState));
		dialogBuilder.setNegativeButton(R.string._ok, null);
		mDialog = dialogBuilder.create();
		mDialog.setCancelable(true);
		mDialog.setOnDismissListener(this);
		mDialog.setCanceledOnTouchOutside(true);
		mDialog.show();
	}

	private View buildCustomView(Bundle savedInstanceState) {

		
		
		LinearLayout root = new LinearLayout(this);
		root.setOrientation(1);
		root.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
		root.setFocusable(true);
		root.setFocusableInTouchMode(true);

		ScrollView scroll = new ScrollView(this);
		root.addView(scroll, new LinearLayout.LayoutParams(-1, -1, 1.0F));
		LinearLayout scrollable = new LinearLayout(this);
		scrollable.setOrientation(1);
		scroll.addView(scrollable);

		/*
		if (BuildConfig.DEBUG) {
			try {
				int version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
				text += "\n\nVersion: "+version;
				if (!VHAApplication.AcraInitialized) {
					text += "\n\n  --->  ACRA not initalized!";
				}
			} catch (NameNotFoundException e) {
				Log.w(TAG, "Name not found getting version", e);
			}
		}*/
		
		WebView webView = new WebView(this);
		String text = "<html><body>" +
				"Hotels ratings and reviews powered by <a target='_blank' href='http://www.tripadvisor.co.uk'>"+
					"<img src='http://www.tripadvisor.co.uk/img/cdsi/langs/en/tripadvisor_logo_132x24-20654-0.gif' /></a><br><hr>"+
				"Hotel search and booking by Expedia<br><hr>"+
				"Text parsing by Evature"+
				"</body></html>";
		webView.loadData(text, "text/html", "utf-8");
		
		scrollable.addView(webView);
		
		return root;
	}

	public void onDismiss(DialogInterface dialog) {
		finish();
	}

}
