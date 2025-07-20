package com.quantumresearch.mycel.infrastructure.plugin.bluetooth;

import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
interface BluetoothConnectionFactory<S> {

	DuplexTransportConnection wrapSocket(DuplexPlugin plugin, S socket)
			throws IOException;
}
