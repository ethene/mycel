package com.quantumresearch.mycel.spore.plugin.modem;

import com.quantumresearch.mycel.spore.api.reliability.ReliabilityLayerFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.system.SystemClock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class ModemFactoryImpl implements ModemFactory {

	private final Executor ioExecutor;
	private final ReliabilityLayerFactory reliabilityFactory;
	private final Clock clock;

	ModemFactoryImpl(Executor ioExecutor,
			ReliabilityLayerFactory reliabilityFactory) {
		this.ioExecutor = ioExecutor;
		this.reliabilityFactory = reliabilityFactory;
		clock = new SystemClock();
	}

	@Override
	public Modem createModem(Modem.Callback callback, String portName) {
		return new ModemImpl(ioExecutor, reliabilityFactory, clock, callback,
				new SerialPortImpl(portName));
	}
}
