package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.event.EventExecutor;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginManager;
import com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveTask;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class RemovableDriveTaskFactoryImpl implements RemovableDriveTaskFactory {

	private final DatabaseComponent db;
	private final Executor eventExecutor;
	private final PluginManager pluginManager;
	private final ConnectionManager connectionManager;
	private final EventBus eventBus;

	@Inject
	RemovableDriveTaskFactoryImpl(
			DatabaseComponent db,
			@EventExecutor Executor eventExecutor,
			PluginManager pluginManager,
			ConnectionManager connectionManager,
			EventBus eventBus) {
		this.db = db;
		this.eventExecutor = eventExecutor;
		this.pluginManager = pluginManager;
		this.connectionManager = connectionManager;
		this.eventBus = eventBus;
	}

	@Override
	public RemovableDriveTask createReader(RemovableDriveTaskRegistry registry,
			TransportProperties p) {
		return new RemovableDriveReaderTask(eventExecutor, pluginManager,
				connectionManager, eventBus, registry, p);
	}

	@Override
	public RemovableDriveTask createWriter(RemovableDriveTaskRegistry registry,
			ContactId c, TransportProperties p) {
		return new RemovableDriveWriterTask(db, eventExecutor, pluginManager,
				connectionManager, eventBus, registry, c, p);
	}
}
