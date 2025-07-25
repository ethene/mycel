package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.TimeLoggingExecutor;
import com.quantumresearch.mycel.spore.api.crypto.CryptoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static java.util.concurrent.TimeUnit.SECONDS;

@Module
public class CryptoExecutorModule {

	public static class EagerSingletons {
		@Inject
		@CryptoExecutor
		ExecutorService cryptoExecutor;
	}

	/**
	 * The maximum number of executor threads.
	 * <p>
	 * The number of available processors can change during the lifetime of the
	 * JVM, so this is just a reasonable guess.
	 */
	private static final int MAX_EXECUTOR_THREADS =
			Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	public CryptoExecutorModule() {
	}

	@Provides
	@Singleton
	@CryptoExecutor
	ExecutorService provideCryptoExecutorService(
			LifecycleManager lifecycleManager, ThreadFactory threadFactory) {
		// Use an unbounded queue
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
		// Discard tasks that are submitted during shutdown
		RejectedExecutionHandler policy =
				new ThreadPoolExecutor.DiscardPolicy();
		// Create a limited # of threads and keep them in the pool for 60 secs
		ExecutorService cryptoExecutor = new TimeLoggingExecutor(
				"CryptoExecutor", 0, MAX_EXECUTOR_THREADS, 60, SECONDS, queue,
				threadFactory, policy);
		lifecycleManager.registerForShutdown(cryptoExecutor);
		return cryptoExecutor;
	}

	@Provides
	@CryptoExecutor
	Executor provideCryptoExecutor(
			@CryptoExecutor ExecutorService cryptoExecutor) {
		return cryptoExecutor;
	}
}
