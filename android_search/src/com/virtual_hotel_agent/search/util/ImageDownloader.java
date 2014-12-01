package com.virtual_hotel_agent.search.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;

public class ImageDownloader {

	private static final String TAG = "ImageDownloader";
	private boolean mRunThreads;
	private LruCache<String, Bitmap> cache;
	private Handler handler;
	private Thread mImageDownloadThread;
	private Handler doneHandler;

	public ImageDownloader(LruCache<String, Bitmap> cache, Handler handler) {
		this(cache, handler, null);
	}
	
	public ImageDownloader(LruCache<String, Bitmap> cache, Handler handler, Handler doneAllHandler) {
		this.cache = cache;
		this.handler = handler;
		this.doneHandler = doneAllHandler;
		mRunThreads = false;
		mImageDownloadThread = null;
	}
	
	public void stopDownload() {
		Log.d(TAG, "Stopping downloader");
		mRunThreads = false;
		handler = null;
		doneHandler = null;
	}
	
	public static Bitmap download_Image(String path) {
		URL url = null;
		Bitmap bmp;

		try {
			url = new URL(path);
		} catch (MalformedURLException e) {
			VHAApplication.logError(TAG, "Maformed URL in ImageDownloader", e);
			return null;
		}

		URLConnection connection = null;

		Object response = null;
		try {
			connection = url.openConnection();
			connection.setUseCaches(true);
			response = connection.getContent();
			bmp = BitmapFactory.decodeStream((InputStream) response);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			VHAApplication.logError(TAG, "IOException loading bitmap", e);
			return null;
		} catch (OutOfMemoryError e) {
			VHAApplication.logError(TAG, "Out of memory loading bmp", e);
			return null;
		}

		return bmp;
	}
	
	
	public void startDownload(final List<String> urls) {
		mRunThreads = true;
		mImageDownloadThread = new Thread() {

			@Override
			public void run() {
				Log.d(TAG, "Starting downloading "+Thread.currentThread().getId());
				Thread.currentThread().setPriority(MIN_PRIORITY);
				Bitmap bmp;
				
				if(urls == null || urls.size() == 0) {
					return;
				}
				
				int count = 0;
				for (String url : urls) {
					if (mRunThreads) {
						bmp = null;
						if (url != null) {
							bmp = cache.get(url);
							if (bmp == null) {
								//Log.d(TAG, url+" not found - downloading");
								bmp = download_Image(url);
								if (bmp != null) {
									synchronized (cache) {
										if (cache.get(url) == null) {
											cache.put(url, bmp);
										}
									}
								}
								//Log.d(TAG, "Now in cache: "+cache.size()+" kb");
							}
						}

						if (bmp != null && handler != null) {
							Message message = Message.obtain();
							message.obj = bmp;
							message.arg1 = count++;
							handler.sendMessage(message);
						}
					}
				}
				
				if (doneHandler != null) {
					Message message = Message.obtain();
					message.obj = null;
					doneHandler.sendMessage(message);
				}
				Log.d(TAG, "Done downloading "+Thread.currentThread().getId() + " mRunThread="+mRunThreads);
			}
		};
		mImageDownloadThread.start();
	}
}
