package com.softskills.evasearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

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

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.softskills.components.S3LocationUpdater;

public class XpediaProtocol  {

	public class MessagePair {
		public MessagePair(int i, String string) {
			mMessageText = string;
			mMessageId = i;
		}
		public String mMessageText;
		public int mMessageId;

	}


	public class StringLineEndedException extends Exception {

	}



	static String TAG="XpediaProtocol"; 



	/**
	 * {@link StatusLine} HTTP status code when no server error has occurred.
	 */
	private static final int HTTP_STATUS_OK = 200;
	
	private static final int HTTP_TIMEOUT = 8000;
	
	private final static String HOTEL_LIST_URL = "http://api.ean.com/ean-services/rs/hotel/v3/list?";
	private final static String HOTEL_INFO_URL = "http://api.ean.com/ean-services/rs/hotel/v3/info?";
	private static final String CONSTANT_HTTP_PARAMS = "locale=en_US&customerUserAgent=Mozilla%2F5.0+%28Windows+NT+5.1%29+AppleWebKit%2F534.24+%28KHTML%2C+like+Gecko%29+Chrome%2F11.0.696.71+Safari%2F534.24";

	//    protected synchronized String getUrlContent(String url, List<NameValuePair> nameValuePairs){
	// Create client and set our specific user-agent string

	static String getApiKey()
	{
		return EvaSearchApplication.getApiKey();
	}
	
	static String getSecret()
	{
		return EvaSearchApplication.getSecret();
	}
	
	static Bitmap download_Image(String path) {
		
		URL url = null;
		Bitmap bmp;
		
		try {
			url = new URL(path);
		} catch (MalformedURLException e) {				
			e.printStackTrace();
			return null;
		}


		URLConnection connection=null;

		Object response = null;
		try
		{				
				connection = url.openConnection();
				connection.setUseCaches(true); //
				response = connection.getContent();
				bmp =  BitmapFactory.decodeStream((InputStream)response);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();				
			return null;
		}
		catch(OutOfMemoryError e)
		{
			Log.e(TAG,"Out of memory");
			e.printStackTrace();
			return null;
		}

		return bmp;
	}
	
	private static String getSignature() {
		String sig = "";
		try
		{
			MessageDigest md = MessageDigest.getInstance("MD5");
			long timeInSeconds = (System.currentTimeMillis() / 1000);
			String input = getApiKey() + getSecret() + timeInSeconds;
			md.update(input.getBytes());
			sig = String.format("%032x", new BigInteger(1, md.digest()));
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
			sig = "";
		}
		return sig;
	}
	
	static String getExpediaHotelInformation(int hotelId, String currencyCode)
	{
		byte[] sBuffer = new byte[512];
		String urlString = HOTEL_INFO_URL;
		urlString+="apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString+="&cid=352395&";
		urlString+= CONSTANT_HTTP_PARAMS;
		urlString+="&currencyCode=" + currencyCode + "&customerIpAddress=127.0.0.1&minorRev=1&_type=json";
		urlString+="&hotelId="+hotelId;
		urlString+="&options=0";


		DefaultHttpClient client = new DefaultHttpClient();

		HttpGet request = new HttpGet(urlString);


		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG,"Invalid response from server!");
				HttpEntity entity = response.getEntity();
				Log.e(TAG,""+EntityUtils.toString(entity));
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
			Log.e(TAG,"Problem communicating with API");
			Log.e(TAG,e.getStackTrace().toString());
			return null;
		}

	}
	
	public static String getStringSuper(JSONObject jobj, String requestedKey)
	{
		try {
			Iterator jsonIterator = jobj.keys();

			while(jsonIterator.hasNext())
			{
				String key = String.valueOf(jsonIterator.next());
				String value = jobj.getString(key);
				if(key.equals(requestedKey))
				{
					return value;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static void printJsonObject(JSONObject jobj)
	{
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

	public static String getParamFromEvatureResponse(String evatureResponse,String param)
	{
		JSONObject evatureObject;
		try {
			evatureObject = new JSONObject(evatureResponse);

			JSONObject evatureReply = evatureObject.getJSONObject("api_reply");
			JSONObject eanObject = evatureReply.getJSONObject("ean");
			Iterator jsonIterator = eanObject.keys();

			while(jsonIterator.hasNext())
			{
				String key = String.valueOf(jsonIterator.next());
				String value = eanObject.getString(key);
				if(key.equals(param))
				{
					return key+"="+value;
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	static String getExpediaAnswer(String urlString, String currencyCode)
	{
		if (urlString == null)
			return null;
		
		double longitude, latitude;
		try
		{
			S3LocationUpdater location = S3LocationUpdater.getInstance();
			longitude = location.getLongitude();
			latitude = location.getLatitude();
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
			longitude = -1;
			latitude = -1;
		}
		
		if(longitude==-1)
		{
			longitude = 10000;
			latitude = 10000;
		}
		
		byte[] sBuffer = new byte[512];
		String params="";
		
		Log.i("XPD",urlString);
		try {   		
			JSONObject evatureObject = new JSONObject(urlString);
			JSONObject evatureReply = evatureObject.getJSONObject("api_reply");
			JSONObject eanObject = evatureReply.getJSONObject("ean");
			
			if (eanObject.isNull("latitude") || eanObject.isNull("longitude"))
			{
				eanObject.put("latitude", latitude);
				eanObject.put("longitude", longitude);
			}
			
			Iterator jsonIterator = eanObject.keys();
			
			while(jsonIterator.hasNext())
			{
				String key = String.valueOf(jsonIterator.next());
				String value = eanObject.getString(key);
				params+="&"+key+"="+value;
			}

			params+="&maxRatePlanCount=2";
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		DefaultHttpClient client = new DefaultHttpClient();


		//"http://api.ean.com/ean-services/rs/hotel/v3/list?cid=55505&minorRev=5&apiKey=af2e5g53qcte7u8n3rvw7vtg&_type=json&locale=en_EN&customerIpAddress=172.16.82.13&customerUserAgent=Mozilla/5.0+(Windows+NT+6.1;+WOW64)+AppleWebKit/534.24+(KHTML,+like+Gecko)+Chrome/11.0.696.68+Safari/534.24&customerSessionId=&xml=%3CHotelListRequest%3E%3CarrivalDate%3E06/12/2011%3C/arrivalDate%3E%3CdepartureDate%3E06/14/2011%3C/departureDate%3E%3CRoomGroup%3E%3CRoom%3E%3CnumberOfAdults%3E2%3C/numberOfAdults%3E%3C/Room%3E%3C/RoomGroup%3E%3Ccity%3EBarcelona%3C/city%3E%3CcountryCode%3EES%3C/countryCode%3E%3C/HotelListRequest%3E";
		/* String urlString = url+"?";

        for(int i=0;i<nameValuePairs.size();i++)
        {
        	urlString += nameValuePairs.get(i).getName()+"="+nameValuePairs.get(i).getValue();

        	if(i!=nameValuePairs.size()-1)
        	{
        		urlString+="&";
        	}
        }*/

		urlString = "http://api.ean.com/ean-services/rs/hotel/v3/list?";
		urlString+="numberOfResults=10&"; 
		urlString+="apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString+="&cid=352395&";
		urlString+="locale=en_US&customerUserAgent=Mozilla%2F5.0+%28Windows+NT+5.1%29+AppleWebKit%2F534.24+%28KHTML%2C+like+Gecko%29+Chrome%2F11.0.696.71+Safari%2F534.24"+
					"&currencyCode=" + currencyCode +
					"&customerIpAddress=127.0.0.1&minorRev=1";
		urlString+=params;                

		HttpGet request = new HttpGet(urlString);


		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG,"Invalid response from server!");

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
			Log.e(TAG,"Problem communicating with API");
			Log.e(TAG,e.getStackTrace().toString());
			return null;
		}
	}

	private static HttpResponse executeWithTimeout(DefaultHttpClient client, HttpGet request) throws ClientProtocolException, IOException {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
		HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
		client.setParams(params);
		return client.execute(request);
	}
	
	public static String getEvatureResponse(String text) {
		String evatureUrl = "http://api2.evaws.com/ean/v1.0?input_text=";
		evatureUrl+=URLEncoder.encode(text);

		byte[] sBuffer = new byte[512];
		DefaultHttpClient client = new DefaultHttpClient();        

		HttpGet request = new HttpGet(evatureUrl);


		try {
			
			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG,"Invalid response from server!");

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
			Log.e(TAG,"Problem communicating with API");
			Log.e(TAG,e.getStackTrace().toString());
			return null;
		}
	}

	public static String getRoomInformationForHotel(int hotelId,
			String arrivalDateParam, String departureDateParam, String currencyCode, int numOfAdults) {
		byte[] sBuffer = new byte[512];
		String urlString = "http://api.ean.com/ean-services/rs/hotel/v3/avail?";
		urlString+="apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString+="&cid=352395&";
		urlString+="locale=en_US&customerUserAgent=Mozilla%2F5.0+%28Windows+NT+5.1%29+AppleWebKit%2F534.24+%28KHTML%2C+like+Gecko%29+Chrome%2F11.0.696.71+Safari%2F534.24";
		urlString+="&currencyCode=" + currencyCode + "&customerIpAddress=127.0.0.1&minorRev=1&_type=json";
		urlString+="&hotelId="+hotelId;
		urlString+="&"+arrivalDateParam+"&"+departureDateParam+"&room1=" + numOfAdults;
		urlString+="&options=0";


		DefaultHttpClient client = new DefaultHttpClient();
//		LayoutInflater li = this.getLayoutInflater();
//		LinearLayout foot = (LinearLayout) li.inflate(R.layout.listfoot, null);
//		mHotelListView.addFooterView(foot);
		HttpGet request = new HttpGet(urlString);


		try {

			HttpResponse response = client.execute(request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG,"Invalid response from server!");

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
			Log.e(TAG,"Problem communicating with API");
			Log.e(TAG,e.getStackTrace().toString());
			return null;
		}

	}

	public static String getExpediaNext(String mQueryString, String currencyCode) {
		DefaultHttpClient client = new DefaultHttpClient();
		
		byte[] sBuffer = new byte[512];
		String urlString = "";

		urlString = "http://api.ean.com/ean-services/rs/hotel/v3/list?";
		urlString+="numberOfResults=10&";
		urlString+="apiKey=" + getApiKey() + "&sig=" + getSignature();
		urlString+="&cid=352395&";
		urlString+="locale=en_US&customerUserAgent=Mozilla%2F5.0+%28Windows+NT+5.1%29+AppleWebKit%2F534.24+%28KHTML%2C+like+Gecko%29+Chrome%2F11.0.696.71+Safari%2F534.24" + 
					"&currencyCode=" + currencyCode + "&customerIpAddress=127.0.0.1&minorRev=1";
		urlString+=mQueryString;                

		HttpGet request = new HttpGet(urlString);


		try {

			HttpResponse response = executeWithTimeout(client, request);

			// Check if server response is valid
			StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HTTP_STATUS_OK) {

				Log.e(TAG,"Invalid response from server!");

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
			Log.e(TAG,"Problem communicating with API");
			Log.e(TAG,e.getStackTrace().toString());
			return null;
		}
	}

}
