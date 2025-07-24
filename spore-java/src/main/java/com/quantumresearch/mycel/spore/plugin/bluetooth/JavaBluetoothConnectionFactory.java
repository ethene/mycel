package com.quantumresearch.mycel.spore.plugin.bluetooth;

import com.quantumresearch.mycel.spore.api.io.TimeoutMonitor;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

import javax.annotation.concurrent.Immutable;
import javax.microedition.io.StreamConnection;

@Immutable
@NotNullByDefault
class JavaBluetoothConnectionFactory
		implements BluetoothConnectionFactory<StreamConnection> {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final TimeoutMonitor timeoutMonitor;

	JavaBluetoothConnectionFactory(
			BluetoothConnectionLimiter connectionLimiter,
			TimeoutMonitor timeoutMonitor) {
		this.connectionLimiter = connectionLimiter;
		this.timeoutMonitor = timeoutMonitor;
	}

	@Override
	public DuplexTransportConnection wrapSocket(DuplexPlugin plugin,
			StreamConnection socket) throws IOException {
		return new JavaBluetoothTransportConnection(plugin, connectionLimiter,
				timeoutMonitor, socket);
	}
}
