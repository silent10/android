/*
 	Wraps a voice streamer in a http post request to Eva server
 */
package com.evaapis.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.evature.util.DLog;

@SuppressLint("DefaultLocale")
public class EvaVoiceClient {

	private static final String TAG = "EvaVoiceClient";

	private String LANGUAGE = "ENUS";
	private String CODEC = "audio/x-flac;rate=16000";//"audio/x-speex;rate=16000";	//MP3
	private String RESULTS_FORMAT = "text/plain";


	private static short PORT = (short) 443;

	private String mEvaResponse;

	private final SpeechAudioStreamer mSpeechAudioStreamer;

	private boolean mInTransaction = false;
//	HttpPost mHttpPost = null;
	
	private final EvaComponent.EvaConfig mConfig;

	// debug time measurements
	public long timeSpentReadingResponse;
	public long timeSpentUploading;
	public long timeWaitingForServer;

	OutputStream mUploadStream;
	
	boolean hadError;
	private final boolean editLastUtterance;

	private Context mContext;

	private HttpURLConnection mConnection;



	/****
	 * 
	 * @param context - android context
	 * @param config 
	 * @param speechAudioStreamer
	 */
	public EvaVoiceClient(Context context, final EvaComponent.EvaConfig config,
			final SpeechAudioStreamer speechAudioStreamer, final boolean editLastUtterance) {
		mConfig = config;
		mContext = context;
		mSpeechAudioStreamer = speechAudioStreamer;	
		this.editLastUtterance = editLastUtterance; 
	}

//	private static HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException
//	{
//		HttpParams params = new BasicHttpParams();
//		HttpProtocolParams.setContentCharset(params, "UTF-8");
//		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
//		HttpProtocolParams.setUseExpectContinue(params, false);
//		HttpConnectionParams.setConnectionTimeout(params, 10000); // wait 10 seconds to establish connection
//		HttpConnectionParams.setSoTimeout(params, 120000); // wait 120 seconds to get first byte in response
//
//		// Initialize the HTTP client
//		HttpClient httpclient = new DefaultHttpClient(params);
//
//		SSLSocketFactory sf=null;
//		try {
//			sf = new EvatureSSLSocketFactory(null);
//		} catch (UnrecoverableKeyException e) {
//			Log.e(TAG, "UnrecoverableKeyException", e);
//		} catch (KeyStoreException e) {
//			Log.e(TAG, "KeyStoreException", e);
//		}
//		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
//		Scheme sch = new Scheme("https", sf, PORT);
//		httpclient.getConnectionManager().getSchemeRegistry().register(sch);
//
//		return httpclient;
//	}


	private URL getURL() throws Exception
	{
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();

		qparams.add(new BasicNameValuePair("site_code", mConfig.siteCode));
		qparams.add(new BasicNameValuePair("api_key", mConfig.appKey));
		qparams.add(new BasicNameValuePair("sdk_version", EvaComponent.SDK_VERSION));
		if (mConfig.appVersion != null) {
			qparams.add(new BasicNameValuePair("app_version", mConfig.appVersion));
		}
		qparams.add(new BasicNameValuePair("uid",  mConfig.deviceId));
		qparams.add(new BasicNameValuePair("ffi_chains", "true"));
		qparams.add(new BasicNameValuePair("ffi_statement", "true"));
		qparams.add(new BasicNameValuePair("session_id", mConfig.sessionId));
		if (mConfig.context != null) {
			qparams.add(new BasicNameValuePair("context", mConfig.context));
		}
		if (mConfig.scope != null) {
			qparams.add(new BasicNameValuePair("scope", mConfig.scope));
		}
		try {
			double longitude = EvatureLocationUpdater.getLongitude();
			double latitude = EvatureLocationUpdater.getLatitude();
			if (latitude != EvatureLocationUpdater.NO_LOCATION) {
				qparams.add(new BasicNameValuePair("longitude",""+longitude));
				qparams.add(new BasicNameValuePair("latitude",""+latitude));
			}
		} catch (Exception e1) {
			DLog.e(TAG, "Exception setting location", e1);
		}
		
		if (mConfig.vrService != null && !"none".equals(mConfig.vrService)) {
			qparams.add(new BasicNameValuePair("vr_service", mConfig.vrService));
		}
		
		if (mConfig.language != null) {
			qparams.add(new BasicNameValuePair("language", mConfig.language.replaceAll("-.*$", "")));
		}
		
		if (mConfig.locale != null) {
			qparams.add(new BasicNameValuePair("locale", mConfig.locale));
		}
		else {
			Locale currentLocale = Locale.getDefault();
			qparams.add(new BasicNameValuePair("locale", currentLocale.getCountry())); 
		}
		
		qparams.add(new BasicNameValuePair("time_zone", (""+TimeZone.getDefault().getRawOffset()/3600000.0).replaceFirst("\\.0+$",  "")));
		qparams.add(new BasicNameValuePair("android_ver", String.valueOf(android.os.Build.VERSION.RELEASE)));
		qparams.add(new BasicNameValuePair("device", android.os.Build.MODEL));
		
		if (editLastUtterance) {
			qparams.add(new BasicNameValuePair("edit_last_utterance", "true"));
		}
		
		for (String key : mConfig.extraParams.keySet()) {
			String val = mConfig.extraParams.get(key);
			if (val != null)
				qparams.add(new BasicNameValuePair(key, val));
		}
		
		String host = mConfig.vproxyHost.toLowerCase();
		if (host.startsWith("http") == false) {
			host = "https://"+host;
		}
		int port = -1;
		String protocol = "https";
		try {
			URL aURL = new URL(host);
			port = aURL.getPort();
			host = aURL.getHost();
			protocol = aURL.getProtocol();
		}catch (MalformedURLException e) {
		}
		if (port == -1) {
			port = PORT;
		}

		URI uri = URIUtils.createURI(protocol, host, port, mConfig.apiVersion, URLEncodedUtils.format(qparams, "UTF-8"), null);
		return uri.toURL();
	}

	/*
	 * This is a simpler helper function to setup the Header parameters
	 */
	private HttpURLConnection getConnection(URL url) throws IOException 
	{
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		try {
			conn.setRequestMethod("POST");
		} catch (ProtocolException e) {
			e.printStackTrace();
		}
		conn.setRequestProperty("Content-Type", CODEC);
		conn.setRequestProperty("Content-Language", LANGUAGE);
		conn.setRequestProperty("Accept-Language", LANGUAGE);
		conn.setRequestProperty("Accept", RESULTS_FORMAT);
//		conn.setRequestProperty("Accept-Encoding","gzip");
//		conn.setRequestProperty("Transfer-Encoding","chunked");

		conn.setReadTimeout(10000);
		conn.setConnectTimeout(10000);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setChunkedStreamingMode(512);
		conn.setUseCaches(false);
		return conn;
	}

	/***
	 * Start the recording and return it as 
	 * @param speechAudioStreamer
	 * @return
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	private void setAudioContent(HttpURLConnection connection, SpeechAudioStreamer speechAudioStreamer) throws Exception
	{
		String filepath = null;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean saveEncoded = prefs.getBoolean("eva_save_encoded", false);
		OutputStream uploadStream = connection.getOutputStream();
		if (saveEncoded) {
			filepath = Environment.getExternalStorageDirectory().getPath() + "/recording.flac";
			mUploadStream = new DebugOutputStream(uploadStream, saveEncoded, filepath);
		}
		else {
			mUploadStream = uploadStream;
		}
		
		boolean success = speechAudioStreamer.start(mUploadStream);
		if (!success) {
			throw new Exception("Failed getting audio content");
		}
	}


	private StringBuilder inputStreamToString(InputStream is) throws UnsupportedEncodingException {
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));

		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) { 
				total.append(line); 
			}
		} catch (IOException e) {
			DLog.e(TAG, "IOError reading inputStream", e);
		}

		// Return full string
		return total;
	}

	private void processResponse(HttpURLConnection connection) throws IllegalStateException, IOException
	{
		DLog.i(TAG,"<<< Getting response");
		long t0 = System.nanoTime();

//		InputStream is = resEntity.getContent();
//		Header contentEncoding = response.getFirstHeader("Content-Encoding");
//		if (contentEncoding != null && contentEncoding.getValue().equalsIgnoreCase("gzip")) {
//			Log.d(TAG, "gzip encoded vproxy result");
//			is = new GZIPInputStream(is);
//		}
//
		DLog.i(TAG, "status: "+connection.getResponseCode()+"  "+connection.getResponseMessage());
		for ( Map.Entry<String,List<String>> header : connection.getHeaderFields().entrySet()) {
		    System.out.println(header.getKey() + "=" + header.getValue());
		}

		if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) { 
			mEvaResponse = inputStreamToString(connection.getInputStream()).toString();
		}
		else {
			mEvaResponse = null;
			DLog.w(TAG, "Error from server: "+inputStreamToString(connection.getErrorStream()).toString());
		}

		
//		resEntity.consumeContent();
		
		timeSpentReadingResponse = (System.nanoTime() - t0) / 1000000;
		DLog.i(TAG, "<<< Got Response");//: \n"+mEvaResponse);

	}
	
	public String getEvaResponse()
	{
		return mEvaResponse;
	}

	public void startVoiceRequest() throws Exception{
		mInTransaction = true;
		hadError = false;
		try {
			URL url = getURL();
			DLog.i(TAG,"<<< Sending post request to URL: "+url);

			mConnection = getConnection(url);
			
			long t0 = System.nanoTime();
			setAudioContent(mConnection, mSpeechAudioStreamer);
			Thread.sleep(10);
			// wait until write is complete
			while (mInTransaction && mSpeechAudioStreamer.getIsRecording()) {
				Thread.sleep(10);
			}
			long t1 = System.nanoTime();
			timeSpentUploading = (t1 - t0) / 1000000;
			if (mInTransaction) {
				t0 = System.nanoTime();
				//timeWaitingForServer = (t0 - mUploadStream.timeOfLastBuffer) / 1000000;
				processResponse(mConnection);
			}
		}
		catch (IOException e) {
			if ("Connection already shutdown".equals(e.getMessage())) {
				DLog.i(TAG, "Connection already shutdown");
			}
			if ("Request aborted".equals(e.getMessage())) {
				DLog.i(TAG, "Request aborted");
			}
			else {
				DLog.e(TAG, "Exception sending voice request", e);
				hadError = true;
			}
		}
		catch(Exception e)	{
			DLog.e(TAG, "Exception sending voice request", e);
			hadError = true;
		}
		finally {
			if (mConnection != null) {
				mConnection.disconnect();
				mConnection = null;
			}
			mInTransaction = false;
		}
	}
	/*
	public static void sendAudioFile(Context context, EvaComponent.EvaConfig config, Uri fileuri, String filekey) {
		HttpClient httpclient = null;
		try {
			httpclient = getHttpClient();

			String host = config.vproxyHost.toLowerCase();
			if (host.startsWith("http") == false) {
				host = "https://"+host;
			}
			
			String url = host+"/save_to_s3/"+filekey;
			Log.i(TAG,"<<< Sending audio file to URI: "+url);

		    HttpPost httppost = new HttpPost(url);
		    ContentResolver contentResolver = context.getContentResolver();
		    InputStreamEntity reqEntity =  new InputStreamEntity( contentResolver.openInputStream(fileuri), -1); //new FileInputStream(file), -1);
		    reqEntity.setContentType("audio/amr");
//		    reqEntity.setChunked(true);
		    httppost.setEntity(reqEntity);
			long t0 = System.nanoTime();
			HttpResponse response = httpclient.execute(httppost);
			long t1 = System.nanoTime();
			Log.d(TAG, "Response: code="+ response.getStatusLine().getStatusCode()+ ";   Time spent uploading audio file = "+ ((t1 - t0) / 1000000));
			if( httpclient != null ) {
				httpclient.getConnectionManager().shutdown();
				httpclient = null;
			}
		}
		catch (IOException e) {
			if (e.getMessage().equals("Connection already shutdown")) {
				Log.i(TAG, "Connection already shutdown");
			}
			else {
				Log.e(TAG, "Exception sending audio file", e);
			}
		}
		catch(Exception e)	{
			Log.e(TAG, "Exception sending audio file", e);
		}
		finally {
			// When HttpClient instance is no longer needed, 
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			if( httpclient != null ) 
				httpclient.getConnectionManager().shutdown();
		}
	}
	*/

	public void stopTransfer()
	{
		
		if(getInTransaction())
		{
			if( mConnection != null ) {
				mConnection.disconnect();
				mConnection = null;
			}

			mInTransaction=false;
		}
	}

	public boolean getInTransaction()
	{
		return mInTransaction;
	}


}
