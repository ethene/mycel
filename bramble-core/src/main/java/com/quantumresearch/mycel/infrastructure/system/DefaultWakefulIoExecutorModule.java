package com.quantumresearch.mycel.infrastructure.system;

import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.system.WakefulIoExecutor;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;

/**
 * Provides a default implementation of {@link WakefulIoExecutor} for systems
 * without wake locks.
 */
@Module
public class DefaultWakefulIoExecutorModule {

	@Provides
	@WakefulIoExecutor
	Executor provideWakefulIoExecutor(@IoExecutor Executor ioExecutor) {
		return ioExecutor;
	}
}
