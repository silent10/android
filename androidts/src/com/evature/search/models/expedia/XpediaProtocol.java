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

import com.evaapis.EvaAPIs;
import com.evaapis.EvaApiReply;
import com.evature.search.MyApplication;

public class XpediaProtocol {

	private final static String TAG = XpediaProtocol.class.getSimpleName();
	private static String minorRev = "16";

	public class MessagePair {
		public MessagePair(int i, String string) {
			mMessageText = string;
			mMessageId = i;
		}

		public String mMessageText;
		public int mMessageId;

	}

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
		byte[] sBuffer = new byte[512];
		String urlString = HOTEL_INFO_URL;
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS;
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		urlString += "&options=0";

		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet request = new HttpGet(urlString);

		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG, "Invalid response from server!");
				HttpEntity entity = response.getEntity();
				Log.e(TAG, "" + EntityUtils.toString(entity));
				return null;
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray());
		} catch (IOException e) {
			Log.e(TAG, "Problem communicating with API");
			Log.e(TAG, e.getStackTrace().toString());
			return null;
		}

	}

	public static String getExpediaAnswer(EvaApiReply apiReply, String currencyCode) {
		Log.i(TAG, "getExpediaAnswer()");
		if (apiReply == null)
			return null;

		double longitude, latitude;
				
		longitude = EvaAPIs.getLongitude();
		latitude = EvaAPIs.getLatitude();
		
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

		DefaultHttpClient client = new DefaultHttpClient();

		String urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS + "&currencyCode=" + currencyCode;
		urlString += params;

		HttpGet request = new HttpGet(urlString);

		Log.i(TAG, "EXPEDIA URL = " + urlString);

		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG, "Invalid response from server!");

				return null;
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray()); // Got an out of memory error here!
		} catch (IOException e) {
			Log.e(TAG, "Problem communicating with API");
			Log.e(TAG, e.getStackTrace().toString());
			return null;
		}
	}

	private static HttpResponse executeWithTimeout(DefaultHttpClient client, HttpGet request)
			throws ClientProtocolException, IOException {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
		client.setParams(params);
		return client.execute(request);
	}

	public static String getRoomInformationForHotel(int hotelId, String arrivalDateParam, String departureDateParam,
			String currencyCode, int numOfAdults) {
		byte[] sBuffer = new byte[512];
		String urlString = HOTEL_AVAILABILITY_URL;
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS;
		urlString += "&currencyCode=" + currencyCode + "&_type=json";
		urlString += "&hotelId=" + hotelId;
		urlString += "&" + arrivalDateParam + "&" + departureDateParam + "&room1=" + numOfAdults;
		urlString += "&options=0";

		DefaultHttpClient client = new DefaultHttpClient();
		// LayoutInflater li = this.getLayoutInflater();
		// LinearLayout foot = (LinearLayout) li.inflate(R.layout.listfoot, null);
		// mHotelListView.addFooterView(foot);
		HttpGet request = new HttpGet(urlString);

		try {

			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG, "Invalid response from server!");

				return null;
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray());
		} catch (IOException e) {
			Log.e(TAG, "Problem communicating with API");
			Log.e(TAG, e.getStackTrace().toString());
			return null;
		}

	}

	public static String getExpediaNext(String mQueryString, String currencyCode) {
		DefaultHttpClient client = new DefaultHttpClient();

		byte[] sBuffer = new byte[512];
		String urlString = "";

		urlString = HOTEL_LIST_URL;
		urlString += "numberOfResults=10&";
		urlString += "apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString += "&cid=" + getClientId() + "&";
		urlString += CONSTANT_HTTP_PARAMS + "&currencyCode=" + currencyCode;
		urlString += mQueryString;

		HttpGet request = new HttpGet(urlString);

		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG, "Invalid response from server!");

				return null;
			}

			// Pull content stream from response
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			ByteArrayOutputStream content = new ByteArrayOutputStream();

			// Read response into a buffered stream
			int readBytes = 0;
			while ((readBytes = inputStream.read(sBuffer)) != -1) {
				content.write(sBuffer, 0, readBytes);
			}

			// Return result from buffered stream
			return new String(content.toByteArray());
		} catch (IOException e) {
			Log.e(TAG, "Problem communicating with API");
			Log.e(TAG, e.getStackTrace().toString());
			return null;
		}
	}

	public static void printJsonObject(JSONObject jobj) {
		try {
			Iterator jsonIterator = jobj.keys();

			while(jsonIterator.hasNext())
			{
				String key = String.valueOf(jsonIterator.next());
				String value = jobj.getString(key);
				Log.i("TAG",key+"="+value);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
