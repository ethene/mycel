package com.quantumresearch.mycel.infrastructure.db;

import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseConfig;
import com.quantumresearch.mycel.infrastructure.api.db.TransactionManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.event.EventExecutor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.ShutdownManager;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;

import java.sql.Connection;
import java.util.concurrent.Executor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DatabaseModule {

	@Provides
	@Singleton
	Database<Connection> provideDatabase(DatabaseConfig config,
			MessageFactory messageFactory, Clock clock) {
		return new H2Database(config, messageFactory, clock);
	}

	@Provides
	@Singleton
	DatabaseComponent provideDatabaseComponent(Database<Connection> db,
			EventBus eventBus, @EventExecutor Executor eventExecutor,
			ShutdownManager shutdownManager) {
		return new DatabaseComponentImpl<>(db, Connection.class, eventBus,
				eventExecutor, shutdownManager);
	}

	@Provides
	TransactionManager provideTransactionManager(DatabaseComponent db) {
		return db;
	}
}
