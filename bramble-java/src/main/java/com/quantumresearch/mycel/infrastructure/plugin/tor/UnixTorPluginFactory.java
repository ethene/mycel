package com.quantumresearch.mycel.infrastructure.plugin.tor;

import com.quantumresearch.mycel.infrastructure.api.battery.BatteryManager;
import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.event.EventExecutor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.network.NetworkManager;
import com.quantumresearch.mycel.infrastructure.api.plugin.Backoff;
import com.quantumresearch.mycel.infrastructure.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.infrastructure.api.plugin.PluginCallback;
import com.quantumresearch.mycel.infrastructure.api.plugin.TorControlPort;
import com.quantumresearch.mycel.infrastructure.api.plugin.TorDirectory;
import com.quantumresearch.mycel.infrastructure.api.plugin.TorSocksPort;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;
import org.briarproject.onionwrapper.CircumventionProvider;
import org.briarproject.onionwrapper.LocationUtils;
import org.briarproject.onionwrapper.TorWrapper;
import org.briarproject.onionwrapper.UnixTorWrapper;

import java.io.File;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.net.SocketFactory;

import static java.util.logging.Level.INFO;
import static com.quantumresearch.mycel.infrastructure.util.OsUtils.isLinux;

@Immutable
@NotNullByDefault
public class UnixTorPluginFactory extends TorPluginFactory {

	@Inject
	UnixTorPluginFactory(@IoExecutor Executor ioExecutor,
			@EventExecutor Executor eventExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			NetworkManager networkManager,
			LocationUtils locationUtils,
			EventBus eventBus,
			SocketFactory torSocketFactory,
			BackoffFactory backoffFactory,
			CircumventionProvider circumventionProvider,
			BatteryManager batteryManager,
			Clock clock,
			CryptoComponent crypto,
			@TorDirectory File torDirectory,
			@TorSocksPort int torSocksPort,
			@TorControlPort int torControlPort) {
		super(ioExecutor, eventExecutor, wakefulIoExecutor, networkManager,
				locationUtils, eventBus, torSocketFactory, backoffFactory,
				circumventionProvider, batteryManager, clock, crypto,
				torDirectory, torSocksPort, torControlPort);
	}

	@Nullable
	@Override
	String getArchitectureForTorBinary() {
		if (!isLinux()) return null;
		String arch = System.getProperty("os.arch");
		if (LOG.isLoggable(INFO)) {
			LOG.info("System's os.arch is " + arch);
		}
		//noinspection IfCanBeSwitch
		if (arch.equals("amd64")) return "x86_64";
		else if (arch.equals("aarch64")) return "aarch64";
		else if (arch.equals("arm")) return "armhf";
		return null;
	}

	@Override
	TorPlugin createPluginInstance(Backoff backoff,
			TorRendezvousCrypto torRendezvousCrypto, PluginCallback callback,
			String architecture) {
		TorWrapper tor = new UnixTorWrapper(ioExecutor, eventExecutor,
				architecture, torDirectory, torSocksPort, torControlPort);
		return new TorPlugin(ioExecutor, wakefulIoExecutor, networkManager,
				locationUtils, torSocketFactory, circumventionProvider,
				batteryManager, backoff, torRendezvousCrypto, tor, callback,
				MAX_LATENCY, MAX_IDLE_TIME);
	}
}
