package com.quantumresearch.mycel.infrastructure.system;

import android.app.Application;

import com.quantumresearch.mycel.infrastructure.api.event.EventExecutor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.system.AndroidExecutor;
import com.quantumresearch.mycel.infrastructure.api.system.ResourceProvider;
import com.quantumresearch.mycel.infrastructure.api.system.SecureRandomProvider;
import org.briarproject.onionwrapper.AndroidLocationUtilsFactory;
import org.briarproject.onionwrapper.LocationUtils;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidSystemModule {

	private final ScheduledExecutorService scheduledExecutorService;

	public AndroidSystemModule() {
		// Discard tasks that are submitted during shutdown
		RejectedExecutionHandler policy =
				new ScheduledThreadPoolExecutor.DiscardPolicy();
		scheduledExecutorService = new ScheduledThreadPoolExecutor(1, policy);
	}

	@Provides
	@Singleton
	ScheduledExecutorService provideScheduledExecutorService(
			LifecycleManager lifecycleManager) {
		lifecycleManager.registerForShutdown(scheduledExecutorService);
		return scheduledExecutorService;
	}

	@Provides
	@Singleton
	SecureRandomProvider provideSecureRandomProvider(
			AndroidSecureRandomProvider provider) {
		return provider;
	}

	@Provides
	@Singleton
	LocationUtils provideLocationUtils(Application app) {
		return AndroidLocationUtilsFactory.createAndroidLocationUtils(app);
	}

	@Provides
	@Singleton
	AndroidExecutor provideAndroidExecutor(
			AndroidExecutorImpl androidExecutor) {
		return androidExecutor;
	}

	@Provides
	@Singleton
	@EventExecutor
	Executor provideEventExecutor(AndroidExecutor androidExecutor) {
		return androidExecutor::runOnUiThread;
	}

	@Provides
	@Singleton
	ResourceProvider provideResourceProvider(AndroidResourceProvider provider) {
		return provider;
	}
}
