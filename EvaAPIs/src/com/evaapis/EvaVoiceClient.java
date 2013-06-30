/*
 	Wraps a voice streamer in a http post request to Eva server
 */
package com.evaapis;

import java.io.BufferedReader;
import java.io.File;
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
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.evature.util.EvatureSSLSocketFactory;

public class EvaVoiceClient {

	private static final String TAG = "EvaVoiceClient";

	private String mSiteCode = "UNKNOWN";
	private String mAppKey = "UNKNOWN";
	private String mDeviceId = "0000";
	private String LANGUAGE = "ENUS";
	private String CODEC = "audio/x-wav"; 
	private String RESULTS_FORMAT = "text/plain";

	private static short PORT = (short) 443;
	private static String HOSTNAME = "vproxy.evaws.com";

	private String mEvaJson;
	private String mSessionId;


	private boolean mInTransaction = false;
	HttpPost mHttpPost = null;

	public EvaVoiceClient(String siteCode, String appKey, String deviceId,
			String sessionId) {
		mSiteCode = siteCode;
		mAppKey = appKey;
		mDeviceId = deviceId;
		mSessionId = sessionId;
	}

	@SuppressWarnings("deprecation")
	private HttpClient getHttpClient() throws NoSuchAlgorithmException,
			KeyManagementException {
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setContentCharset(params, "UTF-8");
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setUseExpectContinue(params, false);

		// Initialize the HTTP client
		HttpClient httpclient = new DefaultHttpClient(params);

		SSLSocketFactory sf = null;
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

	private URI getURI() throws Exception {
		List<NameValuePair> qparams = new ArrayList<NameValuePair>();

		qparams.add(new BasicNameValuePair("site_code", mSiteCode));
		qparams.add(new BasicNameValuePair("api_key", mAppKey));
		qparams.add(new BasicNameValuePair("id", mDeviceId));
		qparams.add(new BasicNameValuePair("session_id", mSessionId));

		try {
			double longitude = EvatureLocationUpdater.getLongitude();
			double latitude = EvatureLocationUpdater.getLatitude();
			if (latitude != -1 && longitude != -1) {
				qparams.add(new BasicNameValuePair("longitude", "" + longitude));
				qparams.add(new BasicNameValuePair("latitude", "" + latitude));
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		URI uri = URIUtils.createURI("https", HOSTNAME, PORT, "",
				URLEncodedUtils.format(qparams, "UTF-8"), null);

		return uri;
	}

	/*
	 * This is a simpler helper function to setup the Header parameters
	 */
	private HttpPost getHeader(URI uri, long contentLength)
			throws UnsupportedEncodingException {
		HttpPost httppost = new HttpPost(uri);

		// httppost.setHeader("Transfer-Encoding", "chunked");
		// httppost.addHeader("Transfer-Encoding", "chunked");

		httppost.addHeader("Content-Type", CODEC);
		// httppost.addHeader("Content-Language", LANGUAGE);
		// httppost.addHeader("Accept-Language", LANGUAGE);
		httppost.addHeader("Accept", RESULTS_FORMAT);

		return httppost;
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

	private void processResponse(HttpResponse response)
			throws IllegalStateException, IOException {
		HttpEntity resEntity = response.getEntity();

		Log.i(TAG, "Response status: " + response.getStatusLine());
		Header[] h = response.getAllHeaders();

		for (int i = 0; i < h.length; i++) {
			Log.i(TAG, "Header:" + h[i].getName() + "=" + h[i].getValue());
		}

		InputStream is = resEntity.getContent();

		mEvaJson = inputStreamToString(is).toString();

		Log.i(TAG, "Response: \n" + mEvaJson);

		resEntity.consumeContent();
	}

	public String getEvaJson() {
		return mEvaJson;
	}

	
	
	public void sendFile(File outfile) {
		HttpClient httpclient = null;
		try {
			httpclient = getHttpClient();

			URI uri = getURI();
			Log.i(TAG, "Sending post request to URI: " + uri);

			FileEntity reqEntity = new FileEntity(outfile, "audio/wav");

			mHttpPost = getHeader(uri, outfile.length());//0); // fileSize);
			mHttpPost.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(mHttpPost);

			Log.i(TAG, "After Sending post request");
			processResponse(response);

			if (httpclient != null) {
				httpclient.getConnectionManager().shutdown();
				httpclient = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "Exception sending voice request", e);
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			if (httpclient != null)
				httpclient.getConnectionManager().shutdown();
			mInTransaction = false;
		}
	}

	public void stopTransfer() {
		if (getInTransaction()) {
			mHttpPost.abort();
			mInTransaction = false;

		}
	}
	
	public boolean getInTransaction() {
		return mInTransaction;
	}

}
