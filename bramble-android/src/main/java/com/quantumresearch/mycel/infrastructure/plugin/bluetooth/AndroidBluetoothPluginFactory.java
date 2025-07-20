package com.quantumresearch.mycel.infrastructure.plugin.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothSocket;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.io.TimeoutMonitor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.plugin.Backoff;
import com.quantumresearch.mycel.infrastructure.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginCallback;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.infrastructure.api.system.AndroidExecutor;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.SecureRandom;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.plugin.BluetoothConstants.ID;

@Immutable
@NotNullByDefault
public class AndroidBluetoothPluginFactory implements DuplexPluginFactory {

	private static final int MAX_LATENCY = 30 * 1000; // 30 seconds
	private static final int MAX_IDLE_TIME = 30 * 1000; // 30 seconds
	private static final int MIN_POLLING_INTERVAL = 60 * 1000; // 1 minute
	private static final int MAX_POLLING_INTERVAL = 10 * 60 * 1000; // 10 mins
	private static final double BACKOFF_BASE = 1.2;

	private final Executor ioExecutor, wakefulIoExecutor;
	private final AndroidExecutor androidExecutor;
	private final AndroidWakeLockManager wakeLockManager;
	private final Application app;
	private final SecureRandom secureRandom;
	private final EventBus eventBus;
	private final Clock clock;
	private final TimeoutMonitor timeoutMonitor;
	private final BackoffFactory backoffFactory;

	@Inject
	AndroidBluetoothPluginFactory(@IoExecutor Executor ioExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			AndroidExecutor androidExecutor,
			AndroidWakeLockManager wakeLockManager,
			Application app,
			SecureRandom secureRandom,
			EventBus eventBus,
			Clock clock,
			TimeoutMonitor timeoutMonitor,
			BackoffFactory backoffFactory) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.androidExecutor = androidExecutor;
		this.wakeLockManager = wakeLockManager;
		this.app = app;
		this.secureRandom = secureRandom;
		this.eventBus = eventBus;
		this.clock = clock;
		this.timeoutMonitor = timeoutMonitor;
		this.backoffFactory = backoffFactory;
	}

	@Override
	public TransportId getId() {
		return ID;
	}

	@Override
	public long getMaxLatency() {
		return MAX_LATENCY;
	}

	@Override
	public DuplexPlugin createPlugin(PluginCallback callback) {
		BluetoothConnectionLimiter connectionLimiter =
				new BluetoothConnectionLimiterImpl(eventBus);
		BluetoothConnectionFactory<BluetoothSocket> connectionFactory =
				new AndroidBluetoothConnectionFactory(connectionLimiter,
						wakeLockManager, timeoutMonitor);
		Backoff backoff = backoffFactory.createBackoff(MIN_POLLING_INTERVAL,
				MAX_POLLING_INTERVAL, BACKOFF_BASE);
		AndroidBluetoothPlugin plugin = new AndroidBluetoothPlugin(
				connectionLimiter, connectionFactory, ioExecutor,
				wakefulIoExecutor, secureRandom, androidExecutor, app,
				clock, backoff, callback, MAX_LATENCY, MAX_IDLE_TIME);
		eventBus.addListener(plugin);
		return plugin;
	}
}
