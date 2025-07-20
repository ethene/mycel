package com.quantumresearch.mycel.infrastructure.system;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.system.WakefulIoExecutor;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;

@Module
public
class AndroidWakefulIoExecutorModule {

	@Provides
	@WakefulIoExecutor
	Executor provideWakefulIoExecutor(@IoExecutor Executor ioExecutor,
			AndroidWakeLockManager wakeLockManager) {
		return r -> wakeLockManager.executeWakefully(r, ioExecutor,
				"WakefulIoExecutor");
	}
}
