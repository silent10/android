package com.virtual_hotel_agent.components;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.LruCache;
import android.widget.ImageView;

import com.ean.mobile.hotel.HotelImageTuple;
import com.evature.util.Log;
import com.virtual_hotel_agent.search.VHAApplication;

/***
 * Loads images in background
 * Thread pooled and cached
 */
public class S3DrawableBackgroundLoader {
	private final LruCache<String, BitmapDrawable> mCache;
	private ExecutorService mThreadPool;

	// from imageView to cache key
	private final Map<ImageView, String> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private int mThreadPoolSize;

	private static int THREAD_POOL_SIZE = 5;

	static S3DrawableBackgroundLoader mTheObject = null;

	private static final String TAG = "DrawableBackgroundLoader";


	ExecutorService createThreadPool() {
		return S3LifoThreadPoolExecutor.createInstance(mThreadPoolSize);
	}

	public S3DrawableBackgroundLoader(int threadPoolSize, LruCache<String, BitmapDrawable> cache) {
		if (threadPoolSize <= 0) {
			threadPoolSize = THREAD_POOL_SIZE;
		}
		mThreadPoolSize = threadPoolSize;
		mThreadPool = createThreadPool();
		mCache = cache;
	}

	public static interface LoadedCallback {
		void drawableLoaded(boolean success, BitmapDrawable drawable);
	}
	
	/**
	 * Clears all instance data and stops running threads
	 */
	public void Reset() {
		ExecutorService oldThreadPool = mThreadPool;
		mThreadPool = createThreadPool();
		oldThreadPool.shutdownNow();

		// mCache.evictAll(); 
		mImageViews.clear();
	}

	abstract class SourceContainer {
		public static final int SOURCE_CONTACTS_CONTENT_RESOLVER = 1;
		public static final int SOURCE_WEB_URL = 2;
		public static final int SOURCE_EXPEDIA_IMG = 3;

		int mType;

		SourceContainer(int type) {
			mType = type;
		}

		abstract public BitmapDrawable getDrawable() throws IOException;
	}

	class SourceContainerWebUrl extends SourceContainer {
		String mUrl;

		SourceContainerWebUrl(String url) {
			super(SOURCE_WEB_URL);
			mUrl = url;
		}

		@Override
		public BitmapDrawable getDrawable() throws IOException {
			URL url = null;
			try {
				url = new URL(mUrl);
			} catch (MalformedURLException e) {
				if (mUrl == null) {
					mUrl= "<null>";
				}
				else if (mUrl.equals("")) {
					mUrl = "<empty>";
				}
				VHAApplication.logError(TAG, "Maformed URL in S3Drawable: "+mUrl, e);
				return null;
			}

			URLConnection connection = null;

			Object response = null;
			connection = url.openConnection();
			connection.setUseCaches(true); //
			response = connection.getContent();

			return new BitmapDrawable((InputStream) response);

		}
	}
	
	class ExpediaSourceContainerUrl extends SourceContainer {
		
		private HotelImageTuple imgData;
		private boolean isThumbnail;

		ExpediaSourceContainerUrl(HotelImageTuple imgData, boolean isThumbnail) {
			super(SOURCE_EXPEDIA_IMG);
			this.imgData = imgData;
			this.isThumbnail = isThumbnail;
		}

		@Override
		public BitmapDrawable getDrawable() throws IOException {
			URL url = null;
			if (isThumbnail) {
				url = imgData.thumbnailUrl;
			}
			else {
				url = imgData.mainUrl;
			}
			if (url == null)
				return null;

			while (true) {
				SourceContainerWebUrl source = new SourceContainerWebUrl(url.toString());
				try {
					BitmapDrawable result = source.getDrawable();
					return result;
				}
				catch(FileNotFoundException e) {
					boolean downgraded;
					if (isThumbnail) {
						downgraded = imgData.downgradeThumbnailResolution();
					}
					else {
						downgraded = imgData.downgradeImgResolution();
					}
					if (!downgraded) {
						// can't downgrade anymore - raise the Not Found exception
						throw e;
					}
					// else - downgraded image resolution - now try again
				}
			}
		}
	}

	public void loadDrawable(String url, final ImageView imageView, BitmapDrawable placeholder, LoadedCallback callback) {
		loadDrawable(new SourceContainerWebUrl(url), url, imageView, placeholder, callback);
	}
	
	public void loadDrawable(HotelImageTuple image, boolean thumbnail, final ImageView imageView, BitmapDrawable placeholder, LoadedCallback callback) {
		loadDrawable(new ExpediaSourceContainerUrl(image, thumbnail), 
				thumbnail ? image.thumbnailUrl.toString() : image.mainUrl.toString(), 
						imageView, placeholder, callback);
	}
	
	private void loadDrawable(SourceContainer sourceContainer, String lookup, final ImageView imageView,
			BitmapDrawable placeholder, LoadedCallback callback) {

//		Log.d(TAG, "Loading url "+lookup +" to imageView "+imageView);

		mImageViews.put(imageView, lookup);
		BitmapDrawable drawable = getDrawableFromCache(lookup);

		// check in UI thread, so no concurrency issues
		if (drawable != null) {
			// Log.d(null, "Item loaded from mCache: " + url);
			imageView.setImageDrawable(drawable);
			if (callback != null)
				callback.drawableLoaded(true, drawable);
		} else {
			if (placeholder != null)
				imageView.setImageDrawable(placeholder);
			queueJob(sourceContainer, lookup, imageView, placeholder, callback);
		}
	}

	public BitmapDrawable getDrawableFromCache(String lookup) {
		if (lookup != null)
			return mCache.get(lookup);
		return null;
	}
	
	private synchronized void removeFromCache(String lookup) {
		if (lookup != null)
			mCache.remove(lookup);
	}

	private synchronized void putDrawableInCache(String lookup, BitmapDrawable drawable) {
//		int chacheControllerSize = mChacheController.size();
//		if (chacheControllerSize > MAX_CACHE_SIZE)
//			mChacheController.subList(0, MAX_CACHE_SIZE / 2).clear();
//
//		mChacheController.addLast(drawable);
//		mCache.put(lookup, new SoftReference<Drawable>(drawable));
		mCache.put(lookup, drawable);
	}

	private void queueJob(final SourceContainer sourceContainer, final String lookup, final ImageView imageView,
			final BitmapDrawable placeholder, final LoadedCallback callback) {
		/* Create handler in UI thread. */
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String tag = mImageViews.get(imageView);
				if (tag != null && tag.equals(lookup)) {
					if (imageView.isShown())
						if (msg.obj != null) {
							imageView.setImageDrawable((Drawable) msg.obj);
							if (callback != null)
								callback.drawableLoaded(true, (BitmapDrawable) msg.obj);
						} else {
							if (placeholder != null)
								imageView.setImageDrawable(placeholder);
							if (callback != null)
								callback.drawableLoaded(false, placeholder);
						}
				}
			}
		};

		mThreadPool.submit(new Runnable() {

			@Override
			public void run() {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

				BitmapDrawable bmp = null;
				int error = 0;
				try {
					bmp = downloadDrawable(sourceContainer, lookup);
				}
				catch (FileNotFoundException e) {
					Log.w(TAG, "Image not found");
					error = 2;
				} catch (IOException e) {
					VHAApplication.logError(TAG, "Error loading image", e);
					error = 1;
				}
				
				// if the view is not visible anymore, the image will be ready for next time in cache
				if (imageView.isShown()) {
					Message message = Message.obtain();
					message.obj = bmp;
					message.what = error;

					handler.sendMessage(message);
				}

			}
		});
	}

	private BitmapDrawable downloadDrawable(SourceContainer sourceContainer, String lookup) throws IOException {

		BitmapDrawable drawable = sourceContainer.getDrawable();
		if (drawable != null)
			putDrawableInCache(lookup, drawable);
		return drawable;
	}
}
