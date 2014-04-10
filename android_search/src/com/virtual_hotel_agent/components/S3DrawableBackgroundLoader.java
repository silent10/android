package com.virtual_hotel_agent.components;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

import com.virtual_hotel_agent.search.VHAApplication;

public class S3DrawableBackgroundLoader {
	private final LruCache<String, Drawable> mCache = new LruCache<String, Drawable>(MAX_CACHE_SIZE);
	private ExecutorService mThreadPool;
	private final Map<ImageView, String> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());

	public static int MAX_CACHE_SIZE = 80;
	public int THREAD_POOL_SIZE = 5;

	static S3DrawableBackgroundLoader mTheObject = null;

	private static final String TAG = "DrawableBackgroundLoader";

	private static class DrawableBackgroundLoaderHolder {
		public static final S3DrawableBackgroundLoader instance = new S3DrawableBackgroundLoader();
	}

	public static S3DrawableBackgroundLoader getInstance() {
		return DrawableBackgroundLoaderHolder.instance;
	}

	ExecutorService createThreadPool() {
		return S3LifoThreadPoolExecutor.createInstance(THREAD_POOL_SIZE);
	}

	S3DrawableBackgroundLoader() {
		mThreadPool = createThreadPool();
	}

	/**
	 * Clears all instance data and stops running threads
	 */
	public void Reset() {
		ExecutorService oldThreadPool = mThreadPool;
		mThreadPool = createThreadPool();
		oldThreadPool.shutdownNow();

		//mChacheController.clear();
		//mCache.clear();
		mCache.evictAll();
		mImageViews.clear();
	}

	abstract class SourceContainer {
		public static final int SOURCE_CONTACTS_CONTENT_RESOLVER = 1;
		public static final int SOURCE_WEB_URL = 2;

		int mType;

		SourceContainer(int type) {
			mType = type;
		}

		abstract public Drawable getDrawable(String lookup);
	}

	class SourceContainerWebUrl extends SourceContainer {
		String mUrl;

		SourceContainerWebUrl(String url) {
			super(SOURCE_WEB_URL);
			mUrl = url;
		}

		@Override
		public Drawable getDrawable(String lookup) {
			URL url = null;
			try {
				url = new URL(mUrl);
			} catch (MalformedURLException e) {
				VHAApplication.logError(TAG, "Maformed URL in S3Drawable", e);
				return null;
			}

			URLConnection connection = null;

			Object response = null;
			try {
				connection = url.openConnection();
				connection.setUseCaches(true); //
				response = connection.getContent();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				VHAApplication.logError(TAG, "IO exception", e);
				return null;
			} catch (OutOfMemoryError e) {
				VHAApplication.logError(TAG, "Out of memory", e);
				return null;
			}

			return Drawable.createFromStream((InputStream) response, mUrl);

		}
	}

	public void loadDrawable(String url, final ImageView imageView, Drawable placeholder) {
		loadDrawable(new SourceContainerWebUrl(url), url, imageView, placeholder);
	}

	private void loadDrawable(SourceContainer sourceContainer, String lookup, final ImageView imageView,
			Drawable placeholder) {

		mImageViews.put(imageView, lookup);
		Drawable drawable = getDrawableFromCache(lookup);

		// check in UI thread, so no concurrency issues
		if (drawable != null) {
			// Log.d(null, "Item loaded from mCache: " + url);
			imageView.setImageDrawable(drawable);
		} else {
			imageView.setImageDrawable(placeholder);
			queueJob(sourceContainer, lookup, imageView, placeholder);
		}
	}

	public Drawable getDrawableFromCache(String lookup) {
		return mCache.get(lookup);
	}

	private synchronized void putDrawableInCache(String lookup, Drawable drawable) {
//		int chacheControllerSize = mChacheController.size();
//		if (chacheControllerSize > MAX_CACHE_SIZE)
//			mChacheController.subList(0, MAX_CACHE_SIZE / 2).clear();
//
//		mChacheController.addLast(drawable);
//		mCache.put(lookup, new SoftReference<Drawable>(drawable));
		mCache.put(lookup, drawable);
	}

	private void queueJob(final SourceContainer sourceContainer, final String lookup, final ImageView imageView,
			final Drawable placeholder) {
		/* Create handler in UI thread. */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String tag = mImageViews.get(imageView);
				if (tag != null && tag.equals(lookup)) {
					if (imageView.isShown())
						if (msg.obj != null) {
							imageView.setImageDrawable((Drawable) msg.obj);
						} else {
							imageView.setImageDrawable(placeholder);
						}
				}
			}
		};

		mThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

				final Drawable bmp = downloadDrawable(sourceContainer, lookup);
				// if the view is not visible anymore, the image will be ready for next time in cache
				if (imageView.isShown()) {
					Message message = Message.obtain();
					message.obj = bmp;

					handler.sendMessage(message);
				}

			}
		});
	}

	private Drawable downloadDrawable(SourceContainer sourceContainer, String lookup) {

		Drawable drawable = sourceContainer.getDrawable(lookup);
		putDrawableInCache(lookup, drawable);
		return drawable;
	}
}
