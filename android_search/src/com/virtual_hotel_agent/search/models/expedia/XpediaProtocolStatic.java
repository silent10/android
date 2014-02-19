package com.virtual_hotel_agent.search.models.expedia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.evature.util.Log;

import com.evaapis.android.EvatureLocationUpdater;
import com.evaapis.crossplatform.EvaApiReply;
import com.evaapis.crossplatform.RequestAttributes.SortOrderEnum;
import com.evature.util.ExternalIpAddressGetter;
import com.virtual_hotel_agent.search.MyApplication;

public class XpediaProtocolStatic {

	private final static String TAG = XpediaProtocolStatic.class.getSimpleName();
	private static String minorRev = "26";

	/**
	 * {@link StatusLine} HTTP status code when no server error has occurred.
	 */
	private static final int HTTP_STATUS_OK = 200;

	private static final int HTTP_TIMEOUT = 8000;

	private final static String EXPEDIA_URL = "http://api.ean.com/ean-services/rs/hotel/v3/";
	private final static String HOTEL_LIST_URL = EXPEDIA_URL + "list?";
	private final static String HOTEL_INFO_URL = EXPEDIA_URL + "info?";
	private final static String HOTEL_AVAILABILITY_URL = EXPEDIA_URL + "avail?";
	private static final String CONSTANT_HTTP_PARAMS = "locale=en_US&"
			+ "customerUserAgent=MOBILE_APP"
			+ "&minorRev=" + minorRev;
	
	public static String sessionId=null;

	// protected synchronized String getUrlContent(String url, List<NameValuePair> nameValuePairs){
	// Create client and set our specific user-agent string

	static String getApiKey() {
		return MyApplication.getExpediaApiKey();
	}

	static String getSecret() {
		return MyApplication.getExpediaSecret();
	}

	static String getClientId() {
		return MyApplication.getExpediaClientId();
	}

	public static Bitmap download_Image(String path) {

		URL url = null;
		Bitmap bmp;

		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}

		URLConnection connection = null;

		Object response = null;
		try {
			connection = url.openConnection();
			connection.setUseCaches(true); //
			response = connection.getContent();
			bmp = BitmapFactory.decodeStream((InputStream) response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError e) {
			Log.e(TAG, "Out of memory");
			e.printStackTrace();
			return null;
		}

		return bmp;
	}

	private static String getSignature() {
		String sig = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			long timeInSeconds = (System.currentTimeMillis() / 1000);
			String input = getApiKey() + getSecret() + timeInSeconds;
			md.update(input.getBytes());
			sig = String.format("%032x", new BigInteger(1, md.digest()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			sig = "";
		}
		return sig;
	}
	
	private static String getContantHttpParams() {
		String urlString = "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS;
		String ipAddr = ExternalIpAddressGetter.getExternalIpAddr();
		if (ipAddr != null) {
			urlString += "&customerIpAddress="+ipAddr;
		}
		if (sessionId == null) {
			sessionId = UUID.randomUUID().toString(); 
		}
		urlString += "&customerSessionId="+sessionId;
		return urlString;
	}

	static public JSONObject getExpediaHotelInformation(int hotelId, String currencyCode) {
		String urlString = HOTEL_INFO_URL;
		urlString += getContantHttpParams();
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		//urlString += "&options=0";

		return executeWithTimeout(urlString);
	}

	public static JSONObject getExpediaAnswer(EvaApiReply apiReply, ExpediaRequestParameters db, String currencyCode) {
		Log.i(TAG, "getExpediaAnswer()");
		if (apiReply == null)
			return null;

		double longitude, latitude;
				
		longitude = EvatureLocationUpdater.getLongitude();
		latitude = EvatureLocationUpdater.getLatitude();
		
		String params = "";

		if (apiReply.ean == null)
			apiReply.ean = new HashMap<String, String>();
		
		if ((!apiReply.ean.containsKey("latitude") || !apiReply.ean.containsKey("longitude")) &&
				!apiReply.ean.containsKey("city") && !!apiReply.ean.containsKey("destinationId") &&
				!apiReply.ean.containsKey("destinationString") 
				) {
			// no location returned - use the one from the phone
			if (longitude != -1) {
				apiReply.ean.put("latitude", String.valueOf(latitude));
				apiReply.ean.put("longitude", String.valueOf(longitude));
			}
		}

		for (Map.Entry<String, String> entry : apiReply.ean.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			params += "&" + key + "=" + value;
		}

		params += "&maxRatePlanCount=2";
		
		if (apiReply.requestAttributes != null && apiReply.requestAttributes.sortBy != null &&
				params.contains("&sort=") == false) {
			switch (apiReply.requestAttributes.sortBy) {
				case price:
				case price_per_person:
					if (apiReply.requestAttributes.sortOrder == SortOrderEnum.descending ||
						apiReply.requestAttributes.sortOrder == SortOrderEnum.reverse) {
						params += "&sort=PRICE_REVERSE";
					}
					else {
						params += "&sort=PRICE";
					}
					break;
				case stars:
				case rating:
				case popularity:
				case guest_rating:
				case recommendations:
					if (apiReply.requestAttributes.sortOrder == SortOrderEnum.descending ||
					apiReply.requestAttributes.sortOrder == SortOrderEnum.reverse) {
						params += "&sort=QUALITY_REVERSE";
					}
					else {
						params += "&sort=QUALITY";
					}
					break;
				case name:
					params += "&sort=ALPHA";
					break;
				case distance:
					if (apiReply.ean.containsKey("latitude")) {
						params += "&sort=PROXIMITY";
					}
					break;
			}
		}

		// will create a new sessionId
		sessionId = null;

		String urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += getContantHttpParams();
		urlString += "&currencyCode=" + currencyCode;
		urlString += params;
		
		urlString += "&room1="+db.mNumberOfAdultsParam;
		int numOfChildren = db.getNumberOfChildrenParam();
		if (numOfChildren > 0) {
			urlString += ","+db.getAgeChild1();
			if (numOfChildren > 1) {
				urlString += ","+db.getAgeChild2();
				if (numOfChildren > 2) {
					urlString += ","+db.getAgeChild3();
				}
			}
		}
		
		return executeWithTimeout(urlString);
	}

	private static JSONObject executeWithTimeout(String url) {
		
		Log.d(TAG, "Fetching "+url);
		
		HttpGet request = new HttpGet(url);
		request.addHeader("Accept-Encoding","gzip");

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);

		DefaultHttpClient client = new DefaultHttpClient();
		client.setParams(params);
		try {
			HttpResponse response = client.execute(request);
			
			
			
			// Check if server response is valid
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != HTTP_STATUS_OK) {
				Log.e(TAG, "Status code from server: "+statusCode);
				Log.e(TAG, "Content: "+EntityUtils.toString(response.getEntity()));
				return null;
			}
	
			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			Header contentEncoding = response.getFirstHeader("Content-Encoding");
			if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
				inputStream = new GZIPInputStream(inputStream);
			}
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			byte[] sBuffer = new byte[512];

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			String result = new String(content.toByteArray());
			Log.i(TAG, "Result is "+result);
			JSONObject jResult;
			try {
				jResult = new JSONObject(result);
				sessionId = jResult.optString("customerSessionId", null);
				if (jResult.has("EanWSError")) {
					Log.w(TAG, "Error from expedia: "+jResult.getJSONObject("EanWSError").toString(2));
				}
			}catch(JSONException e) {
				Log.e(TAG, "Error parsing json of expedia result", e);
				jResult = null;
				sessionId = null;
			}
			
			return jResult;
		}
		catch(IOException e) {
			Log.e(TAG, "Problem communicating with API", e);
			return null;
		} 
	}

	public static JSONObject getRoomInformationForHotel(int hotelId, ExpediaRequestParameters db,
			String currencyCode) {
		String arrivalDateParam = db.mArrivalDateParam;
		String departureDateParam = db.mDepartureDateParam;
		int numOfAdults = db.mNumberOfAdultsParam;
		String urlString = HOTEL_AVAILABILITY_URL;
		urlString += getContantHttpParams();
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		urlString += "&arrivalDate=" + arrivalDateParam + "&departureDate=" + departureDateParam;
		urlString += "&room1="+numOfAdults;
		int numOfChildren = db.getNumberOfChildrenParam();
		if (numOfChildren > 0) {
			urlString += ","+db.getAgeChild1();
			if (numOfChildren > 1) {
				urlString += ","+db.getAgeChild2();
				if (numOfChildren > 2) {
					urlString += ","+db.getAgeChild3();
				}
			}
		}
		urlString += "&options=0";

		// LayoutInflater li = this.getLayoutInflater();
		// LinearLayout foot = (LinearLayout) li.inflate(R.layout.listfoot, null);
		// mHotelListView.addFooterView(foot);

		
		return executeWithTimeout(urlString);
	}

	public static JSONObject getExpediaNext(String mQueryString, String currencyCode) {
		String urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += getContantHttpParams() + "&currencyCode=" + currencyCode;
		urlString += mQueryString;

		return executeWithTimeout(urlString);
	}


}