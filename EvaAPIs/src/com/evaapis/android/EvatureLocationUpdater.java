package com.evaapis.android;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


public class EvatureLocationUpdater implements LocationListener {

	private static final int UPDATE_DELAY = 5 * 60 * 1000; // Five minutes
	private static final int UPDATE_DISTANCE = 5 * 1000; // Five kilometers
	private static final String TAG = "EvatureLocationUpdater";
	
	private LocationManager locationManager;
	private Location currentLocation = null;

	private static EvatureLocationUpdater thisInstance = null;
	private static Context appContext = null;

	public EvatureLocationUpdater() {
		assert(thisInstance == null); // Guice should make sure only once instance exists
		thisInstance = this;
		if (appContext != null)
			thisInstance.locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public static void initContext(Context context) {
		appContext = context;
		if (thisInstance != null)
			thisInstance.locationManager = (LocationManager) appContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public void stopGPS() {
		locationManager.removeUpdates(this);
	}

	public void startGPS() {
		boolean gps_enabled = false;
		boolean network_enabled = false;

		// Check if GPS or network provider enabled
		try {
			gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			Log.w(TAG, "No GPS_PROVIDER?");
		}
		try {
			network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			Log.w(TAG, "No NETWORK_PROVIDER?");
		}

		// if enabled, set updates from GPS location provider
		if (gps_enabled)
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_DELAY, UPDATE_DISTANCE, this);

		if (network_enabled)
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_DELAY, UPDATE_DISTANCE,
					this);

		currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		if (currentLocation == null)
			currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
	}

	public static double getLatitude() {
		if (thisInstance != null && thisInstance.currentLocation != null)
			return thisInstance.currentLocation.getLatitude();
		else
			return -1;
	}

	public static double getLongitude() {
		if (thisInstance != null && thisInstance.currentLocation != null)
			return thisInstance.currentLocation.getLongitude();
		else
			return -1;
	}

	@Override
	public void onLocationChanged(Location location) {
		if (isBetterLocation(location, currentLocation)) {
			currentLocation = new Location(location);
			String my_text = String.valueOf(location.getLatitude()) + " / " + String.valueOf(location.getLongitude());
			Log.d(TAG, "Got location: " + my_text);
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		Toast.makeText(appContext, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onProviderEnabled(String provider) {
		Toast.makeText(appContext, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Toast.makeText(appContext, "Status Changed " + provider, Toast.LENGTH_SHORT).show();
	}

	private boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > UPDATE_DELAY;
		boolean isSignificantlyOlder = timeDelta < -UPDATE_DELAY;
		boolean isNewer = timeDelta > 0;

		// If it's been more than defined delay since the current location,
		// use the new location because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than defined delay older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

}
