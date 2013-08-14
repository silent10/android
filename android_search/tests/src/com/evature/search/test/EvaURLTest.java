package com.evature.search.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowLog;

import roboguice.RoboGuice;
import roboguice.config.DefaultRoboModule;
import roboguice.inject.RoboInjector;
import android.app.Application;
import android.location.Location;
import android.location.LocationManager;

import com.evaapis.EvatureLocationUpdater;
import com.evature.search.controllers.activities.MainActivity;
import com.evature.util.DownloadUrl;
import com.evature.util.ExternalIpAddressGetter;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.util.Modules;

@RunWith(RobolectricTestRunner.class)
public class EvaURLTest {

	DownloadUrl mockDownloader = mock(DownloadUrl.class);
		
	private class URLTestModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(DownloadUrl.class).toInstance(mockDownloader);
		}
	}
	
	@Before
    public void setUp()  {
		ShadowLog.stream = System.out; // see android logs in console

		// setup injector to inject mock objects for tests
        Module roboGuiceModule = Modules.override(RoboGuice.newDefaultRoboModule(Robolectric.application)).with(new URLTestModule());
        RoboGuice.setBaseApplicationInjector(Robolectric.application, RoboGuice.DEFAULT_STAGE,
                roboGuiceModule);
        RoboInjector injector = RoboGuice.getInjector(Robolectric.application);
        injector.injectMembers(this);

		ShadowLocationManager slm = Robolectric.shadowOf(mLocationManager);
		slm.setProviderEnabled(LocationManager.GPS_PROVIDER, true);
		slm.setProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
        
		// Note: not calling "resume" because I don't want the GPS and IP-getter to start running yet 
		mActivity = Robolectric.buildActivity(MainActivity.class).create().start().get();
	}
	
    @After
    public void tearDown() {
        RoboGuice.util.reset();
        Application app = Robolectric.application;
        DefaultRoboModule defaultModule = RoboGuice.newDefaultRoboModule(app);
        RoboGuice.setBaseApplicationInjector(app, RoboGuice.DEFAULT_STAGE, defaultModule);
    }
    
	
	private MainActivity mActivity;
	
	@Inject LocationManager mLocationManager;
	@Inject EvatureLocationUpdater mLocationUpdater;
	
	@Test
	public void testEvaGPS() {
		mLocationUpdater.startGPS();
		
		try {
			when(mockDownloader.get(anyString())).thenReturn("{\"status\":true, \"api_reply\":{} }");
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}


		ShadowLocationManager slm = Robolectric.shadowOf(mLocationManager);

		// simulate location update
		Location loc = new Location(LocationManager.GPS_PROVIDER);
		loc.setLongitude(12.34);
		loc.setLatitude(56.78);
		slm.simulateLocation(loc);

		mLocationUpdater.stopGPS();

		// this Location shouldn't be used - updater is stopped
		loc = new Location(LocationManager.GPS_PROVIDER);
		loc.setLongitude(66.66);
		loc.setLatitude(77.77);
		slm.simulateLocation(loc);

		
		mActivity.searchWithText("!!Testing Eva search");
		
		try {
			verify(mockDownloader).get( Matchers.contains("longitude=12.34&latitude=56.78"));
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}
	}
	
	@Inject ExternalIpAddressGetter mExternalIpAddressGetter;
	
	@Test
	public void testEvaIPAddr() {
	
		try {
			when(mockDownloader.get("http://freeapi.evature.com/whatismyip")).thenReturn("{ \"ip_address\": \"12.34.56.78\" }");
			mExternalIpAddressGetter.executeGetIpAddr();
			verify(mockDownloader).get( "http://freeapi.evature.com/whatismyip" );
			assertEquals("12.34.56.78", ExternalIpAddressGetter.getExternalIpAddr());
			
			when(mockDownloader.get(anyString())).thenReturn("{}");
			mActivity.searchWithText("!!Testing Eva search");
			
			verify(mockDownloader).get( Matchers.contains("ip_addr=12.34.56.78"));

			
			ExternalIpAddressGetter.setExternalIpAddr(null); // reset back to make other tests consistent
		} catch (IOException e) {
			fail(); // shoudln't get here because mock downloader does not actually cause IO so has no IO exception... but must use "catch" to make compiler happy
		}
	}
}
