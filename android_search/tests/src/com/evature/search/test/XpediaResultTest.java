package com.evature.search.test;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.util.Transcript;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import android.os.AsyncTask;

import com.evature.search.controllers.web_services.HotelListDownloaderTask;
import com.evature.search.models.expedia.XpediaProtocol;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.util.Modules;

@RunWith(RobolectricTestRunner.class)
public class XpediaResultTest {

	private XpediaProtocol mockProtocol = mock(XpediaProtocol.class);
	private Transcript transcript;

	private class XpediaResultTestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(XpediaProtocol.class).toInstance(mockProtocol);
		}
	}
	
	private class TestXpediaAsyncTask extends HotelListDownloaderTask {
		@Override protected void onPreExecute() {
            transcript.add("onPreExecute");
        }

        @Override protected Void doInBackground(Void... voids) {
            transcript.add("doInBackground ");
            return super.doInBackground(voids);
        }

        @Override protected void onProgressUpdate(Integer... values) {
            transcript.add("onProgressUpdate ");
            super.onProgressUpdate(values);
        }

        @Override protected void onPostExecute(Void result) {
            transcript.add("onPostExecute ");
            super.onPostExecute(result);
        }

        @Override protected void onCancelled() {
        	transcript.add("onCancelled");
        	super.onCancelled();
        }
	}
	
	TestXpediaAsyncTask asyncTask;

	@Before
    public void setup() {
		ShadowLog.stream = System.out;
		
		
        Module roboGuiceModule = Modules.override(RoboGuice.newDefaultRoboModule(Robolectric.application)).with(new XpediaResultTestModule());
        RoboGuice.setBaseApplicationInjector(Robolectric.application, RoboGuice.DEFAULT_STAGE,
                roboGuiceModule);
		
		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);
        
        Robolectric.getBackgroundScheduler().pause();
        Robolectric.getUiThreadScheduler().pause();
        
        transcript = new Transcript();
        
        asyncTask = new TestXpediaAsyncTask();
	}
	
	@Test
	public void testXpediaHotelList() {
		assertThat(asyncTask.getStatus(), is(AsyncTask.Status.PENDING));
        asyncTask.execute();
        assertThat(asyncTask.getStatus(), is(AsyncTask.Status.RUNNING));
        Robolectric.getBackgroundScheduler().unPause();
        assertThat(asyncTask.getStatus(), is(AsyncTask.Status.FINISHED));
	}
}
