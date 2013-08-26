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
import org.mockito.Matchers;
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
import com.evature.search.models.chat.ChatItem;
import com.evature.search.models.chat.ChatItemList;
import com.evature.search.models.chat.ChatItem.ChatType;
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

	private String[] examples;
	
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
		examples = activity.getResources().getStringArray(R.array.examples);
		ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewpager);
		
    	SwipeyTabsPagerAdapter adapter = (SwipeyTabsPagerAdapter) viewPager.getAdapter();
		
    	examplesFragment = (ExamplesFragment) adapter.instantiateItem(viewPager, 0);
		
        assertThat(examplesFragment, notNullValue());
        assertThat(examplesFragment.getActivity(), notNullValue());
        assertThat(examplesFragment.getView(), notNullValue());
    }

	@Test
	public void testExamples() {
		

        ListView examplesListView = (ListView) examplesFragment.getView().findViewById(R.id.examples_list);

        String expectedChat = examples[1];
        assertEquals(expectedChat, examplesListView.getItemAtPosition(1).toString());

		assertEquals(0, mChatListModel.getItemList().size());
		
		assertTrue( examplesListView.performItemClick(examplesListView, 1, 0) );
		
		try {
			verify(mockDownloader).get( Matchers.contains("input_text=Hotel+in+NYC+on+Wednesday+for+5+nights"));
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}

		// should have one item in chat
		assertEquals(1, mChatListModel.getItemList().size());
		assertEquals(expectedChat,  mChatListModel.getItemList().get(0).getChat());
		assertEquals(true, mChatListModel.getItemList().get(0).getType() == ChatType.Me);
	}
    

}
