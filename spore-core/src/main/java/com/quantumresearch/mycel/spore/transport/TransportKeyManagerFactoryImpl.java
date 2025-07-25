package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DatabaseExecutor;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.system.TaskScheduler;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class TransportKeyManagerFactoryImpl implements
		TransportKeyManagerFactory {

	private final DatabaseComponent db;
	private final TransportCrypto transportCrypto;
	private final Executor dbExecutor;
	private final TaskScheduler scheduler;
	private final Clock clock;

	@Inject
	TransportKeyManagerFactoryImpl(DatabaseComponent db,
			TransportCrypto transportCrypto,
			@DatabaseExecutor Executor dbExecutor,
			TaskScheduler scheduler,
			Clock clock) {
		this.db = db;
		this.transportCrypto = transportCrypto;
		this.dbExecutor = dbExecutor;
		this.scheduler = scheduler;
		this.clock = clock;
	}

	@Override
	public TransportKeyManager createTransportKeyManager(
			TransportId transportId, long maxLatency) {
		return new TransportKeyManagerImpl(db, transportCrypto, dbExecutor,
				scheduler, clock, transportId, maxLatency);
	}

}
