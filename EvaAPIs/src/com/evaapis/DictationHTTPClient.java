/*
 * DictationHTTPClient.java
 *
 * This is a simple command-line java app that shows how to use the NMDP HTTP Client Interface for
 *	Dictation and WebSearch requests using the POST method
 *
 * This basic java app will:
 *	1. Create an instance of an HttpClient to interact with our HTTP Client Interface for TTS
 *	2. Use some simple helper methods to setup the URI and HTTP POST parameters
 *	3. Execute the HTTP Request, passing streamed audio from file to the interface
 *	4. Process the HTTP Response, writing the results to the console
 *
 *	Output of progress of the request is logged to console
 *	Values to be passed to the HTTP Client Interface are simply hard-coded class members for demo purposes
 *
 * @copyright  Copyright (c) 2010 Nuance Communications, inc. (http://www.nuance.com)
 *
 * @Created	: June 6, 2011
 * @Author	: Peter Freshman
 */
package com.evaapis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.evature.search.utils.EvatureSSLSocketFactory;

public class DictationHTTPClient {

	//TODO: Remove all nuance references. Change this code from static to instance code
	
	
	/*
	 **********************************************************************************************************
	 * Client Interface Parameters:
	 *
	 * appId: 		You received this by email when you registered
	 * appKey:	 	You received this as a 64-byte Hex array when you registered.
	 * 				If you provide us with your username, we can convert this to a 128-byte string for you.
	 * id: 			Device Id is any character string. Typically a mobile device Id, but for test purposes, use the default value
	 * Language:	The language code to use.
	 *
	 *				Please refer to the FAQ document available at the Nuance Mobile Developer website for a detailed list
	 *				of available languages (http://dragonmobile.nuancemobiledeveloper.com/faq.php)
	 *
	 * codec:		The desired audio format. The supported codecs are:
	 *
	 *					audio/x-wav;codec=pcm;bit=16;rate=8000
	 *					audio/x-wav;codec=pcm;bit=16;rate=11025
	 * 					audio/x-wav;codec=pcm;bit=16;rate=16000
	 *					audio/x-wav
	 *					speex_nb', 'audio/x-speex;rate=8000
	 *					speex_wb', 'audio/x-speex;rate=16000
	 *					audio/amr
	 *					audio/qcelp
	 *					audio/evrc
	 *
	 * Language Model:	The language model to be used for speech-to-text conversion. Supported values are
	 * 					Dictation and WebSearch
	 * 
	 * Results Format: The format the results she be returned as. Supported values are text/plan and application/xml.
	 * 					Currently, application/xml is ignored and will return results as text/plain. However, the next
	 * 					release of Network Speech Services will support results returned in xml format.
	 *
	 *********************************************************************************************************
	 */
	private static String mSiteCode = "thack";
	private static String mAppKey = "thack-london-june-2012";
    static String deviceID = "0000";
	private String LANGUAGE = "ENUS";
	private String CODEC = "audio/x-speex;rate=16000";	//MP3
	private String LM = "Dictation";	// or WebSearch
	private String RESULTS_FORMAT = "text/plain";	// or application/xml



	/*********************************************************************************************************
	 *
	 * HTTP Client Interface URI parameters
	 *
	 * PORT:		To access this interface, port 443 is required
	 * HOSTNAME:	DNS address is dictation.nuancemobility.net
	 * SERVLET:		Dictation Servlet Resource
	 *
	 *********************************************************************************************************
	 */
	private static short PORT = (short) 443;
	private static String HOSTNAME = "vproxy.evaws.com";
	private static String SERVLET = "/NMDPAsrCmdServlet/dictation";

	private static String ADD_CONTEXT = "/NMDPAsrCmdServlet/addContext";

	private static final String SAMPLE_RATE_8K  = "8K";
	private static final String SAMPLE_RATE_11K = "11K";
	private static final String SAMPLE_RATE_16K = "16K";

	private static String cookie = null;

	private static boolean mInTransaction = false;
	
	/*
	 * HttpClient member to handle the Dictation request/response
	 */



	private String sampleRate = "16000";
	private boolean isStreamed = false;


	/*
	 * This function will initialize httpclient, set some basic HTTP parameters (version, UTF),
	 *	and setup SSL settings for communication between the httpclient and our Nuance servers
	 */
	@SuppressWarnings("deprecation")
	private HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException
	{
		// Standard HTTP parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setUseExpectContinue(params, false);

		// Initialize the HTTP client
		httpclient = new DefaultHttpClient(params);

		// Initialize/setup SSL
		TrustManager easyTrustManager = new X509TrustManager() {
			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] arg0, String arg1)
							throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				// TODO Auto-generated method stub
				return null;
			}
		};

		SSLContext sslcontext = SSLContext.getInstance("TLS");
		sslcontext.init(null, new TrustManager[] { easyTrustManager }, null);
		SSLSocketFactory sf=null;
		try {
			sf = new EvatureSSLSocketFactory(sslcontext);
		} catch (UnrecoverableKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", sf, PORT);	// PORT = 443
		httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		// Return the initialized instance of our httpclient
		return httpclient;
	}

	/*
	 * This is a simple helper function to setup the query parameters. We need the following:
	 *	1. appId
	 *	2. appKey
	 *	3. id
	 *
	 *	If your query fails, please be sure to review carefully what you are passing in for these
	 *	name/value pairs. Misspelled names, and invalid AppKey values are a VERY common mistake.
	 */
	private List<NameValuePair> setParams()
	{
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();

		qparams.add(new BasicNameValuePair("site_code", mSiteCode));
		qparams.add(new BasicNameValuePair("api_key", mAppKey));
		qparams.add(new BasicNameValuePair("id",  deviceID));

		EvatureLocationUpdater location;
		try {
			location = EvatureLocationUpdater.getInstance();
			double longitude = location.getLongitude();
			double latitude = location.getLatitude();
			if (latitude != 0 && longitude != 0) {
				qparams.add(new BasicNameValuePair("longitude",""+longitude));
				qparams.add(new BasicNameValuePair("latitude",""+latitude));
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}



		return qparams;
	}

	/*
	 * This is a simple helper function to create the URI.
	 */
	private URI getURI() throws Exception
	{
		// Get the standard set of parameters to be passed in...
		List<NameValuePair> qparams = this.setParams();

		URI uri = URIUtils.createURI("https", HOSTNAME, PORT, "", URLEncodedUtils.format(qparams, "UTF-8"), null);

		return uri;
	}

	/*
	 * This is a simpler helper function to setup the Header parameters
	 */
	private HttpPost getHeader(URI uri, long contentLength) throws UnsupportedEncodingException
	{
		HttpPost httppost = new HttpPost(uri);


		//		httppost.setHeader("Transfer-Encoding", "chunked");	
		//		httppost.addHeader("Transfer-Encoding", "chunked");

		httppost.addHeader("Content-Type",  CODEC);
		httppost.addHeader("Content-Language", LANGUAGE);
		httppost.addHeader("Accept-Language", LANGUAGE);
		httppost.addHeader("Accept", RESULTS_FORMAT);
		httppost.addHeader("Accept-Topic", LM);	

		return httppost;
	}

	SpeechAudioStreamer mFs;
	static private String mEvaJson;

	private InputStreamEntity setAudioContent(SpeechAudioStreamer speechAudioStreamer) throws NumberFormatException, Exception
	{		
		mFs = speechAudioStreamer;
		InputStreamEntity reqEntity  = new InputStreamEntity(mFs.getInputStream(), -1);
		mFs.start();

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Return full string
		return total;
	}

	private void processResponse(HttpResponse response) throws IllegalStateException, IOException
	{
		HttpEntity resEntity = response.getEntity();


		System.out.println(response.getStatusLine());
		Header [] h = response.getAllHeaders();

		for(int i=0;i<h.length;i++)
		{
			Log.i("EVA","Header:"+h[i].getName()+"="+h[i].getValue());
		}

		InputStream is = resEntity.getContent();

		mEvaJson = inputStreamToString(is).toString();

		Log.i("EVA",mEvaJson);
		
		resEntity.consumeContent();
	}
	
	static String getEvaJson()
	{
		return mEvaJson;
	}

	static DictationHTTPClient mDictationHttpClient = null;
	static HttpClient httpclient;

	static SpeechAudioStreamer mSpeechAudioStreamer;
	public static int mSoundLevel;
	/**
	 * @param speechAudioStreamer 
	 * @param args
	 * @throws Exception 
	 */
	public static void startDictation(SpeechAudioStreamer speechAudioStreamer,String appKey, String siteCode,String deviceID) throws Exception{

		deviceID=deviceID;
		
		
		mInTransaction = true;
		
		mSpeechAudioStreamer = speechAudioStreamer;	

		mDictationHttpClient = new DictationHTTPClient();

		try {
			httpclient = mDictationHttpClient.getHttpClient();
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}	

		// Add a debug statement here showing all values being used for this request
		System.out.println();
		mDictationHttpClient.printSettings();
		System.out.println();


		Log.i("EVA","Sending post request");

		try
		{
			URI uri;
			InputStreamEntity reqEntity = mDictationHttpClient.setAudioContent(mSpeechAudioStreamer);
			uri = mDictationHttpClient.getURI();

			HttpPost httppost = mDictationHttpClient.getHeader(uri, 0);	//fileSize);
			httppost.setEntity(reqEntity);		
			HttpResponse response = httpclient.execute(httppost);

			Log.i("EVA","After Sending post request");
			System.out.println();
			System.out.println("----------------- Processing Response ----------------------");
			mDictationHttpClient.processResponse(response);
			// When HttpClient instance is no longer needed, 
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			if( mDictationHttpClient != null && mDictationHttpClient.httpclient != null )
				mDictationHttpClient.httpclient.getConnectionManager().shutdown();
		}
		catch(Exception e)
		{

		}

		finally {
			// When HttpClient instance is no longer needed, 
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			if( mDictationHttpClient != null && mDictationHttpClient.httpclient != null )
				mDictationHttpClient.httpclient.getConnectionManager().shutdown();
			mInTransaction = false;
		}

	}

	static boolean getInTransaction()
	{
		return mInTransaction;
	}
	
	/**
	 * Prints the usage
	 */
	private static void printUsage(){
		/* 
		 * -a app_id 
		 * -k app_key 
		 * -d device_id 
		 * -l lang 
		 * -f audio_format 
		 * -m language model 
		 * -o result_format 
		 * -w audio_file 
		 * -s isStreamed 
		 * -r sample_rate 
		 */
		System.err.println("Usage: java -jar DictationHTTPClient.jar \n" +
				"Optional inputs:\n" +
				"\t-a app id\n" +
				"\t-k app key (128-byte string)\n" +
				"\t-d device id\n" +
				"\t-l lang (Default is en_us)\n" +
				"\t-f audio format (Default is audio/x-wav;codec=pcm;bit=16;rate=16000)\n" +
				"\t-m language model (Default is Dictation. Options are Dictation and WebSearch)\n" +
				"\t-o results format (Default is text/plain. Options are text/plain and application/xml)\n" +
				"\t-w audio file\n" +
				"\t-s isStreamed (Default is false. Options are true and false)\n" +
				"\t-r sample rate (Default is 16000. Options are 8000 and 16000\n");
	}
	private void printSettings()
	{
		System.out.println("----------------- Application Settings ----------------------");

		System.out.println("App Id: " + this.mSiteCode);
		System.out.println("App Key: " + this.mAppKey);
		System.out.println("Device Id: " + this.deviceID);
		System.out.println("Language: " + this.LANGUAGE);
		System.out.println("Language Model: " + this.LM);
		System.out.println("Audio Format: " + this.CODEC);
		System.out.println("Sample Rate: " + this.sampleRate);
		System.out.println("Is Streamed: " + Boolean.toString(this.isStreamed));
		System.out.println("Results Format: " + this.RESULTS_FORMAT);
		System.out.println("Host: " + DictationHTTPClient.HOSTNAME);
		System.out.println("Port: " + DictationHTTPClient.PORT);
		System.out.println("Servlet: " + DictationHTTPClient.SERVLET);
		System.out.println("-------------------------------------------------------------");
	}

	public static int getSoundLevel() {
		return mSoundLevel;
	}
}
