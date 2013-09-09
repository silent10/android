package com.evature.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import com.google.inject.Singleton;

@Singleton
public class DownloadUrl {
	
	// Iftah: I changed this to be non-static method so that a unit test can inject a mock class and verify URL without trouble
	//        but added @Singleton - which should make Guice reuse a single instance of this class for each instantiation
	
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as a string.
	public String get(String myurl) throws IOException {
		HttpGet request = new HttpGet(myurl);
		request.addHeader("Accept-Encoding","gzip");

		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, 4000);
		HttpConnectionParams.setSoTimeout(params, 4000);

		DefaultHttpClient client = new DefaultHttpClient();
		client.setParams(params);
		HttpResponse response = client.execute(request);
		
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
}
