package com.evature.search.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

import com.evaapis.EvaApiReply;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.search.models.ChatItemList;
import com.evature.search.models.expedia.XpediaProtocol;
import com.evature.util.DownloadUrl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;

@RunWith(RobolectricTestRunner.class)
public class EvaReplyTest {
	
	DownloadUrl mockDownloader = mock(DownloadUrl.class);
	XpediaProtocol mockProtocol = mock(XpediaProtocol.class);
	
	MainActivity mActivity;
	
	@Inject ChatItemList mChatListModel;
	
	private class EvaReplyTestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(DownloadUrl.class).toInstance(mockDownloader);
			bind(XpediaProtocol.class).toInstance(mockProtocol);
		}
	}

	
	@Before
    public void setup() {
		ShadowLog.stream = System.out;
		
		
        Module roboGuiceModule = Modules.override(RoboGuice.newDefaultRoboModule(Robolectric.application)).with(new EvaReplyTestModule());
        RoboGuice.setBaseApplicationInjector(Robolectric.application, RoboGuice.DEFAULT_STAGE,
                roboGuiceModule);
		
		RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);
        
		mActivity = Robolectric.buildActivity(MainActivity.class).create().start().get();
//		ViewPager viewPager = (ViewPager) activity.findViewById(R.id.viewpager);
		
//    	SwipeyTabsPagerAdapter adapter = (SwipeyTabsPagerAdapter) viewPager.getAdapter();
    }

	
	@Test
	public void testEvaHotelResult() {
		try {
			when(mockDownloader.get(anyString())).thenReturn(
			"{      \"status\":true, " +
		    "       \"api_reply\":{ " +
			"			   \"ean\": {" +
			"			     \"longitude\": \"-74.00597\"," +
			"			     \"maxStarRating\": \"3\"," +
			"			     \"propertyCategory\": \"1\"," +
			"			     \"latitude\": \"40.71427\"," +
			"			     \"minStarRating\": \"3\"" +
			"			   }," +
			"			   \"Say It\": \"3 stars hotel in New York City, New York\"," +
			"			   \"ProcessedText\": \"3 Star hotels in NYC\"," +
			"			   \"Locations\": [" +
			"			     {" +
			"			       \"Name\": \"Rehovot, Israel (GID=293725)\"," +
			"			       \"Next\": 10," +
			"			       \"Home\": \"GPS\"," +
			"			       \"Index\": 0," +
			"			       \"Type\": \"City\"," +
			"			       \"Latitude\": 31.89421," +
			"			       \"Longitude\": 34.81199," +
			"			       \"Geoid\": 293725," +
			"			       \"Airports\": \"TLV,SDV,JRS,BEV,MTZ\"," +
			"			       \"Derived From\": \"GPS\"," +
			"			       \"Country\": \"IL\"" +
			"			     }," +
			"			     {" +
			"			       \"Name\": \"New York City, New York, United States (GID=5128581)\"," +
			"			       \"Index\": 10," +
			"			       \"Type\": \"City\"," +
			"			       \"Latitude\": 40.71427," +
			"			       \"Actions\": [" +
			"			         \"Get Accommodation\"" +
			"			       ]," +
			"			       \"Longitude\": -74.00597," +
			"			       \"Geoid\": 5128581," +
			"			       \"Airports\": \"EWR,JFK,LGA,JRB\"," +
			"			       \"All Airports Code\": \"NYC\"," +
			"			       \"Country\": \"US\"" +
			"			     }" +
			"			   ]," +
			"			   \"Hotel Attributes\": {" +
			"			     \"Accommodation Type\": \"Hotel\"," +
			"			     \"Quality\": [ 3, 3 ]" +
			"			   }" +
			"			 }" +
			"}");
			
			when(mockProtocol.getExpediaAnswer(notNull(EvaApiReply.class), anyString())).thenReturn("{}"); // TODO: return data for hotel list
		
			assertEquals(0, mChatListModel.getItemList().size());
		
			mActivity.searchWithText("!!Testing Eva search");
			
			verify(mockDownloader).get(anyString());
			verify(mockProtocol).getExpediaAnswer(notNull(EvaApiReply.class), anyString());
		
			// verify chat model holds the eva reply say-it
			assertEquals(1, mChatListModel.getItemList().size());
			assertEquals("3 stars hotel in New York City, New York",  mChatListModel.getItemList().get(0).getChat());
			assertEquals(true, mChatListModel.getItemList().get(0).isEva());

		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}
	}
	
	@Ignore
	@Test
	public void testFlightSearch() {
		// should trigger sabre search (?)
	}
	
	@Ignore
	@Test
	public void testFlightAndHotelSearch() {
		// should trigger sabre search (?) + expedia search 
	}
	
	@Ignore
	@Test
	public void testCarSearch() {
		// sabre search? 
	}
	
	@Ignore
	@Test
	public void testRailSearch() {
		// sabre search? 
	}
	
	@Ignore
	@Test
	public void testCruiseSearch() {
		// sabre search? 
	}
}
