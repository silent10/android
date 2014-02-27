package com.virtual_hotel_agent.search.util;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;

import com.evature.util.Log;
import com.virtual_hotel_agent.search.models.expedia.XpediaProtocolStatic;

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
	
	public void startDownload(final ArrayList<String> urls) {
		mRunThreads = true;
		mImageDownloadThread = new Thread() {

			@Override
			public void run() {
				Log.d(TAG, "Starting downloading "+Thread.currentThread().getId());
				Bitmap bmp;
				
				if(urls == null || urls.size() == 0) {
					return;
				}
				
				for (String url : urls) {
					if (mRunThreads) {
						bmp = null;
						if (url != null) {
							bmp = cache.get(url);
							if (bmp == null) {
								//Log.d(TAG, url+" not found - downloading");
								bmp = XpediaProtocolStatic.download_Image(url);
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
