package com.quantumresearch.mycel.infrastructure.connection;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ConnectionModule {

	@Provides
	ConnectionManager provideConnectionManager(
			ConnectionManagerImpl connectionManager) {
		return connectionManager;
	}

	@Provides
	@Singleton
	ConnectionRegistry provideConnectionRegistry(
			ConnectionRegistryImpl connectionRegistry) {
		return connectionRegistry;
	}
}
