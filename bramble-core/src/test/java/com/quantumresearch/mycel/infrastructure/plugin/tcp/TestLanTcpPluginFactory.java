package com.quantumresearch.mycel.infrastructure.plugin.tcp;

import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.plugin.Backoff;
import com.quantumresearch.mycel.infrastructure.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginCallback;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.infrastructure.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.plugin.LanTcpConstants.ID;

@Immutable
@NotNullByDefault
@SuppressWarnings("unused") // This class is used in briar-desktop
public class TestLanTcpPluginFactory implements DuplexPluginFactory {

	private static final int MAX_LATENCY = 30_000; // 30 seconds
	private static final int MAX_IDLE_TIME = 30_000; // 30 seconds
	private static final int CONNECTION_TIMEOUT = 3_000; // 3 seconds
	private static final int MIN_POLLING_INTERVAL = 60_000; // 1 minute
	private static final int MAX_POLLING_INTERVAL = 600_000; // 10 mins
	private static final double BACKOFF_BASE = 1.2;

	private final Executor ioExecutor, wakefulIoExecutor;
	private final EventBus eventBus;
	private final BackoffFactory backoffFactory;

	@Inject
	public TestLanTcpPluginFactory(@IoExecutor Executor ioExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			EventBus eventBus,
			BackoffFactory backoffFactory) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.eventBus = eventBus;
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
		Backoff backoff = backoffFactory.createBackoff(MIN_POLLING_INTERVAL,
				MAX_POLLING_INTERVAL, BACKOFF_BASE);
		LanTcpPlugin plugin = new LanTcpPlugin(ioExecutor, wakefulIoExecutor,
				backoff, callback, MAX_LATENCY, MAX_IDLE_TIME,
				CONNECTION_TIMEOUT) {
			@Override
			protected boolean canConnectToOwnAddress() {
				return true;
			}
		};
		eventBus.addListener(plugin);
		return plugin;
	}
}
