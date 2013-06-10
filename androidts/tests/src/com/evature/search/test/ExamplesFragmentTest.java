package com.evature.search.test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;
import android.support.v4.view.ViewPager;
import android.widget.ListView;
import android.widget.TextView;

import com.evature.search.R;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.controllers.activities.MainActivity.SwipeyTabsPagerAdapter;
import com.evature.search.models.ChatItemList;
import com.evature.search.views.fragments.ExamplesFragment;
import com.evature.util.DownloadUrl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;

@RunWith(RobolectricTestRunner.class)
public class ExamplesFragmentTest {
	
	ExamplesFragment examplesFragment;
	
	@Inject ChatItemList mChatListModel;
	
	DownloadUrl mockDownloader = mock(DownloadUrl.class);
	
	private class ExamplesTestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(DownloadUrl.class).toInstance(mockDownloader);
		}
	}
	
	
	@Before
    public void setup() {
		ShadowLog.stream = System.out;
		
		try {
			when(mockDownloader.get(anyString())).thenReturn("{\"status\":true, \"api_reply\":{} }");
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}
		
        Module roboGuiceModule = Modules.override(RoboGuice.newDefaultRoboModule(Robolectric.application)).with(new ExamplesTestModule());
        RoboGuice.setBaseApplicationInjector(Robolectric.application, RoboGuice.DEFAULT_STAGE,
                roboGuiceModule);
		
		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);
        
		MainActivity activity = Robolectric.buildActivity(MainActivity.class).create().start().get();
		ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewpager);
		viewPager.setCurrentItem(1);
		
    	SwipeyTabsPagerAdapter adapter = (SwipeyTabsPagerAdapter) viewPager.getAdapter();
		
    	examplesFragment = (ExamplesFragment) adapter.instantiateItem(viewPager, 1);
		
        assertThat(examplesFragment, notNullValue());
        assertThat(examplesFragment.getActivity(), notNullValue());
        assertThat(examplesFragment.getView(), notNullValue());
    }

	@Test
	public void testExamples() {
		

        ListView examplesListView = (ListView) examplesFragment.getView().findViewById(R.id.examples_list);

        String expectedChat = "3 Star hotels in NYC";
        assertEquals(expectedChat, examplesListView.getItemAtPosition(1).toString());

		assertEquals(0, mChatListModel.getItemList().size());
		
		assertTrue( examplesListView.performItemClick(examplesListView, 1, 0) );
		
		try {
			verify(mockDownloader).get( "http://freeapi.evature.com/api/v1.0?" +
					"from_speech&site_code=thack&api_key=thack-london-june-2012" +
					"&language=en&input_text=3+Star+hotels+in+NYC" +
					"&longitude=-1.0&latitude=-1.0");
			// TODO: this fails if testEvaIPAddr runs first!  (because IP-addr is stored in global state) - need to isolate tests!
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}

		// should have one item in chat
		assertEquals(1, mChatListModel.getItemList().size());
		assertEquals(mChatListModel.getItemList().get(0).getChat(), expectedChat);
		assertEquals(mChatListModel.getItemList().get(0).isEva(), false);
	}
    

}
