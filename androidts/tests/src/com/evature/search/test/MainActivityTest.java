package com.evature.search.test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.evature.search.controllers.activities.MainActivity;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	private MainActivity mActivity;
	private TextView mView;

	// private String resourceString;

	public MainActivityTest() {
		super(MainActivity.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = this.getActivity();
		mView = (TextView) mActivity.findViewById(com.evature.search.R.id.debug_view);
		// resourceString = mActivity.getString(com.evature.search.R.string.error_tts_init);
	}

	public void testPreconditions() {
		assertNotNull(mView);
	}

	public void testText() {
		assertEquals("", (String) mView.getText());
	}

}
