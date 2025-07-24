package com.quantumresearch.mycel.spore.plugin.tor;

import android.app.Application;

import org.briarproject.android.dontkillmelib.wakelock.AndroidWakeLockManager;
import com.quantumresearch.mycel.spore.api.battery.BatteryManager;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.event.EventExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.network.NetworkManager;
import com.quantumresearch.mycel.spore.api.plugin.Backoff;
import com.quantumresearch.mycel.spore.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.TorControlPort;
import com.quantumresearch.mycel.spore.api.plugin.TorDirectory;
import com.quantumresearch.mycel.spore.api.plugin.TorSocksPort;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;
import org.briarproject.onionwrapper.AndroidTorWrapper;
import org.briarproject.onionwrapper.CircumventionProvider;
import org.briarproject.onionwrapper.LocationUtils;
import org.briarproject.onionwrapper.TorWrapper;

import java.io.File;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.net.SocketFactory;

import static com.quantumresearch.mycel.spore.util.AndroidUtils.getSupportedArchitectures;

@Immutable
@NotNullByDefault
public class AndroidTorPluginFactory extends TorPluginFactory {

	private final Application app;
	private final AndroidWakeLockManager wakeLockManager;

	@Inject
	AndroidTorPluginFactory(@IoExecutor Executor ioExecutor,
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
			@TorControlPort int torControlPort,
			Application app,
			AndroidWakeLockManager wakeLockManager) {
		super(ioExecutor, eventExecutor, wakefulIoExecutor, networkManager,
				locationUtils, eventBus, torSocketFactory, backoffFactory,
				circumventionProvider, batteryManager, clock, crypto,
				torDirectory, torSocksPort, torControlPort);
		this.app = app;
		this.wakeLockManager = wakeLockManager;
	}

	@Nullable
	@Override
	String getArchitectureForTorBinary() {
		for (String abi : getSupportedArchitectures()) {
			if (abi.startsWith("x86_64")) return "x86_64_pie";
			else if (abi.startsWith("x86")) return "x86_pie";
			else if (abi.startsWith("arm64")) return "arm64_pie";
			else if (abi.startsWith("armeabi")) return "arm_pie";
		}
		return null;
	}

	@Override
	TorPlugin createPluginInstance(Backoff backoff,
			TorRendezvousCrypto torRendezvousCrypto, PluginCallback callback,
			String architecture) {
		TorWrapper tor = new AndroidTorWrapper(app, wakeLockManager,
				ioExecutor, eventExecutor, architecture, torDirectory,
				torSocksPort, torControlPort);
		return new TorPlugin(ioExecutor, wakefulIoExecutor,
				networkManager, locationUtils, torSocketFactory,
				circumventionProvider, batteryManager, backoff,
				torRendezvousCrypto, tor, callback, MAX_LATENCY,
				MAX_IDLE_TIME);
	}
}
