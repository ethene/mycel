package com.quantumresearch.mycel.spore.cleanup;

import com.quantumresearch.mycel.spore.api.cleanup.CleanupManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CleanupModule {

	public static class EagerSingletons {
		@Inject
		CleanupManager cleanupManager;
	}

	@Provides
	@Singleton
	CleanupManager provideCleanupManager(LifecycleManager lifecycleManager,
			EventBus eventBus, CleanupManagerImpl cleanupManager) {
		lifecycleManager.registerService(cleanupManager);
		eventBus.addListener(cleanupManager);
		return cleanupManager;
	}
}
