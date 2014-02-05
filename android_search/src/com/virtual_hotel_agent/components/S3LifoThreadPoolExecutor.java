package com.virtual_hotel_agent.components;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class S3LifoThreadPoolExecutor extends ThreadPoolExecutor {

	public S3LifoThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		// TODO Auto-generated constructor stub
	}

	static S3LifoThreadPoolExecutor createInstance(int threadPoolSize) {
		S3BlockingDeque<Runnable> queue = new S3LinkedBlockingDeque<Runnable>();

		return new S3LifoThreadPoolExecutor(threadPoolSize, Integer.MAX_VALUE, Long.MAX_VALUE, TimeUnit.SECONDS, queue);
	}

};