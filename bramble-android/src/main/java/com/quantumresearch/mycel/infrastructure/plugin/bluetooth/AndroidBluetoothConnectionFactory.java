package com.quantumresearch.mycel.infrastructure.plugin.bluetooth;

import android.bluetooth.BluetoothSocket;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import com.quantumresearch.mycel.infrastructure.api.io.TimeoutMonitor;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
class AndroidBluetoothConnectionFactory
		implements BluetoothConnectionFactory<BluetoothSocket> {

	private final BluetoothConnectionLimiter connectionLimiter;
	private final AndroidWakeLockManager wakeLockManager;
	private final TimeoutMonitor timeoutMonitor;

	AndroidBluetoothConnectionFactory(
			BluetoothConnectionLimiter connectionLimiter,
			AndroidWakeLockManager wakeLockManager,
			TimeoutMonitor timeoutMonitor) {
		this.connectionLimiter = connectionLimiter;
		this.wakeLockManager = wakeLockManager;
		this.timeoutMonitor = timeoutMonitor;
	}

	@Override
	public DuplexTransportConnection wrapSocket(DuplexPlugin plugin,
			BluetoothSocket s) throws IOException {
		return new AndroidBluetoothTransportConnection(plugin,
				connectionLimiter, wakeLockManager, timeoutMonitor, s);
	}
}
