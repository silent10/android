package com.virtual_hotel_agent.search;

// This class is needed so that application crashes are automatically reported back home.
// The formKey is a Google Docs key that enables the application to fill in an online Google "Excel" form.

import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.joda.time.LocalDate;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;
import android.widget.Toast;

import com.ean.mobile.hotel.Hotel;
import com.ean.mobile.hotel.HotelInformation;
import com.ean.mobile.hotel.HotelList;
import com.ean.mobile.hotel.HotelRoom;
import com.ean.mobile.hotel.Reservation;
import com.ean.mobile.hotel.RoomOccupancy;
import com.ean.mobile.request.CommonParameters;
import com.evaapis.android.EvatureLocationUpdater;
import com.evature.util.Log;
import com.virtual_hotel_agent.components.S3DrawableBackgroundLoader;
import com.virtual_hotel_agent.search.controllers.activities.MainActivity;
import com.virtual_hotel_agent.search.models.expedia.ExpediaAppState;
import com.virtual_hotel_agent.search.models.expedia.XpediaDatabase;

@ReportsCrashes(formKey = "dDk0dGxhc1B6Z05vaXh3Q0xxWnhnZlE6MQ")
public class MyApplication extends Application {

	static XpediaDatabase mEvaDb = null;
	static ExpediaAppState  expediaAppState = new ExpediaAppState();
	//static EvaVayantDatabase mVayantDb = null;
	private static final String TAG = "MyApplication";
	private static Context context; // http://stackoverflow.com/a/5114361/78234
	public static boolean AcraInitialized = false;

	@Override
	public void onCreate() {

		// if (!BuildConfig.DEBUG) // Not when in debug mode!
		// The following line triggers the initialization of ACRA
		if (BuildConfig.DEBUG == false) {
			ACRA.init(this); 
			AcraInitialized = true;
		}
		Log.DEBUG = BuildConfig.DEBUG;
		Log.d(TAG, "onCreate");
		
		setupHttpConnectionStuff();
        CommonParameters.cid = "352395";
        CommonParameters.apiKey = "kg9fd64ndnfw4nggggp543e3";
        CommonParameters.signatureSecret = "CfyYPjmE";
        CommonParameters.customerUserAgent = "MOBILE_APP";
        CommonParameters.locale = Locale.US.toString();
        CommonParameters.currencyCode = Currency.getInstance(Locale.US).getCurrencyCode();
		
		EvatureLocationUpdater.initContext(this);
		
		super.onCreate();
		MyApplication.context = getApplicationContext();
	}

	@Override
	public void onLowMemory() {
		MainActivity.LogError(TAG, "onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		Log.d(TAG, "OnTerminate");
		super.onTerminate();
	}

	// public static void setEvaDb(EvaDatabase db) {
	// mEvaDb = db;
	// }

	public static Context getAppContext() {
		return MyApplication.context;
	}

//	public static EvaTravelportDatabase getFlightsDb() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	public static EvaVayantDatabase getJourneyDb() {
//		if (mVayantDb == null) {
//			mVayantDb = new EvaVayantDatabase();
//		}
//		return mVayantDb;
//	}

	
	
	
	/**
     * The query used for the most recent hotel api search.
     */
//    public static String searchQuery;

    /**
     * The number of adults intended for the room searched for with  {@link SampleApp#searchQuery}.
     */
    public static int numberOfAdults;

    /**
     * The number of children intended for the room searched for with {@link SampleApp#searchQuery}.
     */
    public static List<Integer> childAges;

    /**
     * The date intended for arrival at the destination searched for with {@link SampleApp#searchQuery}.
     */
    public static LocalDate arrivalDate = LocalDate.now();

    /**
     * The date intended for departure from the destination searched for with {@link SampleApp#searchQuery}.
     */
    public static LocalDate departureDate = arrivalDate.plusDays(1);

    /**
     * The cache key returned by the api for the current search.
     * Used for pagination through the found hotels that were not returned immediately by the api request.
     * Found using the api-lib class {@link com.ean.mobile.hotel.request.ListRequest}.
     */
    public static String cacheKey;

    /**
     * The cache location returned by the api for the current search.
     * Like {@link SampleApp#cacheKey}, used for pagination.
     * Found using the api-lib class {@link com.ean.mobile.hotel.request.ListRequest}.
     */
    public static String cacheLocation;

    /***
     * true if more pages are available
     */
    public static boolean moreResultsAvailable;
    
    /**
     * The hotel selected from {@link SampleApp#FOUND_HOTELS} for further inspection.
     * Should exist in {@link SampleApp#FOUND_HOTELS}.
     */
    public static Hotel selectedHotel;

    /**
     * The room that has been selected from the hotel information page that is intended to be
     * booked by the user.
     */
    public static HotelRoom selectedRoom;

    /**
     * The hotels found when searching with {@link SampleApp#searchQuery}.
     * Found using the api-lib class {@link com.ean.mobile.hotel.request.ListRequest}.
     */
    public static final List<Hotel> FOUND_HOTELS = new ArrayList<Hotel>();
    
    public static final HashMap<Long, Hotel> HOTEL_ID_MAP = new HashMap<Long, Hotel>();

    /**
     * The in-memory cache of extended hotel information, to make display of the HotelInformation page faster
     * for hotels that have already been loaded.
     */
    public static final LruCache<Long, HotelInformation> EXTENDED_INFOS = new LruCache<Long, HotelInformation>(10);

    /**
     * The rooms found for each hotel. The Keys are the hotelids for the hotels that have rooms found,
     * the values are the list of rooms for that hotel.
     */
    public static final LruCache<Long, List<HotelRoom>> HOTEL_ROOMS
            = new LruCache<Long, List<HotelRoom>>(5);

    // fill up to 30% of maxMemory with images
 	private final static int PHOTO_CACHE_SIZE = (int) (0.4 * Runtime.getRuntime().maxMemory() / 1024);
    public static LruCache<String, Bitmap> HOTEL_PHOTOS = new LruCache<String, Bitmap>(PHOTO_CACHE_SIZE) {
		@Override
		protected int sizeOf(final String key, final Bitmap bitmap) {
			// The cache size will be measured in kilobytes rather than
			// number of items.
			return bitmap.getByteCount()/ 1024;
		}
	};
	
    
    /**
     * The in-memory cache of drawables found for HotelImageTuples. Utilizes {@link HotelImageDrawableMap}. Should
     * instead use some sort of tiered local caching mechanism, but for timing reasons, this remains a custom map
     * implementation based on {@link HashMap}.
     *
     * Note: The {@link HotelImageDrawableMap} will automatically add any HotelImageTuple as a key if it doesn't already
     * exist in the map on usage of .get() and then return the new HotelImageDrawable object constructed for it.
     */
//    public static final Map<HotelImageTuple, HotelImageDrawable> IMAGE_DRAWABLES
//            = Collections.synchronizedMap(new HotelImageDrawableMap());

    private static final Set<Reservation> RESERVATIONS = new TreeSet<Reservation>();



    /**
     * Gets the number of adults and children represented as a RoomOccupancy for easy use in the api.
     * @return The room occupancy directly representing the
     *          {@link SampleApp#numberOfAdults} and {@link SampleApp#childAges}.
     */
    public static RoomOccupancy occupancy() {
        return new RoomOccupancy(numberOfAdults, childAges);
    }

    /**
     * Sends a toast to the user stating that the request has been redirected, likely due to an issue with
     * their internet connection requiring login, and that they should check that as well.
     * @param context The context into which to display the toast. Should be the application context.
     */
    public static void sendRedirectionToast() {
    	final Context context = MyApplication.getAppContext();
    	if (context != null) {
    		Toast.makeText(context, R.string.redirected, Toast.LENGTH_LONG).show();
    	}
    }

    /**
     * Resets just the dates to the default, today+7 and today+9.
     */
    public static void resetDates() {
        arrivalDate = LocalDate.now().plusDays(7);
        departureDate = arrivalDate.plusDays(2);
    }

    /**
     * Completely clears the search and sets the fields filled by a search to their defaults.
     */
    public static void clearSearch() {
//        searchQuery = null;
        numberOfAdults = 0;
        childAges = null;
        resetDates();
        FOUND_HOTELS.clear();
        HOTEL_ID_MAP.clear();
        selectedHotel = null;
        selectedRoom = null;
        EXTENDED_INFOS.evictAll();
        HOTEL_ROOMS.evictAll();
        
        //IMAGE_DRAWABLES.clear();
        S3DrawableBackgroundLoader.getInstance().Reset();
    }

    /**
     * Updates the {@link SampleApp#FOUND_HOTELS} list with new hotels as loaded from a request spawning from a list
     * request constructed by {@link com.ean.mobile.hotel.request.ListRequest#ListRequest(String, String)}, the
     * ean api's concept of "paging".
     * Overloads to {@link SampleApp#updateFoundHotels(com.ean.mobile.hotel.HotelList, boolean)}, with clearOnUpdate
     * set to false.
     * @param hotelList The list of hotels to add to {@link SampleApp#FOUND_HOTELS}.
     */
    public static void updateFoundHotels(final HotelList hotelList) {
        updateFoundHotels(hotelList, false);
    }

    /**
     * Adds the hotelList to the {@link SampleApp#FOUND_HOTELS} list, optionally clearing the extant contents. Also
     * applies the cacheKey and cacheLocation from the hotelList.
     * @param hotelList The list of hotels to add to {@link SampleApp#FOUND_HOTELS}.
     * @param clearOnUpdate Whether or not to clear {@link SampleApp#FOUND_HOTELS}.
     */
    public static synchronized void updateFoundHotels(final HotelList hotelList, final boolean clearOnUpdate) {
        if (clearOnUpdate) {
            FOUND_HOTELS.clear();
            HOTEL_ID_MAP.clear();
        }
        if (hotelList != null) {
            FOUND_HOTELS.addAll(hotelList.hotels);
            for (Hotel hotel : hotelList.hotels) {
            	HOTEL_ID_MAP.put(hotel.hotelId, hotel);
            }
            cacheKey = hotelList.cacheKey;
            cacheLocation = hotelList.cacheLocation;
            Log.d(TAG, "Added "+hotelList.hotels.size()+" hotels, list now at "+FOUND_HOTELS.size()+" / "
            			+hotelList.totalNumberOfResults+"  cacheKey="+cacheKey+"  cacheLoc="+cacheLocation);
            moreResultsAvailable = hotelList.moreResults;
        }
    }

    /**
     * Adds a reservation object to an application-persistent, in-memory cache of reservations, sorted by date.
     * @param reservation The reservation to save.
     */
    public static void addReservationToCache(final Reservation reservation) {
        RESERVATIONS.add(reservation);
    }

    /**
     * Gets the most recent reservation from the collection containing them.
     * @return The reservation requested, or null if there are no reservations.
     */
    public static Reservation getLatestReservation() {
        return RESERVATIONS.size() == 0 ? null : RESERVATIONS.iterator().next();
    }


    private void setupHttpConnectionStuff() {
        // exists due to advice found at http://android-developers.blogspot.com/2011/09/androids-http-clients.html.
        // HTTP connection reuse which was buggy pre-froyo
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
        // signature based url makes cache useless - different URL every time
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//            try {
//                final File httpCacheDir = new File(getCacheDir(), "http");
//                HttpResponseCache.install(httpCacheDir, TEN_MEGABYTES);
//            } catch (IOException ioe) {
//                Log.e(Constants.LOG_TAG, "Could not install http cache on this device.");
//            }
//        }
    }
}
