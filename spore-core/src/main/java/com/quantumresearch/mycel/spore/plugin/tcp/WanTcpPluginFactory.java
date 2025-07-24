package com.quantumresearch.mycel.spore.plugin.tcp;

import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.ShutdownManager;
import com.quantumresearch.mycel.spore.api.plugin.Backoff;
import com.quantumresearch.mycel.spore.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.spore.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.plugin.WanTcpConstants.ID;

@Immutable
@NotNullByDefault
public class WanTcpPluginFactory implements DuplexPluginFactory {

	private static final int MAX_LATENCY = 30_000; // 30 seconds
	private static final int MAX_IDLE_TIME = 30_000; // 30 seconds
	private static final int CONNECTION_TIMEOUT = 30_000; // 30 seconds
	private static final int MIN_POLLING_INTERVAL = 60_000; // 1 minute
	private static final int MAX_POLLING_INTERVAL = 600_000; // 10 mins
	private static final double BACKOFF_BASE = 1.2;

	private final Executor ioExecutor, wakefulIoExecutor;
	private final EventBus eventBus;
	private final BackoffFactory backoffFactory;
	private final ShutdownManager shutdownManager;

	@Inject
	public WanTcpPluginFactory(@IoExecutor Executor ioExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			EventBus eventBus,
			BackoffFactory backoffFactory,
			ShutdownManager shutdownManager) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.eventBus = eventBus;
		this.backoffFactory = backoffFactory;
		this.shutdownManager = shutdownManager;
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
		PortMapper portMapper = new PortMapperImpl(shutdownManager);
		WanTcpPlugin plugin = new WanTcpPlugin(ioExecutor, wakefulIoExecutor,
				backoff, portMapper, callback, MAX_LATENCY, MAX_IDLE_TIME,
				CONNECTION_TIMEOUT);
		eventBus.addListener(plugin);
		return plugin;
	}
}
