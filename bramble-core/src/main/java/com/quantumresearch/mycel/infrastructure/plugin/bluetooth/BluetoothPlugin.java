package com.quantumresearch.mycel.infrastructure.plugin.bluetooth;

import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
public interface BluetoothPlugin extends DuplexPlugin {

	boolean isDiscovering();

	void disablePolling();

	void enablePolling();

	@Nullable
	DuplexTransportConnection discoverAndConnectForSetup(String uuid);

	void stopDiscoverAndConnect();
}
