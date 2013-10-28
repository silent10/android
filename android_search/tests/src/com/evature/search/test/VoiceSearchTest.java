package com.evature.search.test;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Robolectric.shadowOf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;
import android.content.Intent;

import com.evaapis.EvaSpeechRecognitionActivity;
import com.evature.search.controllers.activities.MainActivity;

@RunWith(RobolectricTestRunner.class)
public class VoiceSearchTest {
	
	MainActivity mActivity;
	
	@Before
    public void setup() {
		ShadowLog.stream = System.out;
		
		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);
        
        mActivity = Robolectric.buildActivity(MainActivity.class).create().start().get();
	}
	
	
	@Test
	public void testStartRecordEva() {
		mActivity.eva.searchWithVoice();
		ShadowActivity shadowActivity = shadowOf(mActivity);
		Intent startedIntent = shadowActivity.getNextStartedActivity();
		assertEquals(EvaSpeechRecognitionActivity.class.getName(), startedIntent.getComponent().getClassName());
	}
}
