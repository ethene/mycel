package com.quantumresearch.mycel.infrastructure.reliability;

import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.reliability.ReliabilityLayer;
import com.quantumresearch.mycel.infrastructure.api.reliability.ReliabilityLayerFactory;
import com.quantumresearch.mycel.infrastructure.api.reliability.WriteHandler;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.system.SystemClock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ReliabilityLayerFactoryImpl implements ReliabilityLayerFactory {

	private final Executor ioExecutor;
	private final Clock clock;

	@Inject
	ReliabilityLayerFactoryImpl(@IoExecutor Executor ioExecutor) {
		this.ioExecutor = ioExecutor;
		clock = new SystemClock();
	}

	@Override
	public ReliabilityLayer createReliabilityLayer(WriteHandler writeHandler) {
		return new ReliabilityLayerImpl(ioExecutor, clock, writeHandler);
	}
}
