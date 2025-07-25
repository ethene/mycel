package com.quantumresearch.mycel.spore.sync.validation;

import com.quantumresearch.mycel.spore.PoliteExecutor;
import com.quantumresearch.mycel.spore.api.crypto.CryptoExecutor;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.sync.validation.ValidationManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ValidationModule {

	public static class EagerSingletons {
		@Inject
		ValidationManager validationManager;
	}

	/**
	 * The maximum number of validation tasks to delegate to the crypto
	 * executor concurrently.
	 * <p>
	 * The number of available processors can change during the lifetime of the
	 * JVM, so this is just a reasonable guess.
	 */
	private static final int MAX_CONCURRENT_VALIDATION_TASKS =
			Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	@Provides
	@Singleton
	ValidationManager provideValidationManager(
			LifecycleManager lifecycleManager, EventBus eventBus,
			ValidationManagerImpl validationManager) {
		lifecycleManager.registerService(validationManager);
		eventBus.addListener(validationManager);
		return validationManager;
	}

	@Provides
	@Singleton
	@ValidationExecutor
	Executor provideValidationExecutor(
			@CryptoExecutor Executor cryptoExecutor) {
		return new PoliteExecutor("ValidationExecutor", cryptoExecutor,
				MAX_CONCURRENT_VALIDATION_TASKS);
	}
}
