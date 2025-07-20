package com.quantumresearch.mycel.infrastructure.transport;

import com.quantumresearch.mycel.infrastructure.api.crypto.StreamDecrypterFactory;
import com.quantumresearch.mycel.infrastructure.api.crypto.StreamEncrypterFactory;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TransportModule {

	public static class EagerSingletons {
		@Inject
		KeyManager keyManager;
	}

	@Provides
	StreamReaderFactory provideStreamReaderFactory(
			StreamDecrypterFactory streamDecrypterFactory) {
		return new StreamReaderFactoryImpl(streamDecrypterFactory);
	}

	@Provides
	StreamWriterFactory provideStreamWriterFactory(
			StreamEncrypterFactory streamEncrypterFactory) {
		return new StreamWriterFactoryImpl(streamEncrypterFactory);
	}

	@Provides
	TransportKeyManagerFactory provideTransportKeyManagerFactory(
			TransportKeyManagerFactoryImpl transportKeyManagerFactory) {
		return transportKeyManagerFactory;
	}

	@Provides
	@Singleton
	KeyManager provideKeyManager(LifecycleManager lifecycleManager,
			EventBus eventBus, KeyManagerImpl keyManager) {
		lifecycleManager.registerService(keyManager);
		eventBus.addListener(keyManager);
		return keyManager;
	}
}
