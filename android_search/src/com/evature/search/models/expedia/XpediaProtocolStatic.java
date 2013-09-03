package com.evature.search.models.expedia;

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
import java.util.Iterator;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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
import android.util.Log;

import com.evaapis.EvaApiReply;
import com.evaapis.EvatureLocationUpdater;
import com.evaapis.RequestAttributes.SortOrderEnum;
import com.evature.search.MyApplication;

public class XpediaProtocolStatic {

	private final static String TAG = XpediaProtocolStatic.class.getSimpleName();
	private static String minorRev = "16";

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
			+ "customerUserAgent=Mozilla%2F5.0+%28Windows+NT+5.1%29+AppleWebKit%2F534.24+%28KHTML%2C+like+Gecko%29+Chrome%2F11.0.696.71+Safari%2F534.24"
			+ "&customerIpAddress=127.0.0.1" + "&minorRev=" + minorRev;

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

	static public String getExpediaHotelInformation(int hotelId, String currencyCode) {
		String urlString = HOTEL_INFO_URL;
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS;
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		urlString += "&options=0";

		return executeWithTimeout(urlString);
	}

	public static String getExpediaAnswer(EvaApiReply apiReply, String currencyCode) {
		Log.i(TAG, "getExpediaAnswer()");
		if (apiReply == null)
			return null;

		double longitude, latitude;
				
		longitude = EvatureLocationUpdater.getLongitude();
		latitude = EvatureLocationUpdater.getLatitude();
		
		if (longitude == -1) {
			longitude = 10000;
			latitude = 10000;
		}

		byte[] sBuffer = new byte[512];
		String params = "";

		if ((apiReply.ean == null) || !apiReply.ean.containsKey("latitude") || !apiReply.ean.containsKey("longitude")) {
			if (apiReply.ean == null)
				apiReply.ean = new HashMap<String, String>();
			apiReply.ean.put("latitude", String.valueOf(latitude));
			apiReply.ean.put("longitude", String.valueOf(longitude));
		}

		for (Map.Entry<String, String> entry : apiReply.ean.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			params += "&" + key + "=" + value;
		}

		params += "&maxRatePlanCount=2";
		
		if (apiReply.requestAttributes != null && apiReply.requestAttributes.sortBy != null) {
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


		String urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS + "&currencyCode=" + currencyCode;
		urlString += params;
		
		return executeWithTimeout(urlString);
	}

	private static String executeWithTimeout(String url) {
		
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
			return new String(content.toByteArray());
		}
		catch(IOException e) {
			Log.e(TAG, "Problem communicating with API", e);
			return null;
		}
	}

	public static String getRoomInformationForHotel(int hotelId, String arrivalDateParam, String departureDateParam,
			String currencyCode, int numOfAdults) {
		String urlString = HOTEL_AVAILABILITY_URL;
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS;
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		urlString += "&" + arrivalDateParam + "&" + departureDateParam + "&room1=" + numOfAdults;
		urlString += "&options=0";

		// LayoutInflater li = this.getLayoutInflater();
		// LinearLayout foot = (LinearLayout) li.inflate(R.layout.listfoot, null);
		// mHotelListView.addFooterView(foot);

		
		return executeWithTimeout(urlString);
	}

	public static String getExpediaNext(String mQueryString, String currencyCode) {
		String urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS + "&currencyCode=" + currencyCode;
		urlString += mQueryString;

		return executeWithTimeout(urlString);
	}


}
