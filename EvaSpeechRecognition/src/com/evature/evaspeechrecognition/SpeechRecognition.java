package com.evature.evaspeechrecognition;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;


public class SpeechRecognition {
	private static final String TAG = "SpeechRecognition";

	private static short PORT = (short) 443;


	public static class EvatureSSLSocketFactory extends SSLSocketFactory {
	    SSLContext sslContext = SSLContext.getInstance("TLS");
	
	    public EvatureSSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	        super(truststore);
	
	        TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }
	
	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }
	
	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };
	
	        sslContext.init(null, new TrustManager[] { tm }, null);
	    }
	
	
	    @Override
	    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	    }
	
	    @Override
	    public Socket createSocket() throws IOException {
	        return sslContext.getSocketFactory().createSocket();
	    }
	}


	private static HttpClient getHttpClient() throws NoSuchAlgorithmException, KeyManagementException
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
			Log.e(TAG, "UnrecoverableKeyException", e);
		} catch (KeyStoreException e) {
			Log.e(TAG, "KeyStoreException", e);
		}
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		Scheme sch = new Scheme("https", sf, PORT);
		httpclient.getConnectionManager().getSchemeRegistry().register(sch);

		return httpclient;
	}
	
	private static void sendAudioFile(Context context, Uri fileuri, String filekey) {
		HttpClient httpclient = null;
		try {
			httpclient = getHttpClient();

//			String url = "http://10.0.0.2:8000/save_to_s3/"+filekey; 
			String url = "https://vproxy.evaws.com/save_to_s3/"+filekey;
//			Log.i(TAG,"Sending audio file to URI: "+url);

		    HttpPost httppost = new HttpPost(url);
		    ContentResolver contentResolver = context.getContentResolver();
		    InputStreamEntity reqEntity =  new InputStreamEntity( contentResolver.openInputStream(fileuri), -1); //new FileInputStream(file), -1);
		    reqEntity.setContentType("audio/amr");
//		    reqEntity.setChunked(true);
		    httppost.setEntity(reqEntity);
			long t0 = System.nanoTime();
			HttpResponse response = httpclient.execute(httppost);
			long t1 = System.nanoTime();
//			Log.d(TAG, "Response: code="+ response.getStatusLine().getStatusCode()+ ";   Time spent uploading audio file = "+ ((t1 - t0) / 1000000));
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

	
	public static void requestRecordingKey(Intent intent) {
		intent.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
 		intent.putExtra("android.speech.extra.GET_AUDIO", true);
	}

	
	public static String getRecordingKey(Context context, Intent data) {
		if (data != null && data.getExtras() != null) {
			final Uri audioUri = data.getData();
			if (audioUri != null) {
//				Log.d(TAG, "voice file url: "+audioUri);
				final String uuid = UUID.randomUUID().toString();
				final Context fcontext = context;
				
				Thread thread = new Thread(new Runnable(){
				    @Override
				    public void run() {
				        try {
				        	sendAudioFile(fcontext, audioUri, uuid);
				        }
				        catch(Exception e) {
				        	Log.e(TAG, "Exception sending audio file", e);
				        }
				    }
				});
				thread.start();
				return uuid;
			}
		}
		return "";
	}
	
}
