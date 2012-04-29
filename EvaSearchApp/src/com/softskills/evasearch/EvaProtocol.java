package com.softskills.evasearch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class EvaProtocol {
	
	public static final String TAG="EVP";
	private static final int HTTP_STATUS_OK = 200;
	
	private static final int HTTP_TIMEOUT = 8000;
	
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
        	
            HttpResponse response = executeWithTimeout(client,request);

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
