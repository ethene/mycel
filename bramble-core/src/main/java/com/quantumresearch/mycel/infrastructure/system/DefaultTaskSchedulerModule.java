package com.quantumresearch.mycel.infrastructure.system;

import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.system.TaskScheduler;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DefaultTaskSchedulerModule {

	public static class EagerSingletons {
		@Inject
		TaskScheduler scheduler;
	}

	@Provides
	@Singleton
	TaskScheduler provideTaskScheduler(LifecycleManager lifecycleManager,
			ThreadFactory threadFactory) {
		// Discard tasks that are submitted during shutdown
		RejectedExecutionHandler policy =
				new ScheduledThreadPoolExecutor.DiscardPolicy();
		ScheduledExecutorService scheduledExecutorService =
				new ScheduledThreadPoolExecutor(1, threadFactory, policy);
		lifecycleManager.registerForShutdown(scheduledExecutorService);
		return new TaskSchedulerImpl(scheduledExecutorService);
	}
}
