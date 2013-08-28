/*
 	Wraps a voice streamer in a http post request to Eva server
 */
package com.evaapis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.util.Log;

import com.evature.util.EvatureSSLSocketFactory;

public class EvaVoiceClient {

	private static final String TAG = "EvaVoiceClient";

	private static final boolean LOCAL_DEBUG = false;
	
	private String mSiteCode = "UNKNOWN";
	private String mAppKey = "UNKNOWN";
    private String mDeviceId = "0000";
	private String LANGUAGE = "ENUS";
	private String CODEC = "audio/x-flac;rate=16000";//"audio/x-speex;rate=16000";	//MP3
	private String RESULTS_FORMAT = "text/plain";


	private static short PORT = (short) 443;
	private static String HOSTNAME = "vproxy.evaws.com";

	private String mEvaResponse;
	private String mSessionId;

	private SpeechAudioStreamer mSpeechAudioStreamer;

	private boolean mInTransaction = false;
	HttpPost mHttpPost = null;

	// debug time measurements
	public long timeSpentReadingResponse;
	public long timeSpentExecute;
	public long timeWaitingForServer;

	DebugStream uploadStream;
	
	boolean hadError;

	private String mLocale;
	private String mLanguage;


	public EvaVoiceClient(String siteCode, String appKey, String deviceId, String sessionId, String locale, String language, SpeechAudioStreamer speechAudioStreamer) {
		mSiteCode = siteCode;
		mAppKey = appKey;
		mDeviceId = deviceId;
		mSessionId = sessionId;
		mLocale = locale;
		mLanguage = language;
		mSpeechAudioStreamer = speechAudioStreamer;	
	}

	@SuppressWarnings("deprecation")
	private HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException
	{
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, false);
		HttpConnectionParams.setConnectionTimeout(params, 10000); // wait 10 seconds to establish connection
		HttpConnectionParams.setSoTimeout(params, 120000); // wait 120 seconds to get first byte in response

		// Initialize the HTTP client
		HttpClient httpclient = new DefaultHttpClient(params);

		SSLSocketFactory sf=null;
		try {
			sf = new EvatureSSLSocketFactory(null);
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		}
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", sf, PORT);
		httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		return httpclient;
	}


	private URI getURI() throws Exception
	{
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();

		if (LOCAL_DEBUG) {
//			qparams.add(new BasicNameValuePair("site_code", "concur_m"));
//			qparams.add(new BasicNameValuePair("api_key", "0585a2f5-9d6c-41a4-981a-842fc791b5dc"));
			qparams.add(new BasicNameValuePair("site_code", mSiteCode));
			qparams.add(new BasicNameValuePair("api_key", mAppKey));
			qparams.add(new BasicNameValuePair("vr_service", "ginger_streaming"));
		}
		else {
			qparams.add(new BasicNameValuePair("site_code", mSiteCode));
			qparams.add(new BasicNameValuePair("api_key", mAppKey));
		}
		qparams.add(new BasicNameValuePair("uid",  mDeviceId));
		qparams.add(new BasicNameValuePair("session_id", mSessionId));
		try {
			double longitude = EvatureLocationUpdater.getLongitude();
			double latitude = EvatureLocationUpdater.getLatitude();
			if (latitude != -1 && longitude != -1) {
				qparams.add(new BasicNameValuePair("longitude",""+longitude));
				qparams.add(new BasicNameValuePair("latitude",""+latitude));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		if (mLanguage != null) {
			qparams.add(new BasicNameValuePair("language", mLanguage));
		}
		
		if (mLocale != null) {
			qparams.add(new BasicNameValuePair("locale", mLocale));
		}

		URI uri = URIUtils.createURI("https", HOSTNAME, PORT, "", URLEncodedUtils.format(qparams, "UTF-8"), null);

		return uri;
	}

	/*
	 * This is a simpler helper function to setup the Header parameters
	 */
	private HttpPost getHeader(URI uri, long contentLength) throws UnsupportedEncodingException
	{
		HttpPost httppost = new HttpPost(uri);


		//	httppost.setHeader("Transfer-Encoding", "chunked");	
		//	httppost.addHeader("Transfer-Encoding", "chunked");

		httppost.addHeader("Content-Type",  CODEC);
		httppost.addHeader("Content-Language", LANGUAGE);
		httppost.addHeader("Accept-Language", LANGUAGE);
		httppost.addHeader("Accept", RESULTS_FORMAT);

		return httppost;
	}

	/***
	 * Start the recording and return it as 
	 * @param speechAudioStreamer
	 * @return
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	private InputStreamEntity setAudioContent(SpeechAudioStreamer speechAudioStreamer) throws NumberFormatException, Exception
	{
		InputStream encodedStream = speechAudioStreamer.start();
		uploadStream = new DebugStream(encodedStream);
 		InputStreamEntity reqEntity  = new InputStreamEntity(uploadStream, -1);

		reqEntity.setContentType(CODEC);

		reqEntity.setChunked(true);

		return reqEntity;
	}


	private StringBuilder inputStreamToString(InputStream is) {
		String line = "";
		StringBuilder total = new StringBuilder();

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		try {
			while ((line = rd.readLine()) != null) { 
				total.append(line); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Return full string
		return total;
	}

	private void processResponse(HttpResponse response) throws IllegalStateException, IOException
	{
		Log.i(TAG,"<<< Getting response");
		long t0 = System.nanoTime();

		HttpEntity resEntity = response.getEntity();


		System.out.println(response.getStatusLine());
		Header [] h = response.getAllHeaders();

		for(int i=0;i<h.length;i++)
		{
			Log.i(TAG,"Header:"+h[i].getName()+"="+h[i].getValue());
		}

		InputStream is = resEntity.getContent();

		mEvaResponse = inputStreamToString(is).toString();

		//Log.i(TAG, "Response: \n"+mEvaResponse);
		
		resEntity.consumeContent();
		
		timeSpentReadingResponse = (System.nanoTime() - t0) / 1000000;
		Log.i(TAG,"<<< Got response");

	}
	
	public String getEvaResponse()
	{
		return mEvaResponse;
	}

	public void startVoiceRequest() throws Exception{
		mInTransaction = true;
		hadError = false;
		HttpClient httpclient = null;
		try {
			httpclient = getHttpClient();

			URI uri = getURI();
			Log.i(TAG,"<<< Sending post request to URI: "+uri);

			InputStreamEntity reqEntity = setAudioContent(mSpeechAudioStreamer);
			

			mHttpPost = getHeader(uri, 0);	//fileSize);
			mHttpPost.setEntity(reqEntity);		
			
			long t0 = System.nanoTime();
			HttpResponse response = httpclient.execute(mHttpPost);
			long t1 = System.nanoTime();
			timeWaitingForServer = (t1 - uploadStream.timeOfLastBuffer) / 1000000;
			timeSpentExecute = (t1 - t0) / 1000000;

			processResponse(response);

			if( httpclient != null ) {
				httpclient.getConnectionManager().shutdown();
				httpclient = null;
			}
		}
		catch(Exception e)	{
			Log.e(TAG, "Exception sending voice request", e);
			hadError = true;
		}
		finally {
			// When HttpClient instance is no longer needed, 
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			if( httpclient != null ) 
				httpclient.getConnectionManager().shutdown();
			mInTransaction = false;
		}

	}
	
	public void stopTransfer()
	{
		if(getInTransaction())
		{
			if (mHttpPost != null) {
				mHttpPost.abort();
			}
			mInTransaction=false;
		}
	}

	public boolean getInTransaction()
	{
		return mInTransaction;
	}


}
