package com.evature.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.inject.Singleton;

@Singleton
public class DownloadUrl {
	
	// Iftah: I changed this to be non-static method so that a unit test can inject a mock class and verify URL without trouble
	//        but added @Singleton - which should make Guice reuse a single instance of this class for each instantiation
	
	// Given a URL, establishes an HttpUrlConnection and retrieves
	// the web page content as a InputStream, which it returns as a string.
	public String get(String myurl) throws IOException {
		URL url = new URL(myurl);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(20000 /* milliseconds */);
		conn.setConnectTimeout(25000 /* milliseconds */);
		conn.setRequestMethod("GET");
		conn.addRequestProperty("Accept-Encoding","gzip");
		conn.setDoInput(true);
		// Starts the query
		conn.connect();
		// int response = conn.getResponseCode();
		// Read from web: http://stackoverflow.com/a/1381784/78234
		Reader r = new InputStreamReader(conn.getInputStream(), "UTF-8");
		StringBuilder buf = new StringBuilder();
		char[] sBuffer = new char[512];
		int readBytes = 0;
		while ((readBytes = r.read(sBuffer)) != -1) {
			buf.append(sBuffer, 0, readBytes);
		}
		String str = buf.toString();
		return str;
	}

}
