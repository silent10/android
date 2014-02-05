package com.evature.search.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import com.evature.util.DownloadUrl;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.models.chat.ChatItemList;
import com.virtual_hotel_agent.search.models.chat.ChatItem.ChatType;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocol;

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
	public void testEvaHotelMissingDateResult() {
		try {
			when(mockDownloader.get(anyString())).thenReturn(
			"{ "+
			"  \"message\": \"Successful Parse\"," +
			"  \"session_id\": \"8829be61-001b-11e3-96ce-1231390c5033\"," +
			"  \"rid\": null," +
			"  \"status\": true," +
			"  \"transaction_key\": \"11e3-001b-87fc1d99-96ce-1231390c5033\"," +
			"  \"ver\": \"v1.0.3585\"," +
			"  \"api_reply\": {" +
			"    \"SayIt\": \"3 stars hotel in New York City, New York\"," +
			"    \"Flow\": [" +
			"      {" +
			"        \"SayIt\": \"When would you like to arrive to New York City, New York?\"," +
			"        \"QuestionCategory\": \"Missing Date\"," +
			"        \"QuestionSubCategory\": \"Arrival\"," +
			"        \"QuestionType\": \"Open\"," +
			"        \"Type\": \"Question\"," +
			"        \"RelatedLocations\": [" +
			"          1" +
			"        ]" +
			"      }" +
			"    ]," +
			"    \"ean\": {" +
			"      \"longitude\": \"-74.00597\"," +
			"      \"maxStarRating\": \"3\"," +
			"      \"propertyCategory\": \"1\"," +
			"      \"latitude\": \"40.71427\"," +
			"      \"minStarRating\": \"3\"" +
			"    }," +
			"    \"ProcessedText\": \"3 Star hotels in NYC\"," +
			"    \"Locations\": [" +
			"      {" +
			"        \"Name\": \"Ness Ziona, Israel (GID=294074)\"," +
			"        \"Next\": 10," +
			"        \"Home\": \"GPS\"," +
			"        \"Index\": 0," +
			"        \"Type\": \"City\"," +
			"        \"Latitude\": 31.92933," +
			"        \"Longitude\": 34.79868," +
			"        \"Geoid\": 294074," +
			"        \"Airports\": \"TLV,SDV,JRS,BEV,MTZ\"," +
			"        \"Derived From\": \"GPS\"," +
			"        \"Country\": \"IL\"" +
			"      }," +
			"      {" +
			"        \"Name\": \"New York City, New York, United States (GID=5128581)\"," +
			"        \"Index\": 10," +
			"        \"Type\": \"City\"," +
			"        \"Latitude\": 40.71427," +
			"        \"Actions\": [" +
			"          \"Get Accommodation\"" +
			"        ]," +
			"        \"Longitude\": -74.00597," +
			"        \"Geoid\": 5128581," +
			"        \"Airports\": \"EWR,JFK,LGA,JRB\"," +
			"        \"All Airports Code\": \"NYC\"," +
			"        \"Country\": \"US\"" +
			"      }" +
			"    ]," +
			"    \"Hotel Attributes\": {" +
			"      \"Accommodation Type\": \"Hotel\"," +
			"      \"Quality\": [" +
			"        3," +
			"        3" +
			"      ]" +
			"    }" +
			"  }," +
			"  \"input_text\": \"3 Star hotels in NYC\"" +
			"}" );
			
			when(mockProtocol.getExpediaAnswer(notNull(EvaApiReply.class), anyString())).thenReturn("{}"); // TODO: return data for hotel list
		
			assertEquals(0, mChatListModel.size());
		
			mActivity.eva.searchWithText("!!Testing Eva search");
			
			verify(mockDownloader).get(anyString());
			verify(mockProtocol, never()).getExpediaAnswer(notNull(EvaApiReply.class), anyString());
		
			// verify chat model holds the eva reply say-it
			assertEquals(1, mChatListModel.size());
			assertEquals("When would you like to arrive to New York City, New York?",  mChatListModel.get(0).getChat());
			assertEquals(true, mChatListModel.get(0).getType() == ChatType.DialogQuestion);

		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}
	}
	
	@Test
	public void testEvaHotelResult() {
		try {
			when(mockDownloader.get(anyString())).thenReturn(
			"{" +
			"   \"message\": \"Successful Parse\"," +
			"   \"session_id\": \"afa0f4d4-0038-11e3-96ce-1231390c5033\"," +
			"   \"rid\": null," +
			"   \"status\": true," +
			"   \"transaction_key\": \"11e3-0038-af42ba73-96ce-1231390c5033\"," +
			"   \"ver\": \"v1.0.3585\"," +
			"   \"api_reply\": {" +
			"     \"SayIt\": \"hotel in New York City, New York, arriving August 14th, 2013 for 5 nights\"," +
			"     \"Flow\": [" +
			"       {" +
			"         \"SayIt\": \"Hotel in New York City, New York, arriving August 14th, 2013 for 5 nights\"," +
			"         \"Type\": \"Hotel\"," +
			"         \"RelatedLocations\": [" +
			"           1" +
			"         ]" +
			"       }" +
			"     ]," +
			"     \"ean\": {" +
			"       \"longitude\": \"-74.00597\"," +
			"       \"arrivalDate\": \"08/14/2013\"," +
			"       \"propertyCategory\": \"1\"," +
			"       \"latitude\": \"40.71427\"," +
			"       \"departureDate\": \"08/19/2013\"" +
			"     }," +
			"     \"ProcessedText\": \"Hotel in NYC on Wednesday for 5 nights\"," +
			"     \"Locations\": [" +
			"       {" +
			"         \"Name\": \"Ness Ziona, Israel (GID=294074)\"," +
			"         \"Next\": 10," +
			"         \"Home\": \"GPS\"," +
			"         \"Index\": 0," +
			"         \"Departure\": {" +
			"           \"Calculated\": true," +
			"           \"Date\": \"2013-08-14\"" +
			"         }," +
			"         \"Type\": \"City\"," +
			"         \"Latitude\": 31.92933," +
			"         \"Longitude\": 34.79868," +
			"         \"Geoid\": 294074," +
			"         \"Airports\": \"TLV,SDV,JRS,BEV,MTZ\"," +
			"         \"Derived From\": \"GPS\"," +
			"         \"Country\": \"IL\"" +
			"       }," +
			"       {" +
			"         \"Name\": \"New York City, New York, United States (GID=5128581)\"," +
			"         \"Index\": 10," +
			"         \"Type\": \"City\"," +
			"         \"Actions\": [" +
			"           \"Get Accommodation\"" +
			"         ]," +
			"         \"Arrival\": {" +
			"           \"Date\": \"2013-08-14\"" +
			"         }," +
			"         \"Latitude\": 40.71427," +
			"         \"Longitude\": -74.00597," +
			"         \"Geoid\": 5128581," +
			"         \"Airports\": \"EWR,JFK,LGA,JRB\"," +
			"         \"All Airports Code\": \"NYC\"," +
			"         \"Stay\": {" +
			"           \"Delta\": \"days=+5\"" +
			"         }," +
			"         \"Country\": \"US\"" +
			"       }" +
			"     ]," +
			"     \"Hotel Attributes\": {" +
			"       \"Accommodation Type\": \"Hotel\"" +
			"     }" +
			"   }," +
			"   \"input_text\": \"Hotel in NYC on Wednesday for 5 nights\"" +
			" }");
			
			when(mockProtocol.getExpediaAnswer(notNull(EvaApiReply.class), anyString())).thenReturn("{}"); // TODO: return data for hotel list
		
			assertEquals(0, mChatListModel.size());
		
			mActivity.eva.searchWithText("!!Testing Eva search");
			
			verify(mockDownloader).get(anyString());
			verify(mockProtocol).getExpediaAnswer(notNull(EvaApiReply.class), anyString());
		
			// verify chat model holds the eva reply say-it
			assertEquals(1, mChatListModel.size());
			assertEquals("Hotel in New York City, New York, arriving August 14th, 2013 for 5 nights",  mChatListModel.get(0).getChat());
			assertEquals(true, mChatListModel.get(0).getType() == ChatType.VirtualAgent);

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
