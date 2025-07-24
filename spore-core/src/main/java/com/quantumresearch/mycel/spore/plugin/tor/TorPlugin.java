package com.quantumresearch.mycel.spore.plugin.tor;

import com.quantumresearch.mycel.spore.PoliteExecutor;
import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.battery.BatteryManager;
import com.quantumresearch.mycel.spore.api.battery.event.BatteryEvent;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementListener;
import com.quantumresearch.mycel.spore.api.network.NetworkManager;
import com.quantumresearch.mycel.spore.api.network.NetworkStatus;
import com.quantumresearch.mycel.spore.api.network.event.NetworkStatusEvent;
import com.quantumresearch.mycel.spore.api.plugin.Backoff;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.PluginException;
import com.quantumresearch.mycel.spore.api.plugin.TorConstants;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.rendezvous.KeyMaterialSource;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousEndpoint;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import com.quantumresearch.mycel.spore.api.settings.event.SettingsUpdatedEvent;
import org.briarproject.nullsafety.InterfaceNotNullByDefault;
import org.briarproject.nullsafety.NotNullByDefault;
import org.briarproject.onionwrapper.CircumventionProvider;
import org.briarproject.onionwrapper.CircumventionProvider.BridgeType;
import org.briarproject.onionwrapper.LocationUtils;
import org.briarproject.onionwrapper.TorWrapper;
import org.briarproject.onionwrapper.TorWrapper.HiddenServiceProperties;
import org.briarproject.onionwrapper.TorWrapper.Observer;
import org.briarproject.onionwrapper.TorWrapper.TorState;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.net.SocketFactory;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.DISABLED;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ENABLING;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.INACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.STARTING_STOPPING;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_PREF_PLUGIN_ENABLE;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_PREF_TOR_MOBILE;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_PREF_TOR_NETWORK;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.HS_PRIVATE_KEY_V3;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.ID;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_MOBILE;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_NETWORK;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_NETWORK_AUTOMATIC;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_NETWORK_WITH_BRIDGES;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_ONLY_WHEN_CHARGING;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PREF_TOR_PORT;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.PROP_ONION_V3;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.REASON_BATTERY;
import static com.quantumresearch.mycel.spore.api.plugin.TorConstants.REASON_MOBILE_DATA;
import static com.quantumresearch.mycel.spore.plugin.tor.TorRendezvousCrypto.SEED_BYTES;
import static com.quantumresearch.mycel.spore.util.IoUtils.tryToClose;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.PrivacyUtils.scrubOnion;
import static com.quantumresearch.mycel.spore.util.StringUtils.isNullOrEmpty;
import static org.briarproject.onionwrapper.CircumventionProvider.BridgeType.MEEK;
import static org.briarproject.onionwrapper.CircumventionProvider.BridgeType.SNOWFLAKE;

@InterfaceNotNullByDefault
class TorPlugin implements DuplexPlugin, EventListener {

	protected static final Logger LOG = getLogger(TorPlugin.class.getName());

	private static final Pattern ONION_V3 = Pattern.compile("[a-z2-7]{56}");

	protected final Executor ioExecutor;
	private final Executor wakefulIoExecutor;
	private final Executor connectionStatusExecutor;
	private final NetworkManager networkManager;
	private final LocationUtils locationUtils;
	private final SocketFactory torSocketFactory;
	private final CircumventionProvider circumventionProvider;
	private final BatteryManager batteryManager;
	private final Backoff backoff;
	private final TorRendezvousCrypto torRendezvousCrypto;
	private final TorWrapper tor;
	private final PluginCallback callback;
	private final long maxLatency;
	private final int maxIdleTime;
	private final int socketTimeout;
	private final AtomicBoolean used = new AtomicBoolean(false);

	protected final PluginState state = new PluginState();

	private volatile Settings settings = null;

	TorPlugin(Executor ioExecutor,
			Executor wakefulIoExecutor,
			NetworkManager networkManager,
			LocationUtils locationUtils,
			SocketFactory torSocketFactory,
			CircumventionProvider circumventionProvider,
			BatteryManager batteryManager,
			Backoff backoff,
			TorRendezvousCrypto torRendezvousCrypto,
			TorWrapper tor,
			PluginCallback callback,
			long maxLatency,
			int maxIdleTime) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.networkManager = networkManager;
		this.locationUtils = locationUtils;
		this.torSocketFactory = torSocketFactory;
		this.circumventionProvider = circumventionProvider;
		this.batteryManager = batteryManager;
		this.backoff = backoff;
		this.torRendezvousCrypto = torRendezvousCrypto;
		this.tor = tor;
		this.callback = callback;
		this.maxLatency = maxLatency;
		this.maxIdleTime = maxIdleTime;
		if (maxIdleTime > Integer.MAX_VALUE / 2) {
			socketTimeout = Integer.MAX_VALUE;
		} else {
			socketTimeout = maxIdleTime * 2;
		}
		// Don't execute more than one connection status check at a time
		connectionStatusExecutor =
				new PoliteExecutor("TorPlugin", ioExecutor, 1);
		tor.setObserver(new Observer() {

			@Override
			public void onState(TorState torState) {
				State s = state.getState(torState);
				if (s == ACTIVE) backoff.reset();
				callback.pluginStateChanged(s);
			}

			@Override
			public void onBootstrapPercentage(int percentage) {
			}

			@Override
			public void onHsDescriptorUpload(String onion) {
			}

			@Override
			public void onClockSkewDetected(long skewSeconds) {
			}
		});
	}

	@Override
	public TransportId getId() {
		return TorConstants.ID;
	}

	@Override
	public long getMaxLatency() {
		return maxLatency;
	}

	@Override
	public int getMaxIdleTime() {
		return maxIdleTime;
	}

	@Override
	public void start() throws PluginException {
		if (used.getAndSet(true)) throw new IllegalStateException();
		// Load the settings
		settings = callback.getSettings();
		// Start Tor
		try {
			tor.start();
		} catch (InterruptedException e) {
			LOG.warning("Interrupted while starting Tor");
			Thread.currentThread().interrupt();
			throw new PluginException();
		} catch (IOException e) {
			throw new PluginException(e);
		}
		// Check whether we're online
		updateConnectionStatus(networkManager.getNetworkStatus(),
				batteryManager.isCharging());
		// Bind a server socket to receive incoming hidden service connections
		bind();
	}

	private void bind() {
		ioExecutor.execute(() -> {
			// If there's already a port number stored in config, reuse it
			String portString = settings.get(PREF_TOR_PORT);
			int port;
			if (isNullOrEmpty(portString)) port = 0;
			else port = Integer.parseInt(portString);
			// Bind a server socket to receive connections from Tor
			ServerSocket ss = null;
			try {
				ss = new ServerSocket();
				ss.bind(new InetSocketAddress("127.0.0.1", port));
			} catch (IOException e) {
				logException(LOG, WARNING, e);
				tryToClose(ss, LOG, WARNING);
				return;
			}
			if (!state.setServerSocket(ss)) {
				LOG.info("Closing redundant server socket");
				tryToClose(ss, LOG, WARNING);
				return;
			}
			// Store the port number
			int localPort = ss.getLocalPort();
			Settings s = new Settings();
			s.put(PREF_TOR_PORT, String.valueOf(localPort));
			callback.mergeSettings(s);
			// Create a hidden service if necessary
			ioExecutor.execute(() -> publishHiddenService(localPort));
			backoff.reset();
			// Accept incoming hidden service connections from Tor
			acceptContactConnections(ss);
		});
	}

	private void publishHiddenService(int localPort) {
		if (!tor.isTorRunning()) return;
		String privKey = settings.get(HS_PRIVATE_KEY_V3);
		LOG.info("Creating v3 hidden service");
		HiddenServiceProperties hsProps;
		try {
			hsProps = tor.publishHiddenService(localPort, 80, privKey);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return;
		}
		if (LOG.isLoggable(INFO)) {
			LOG.info("V3 hidden service " + scrubOnion(hsProps.onion));
		}
		if (privKey == null) {
			// Publish the hidden service's onion hostname in transport props
			TransportProperties p = new TransportProperties();
			p.put(PROP_ONION_V3, hsProps.onion);
			callback.mergeLocalProperties(p);
			// Save the hidden service's private key for next time
			Settings s = new Settings();
			s.put(HS_PRIVATE_KEY_V3, hsProps.privKey);
			callback.mergeSettings(s);
		}
	}

	private void acceptContactConnections(ServerSocket ss) {
		while (true) {
			Socket s;
			try {
				s = ss.accept();
				s.setSoTimeout(socketTimeout);
			} catch (IOException e) {
				// This is expected when the server socket is closed
				LOG.info("Server socket closed");
				state.clearServerSocket(ss);
				return;
			}
			LOG.info("Connection received");
			backoff.reset();
			callback.handleConnection(new TorTransportConnection(this, s));
		}
	}

	private void enableBridges(List<BridgeType> bridgeTypes, String countryCode)
			throws IOException {
		if (bridgeTypes.isEmpty()) {
			tor.disableBridges();
		} else {
			List<String> bridges = new ArrayList<>();
			for (BridgeType bridgeType : bridgeTypes) {
				bridges.addAll(circumventionProvider.getBridges(bridgeType,
						countryCode));
			}
			tor.enableBridges(bridges);
		}
	}

	@Override
	public void stop() {
		ServerSocket ss = state.setStopped();
		tryToClose(ss, LOG, WARNING);
		try {
			tor.stop();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		} catch (InterruptedException e) {
			LOG.warning("Interrupted while stopping Tor");
			Thread.currentThread().interrupt();
		}
	}

	@Override
	public State getState() {
		return state.getState();
	}

	@Override
	public int getReasonsDisabled() {
		return state.getReasonsDisabled();
	}

	@Override
	public boolean shouldPoll() {
		return true;
	}

	@Override
	public int getPollingInterval() {
		return backoff.getPollingInterval();
	}

	@Override
	public void poll(Collection<Pair<TransportProperties, ConnectionHandler>>
			properties) {
		if (getState() != ACTIVE) return;
		backoff.increment();
		for (Pair<TransportProperties, ConnectionHandler> p : properties) {
			connect(p.getFirst(), p.getSecond());
		}
	}

	private void connect(TransportProperties p, ConnectionHandler h) {
		wakefulIoExecutor.execute(() -> {
			DuplexTransportConnection d = createConnection(p);
			if (d != null) {
				backoff.reset();
				h.handleConnection(d);
			}
		});
	}

	@Override
	public DuplexTransportConnection createConnection(TransportProperties p) {
		if (getState() != ACTIVE) return null;
		String onion3 = p.get(PROP_ONION_V3);
		if (onion3 != null && !ONION_V3.matcher(onion3).matches()) {
			// Don't scrub the address so we can find the problem
			if (LOG.isLoggable(INFO)) {
				LOG.info("Invalid v3 hostname: " + onion3);
			}
			onion3 = null;
		}
		if (onion3 == null) return null;
		Socket s = null;
		try {
			if (LOG.isLoggable(INFO)) {
				LOG.info("Connecting to v3 " + scrubOnion(onion3));
			}
			s = torSocketFactory.createSocket(onion3 + ".onion", 80);
			s.setSoTimeout(socketTimeout);
			if (LOG.isLoggable(INFO)) {
				LOG.info("Connected to v3 " + scrubOnion(onion3));
			}
			return new TorTransportConnection(this, s);
		} catch (IOException e) {
			if (LOG.isLoggable(INFO)) {
				LOG.info("Could not connect to v3 "
						+ scrubOnion(onion3) + ": " + e);
			}
			tryToClose(s, LOG, WARNING);
			return null;
		}
	}

	@Override
	public boolean supportsKeyAgreement() {
		return false;
	}

	@Override
	public KeyAgreementListener createKeyAgreementListener(byte[] commitment) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DuplexTransportConnection createKeyAgreementConnection(
			byte[] commitment, BdfList descriptor) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean supportsRendezvous() {
		return true;
	}

	@Override
	public RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k,
			boolean alice, ConnectionHandler incoming) {
		byte[] aliceSeed = k.getKeyMaterial(SEED_BYTES);
		byte[] bobSeed = k.getKeyMaterial(SEED_BYTES);
		byte[] localSeed = alice ? aliceSeed : bobSeed;
		byte[] remoteSeed = alice ? bobSeed : aliceSeed;
		String blob = torRendezvousCrypto.getPrivateKeyBlob(localSeed);
		String localOnion = torRendezvousCrypto.getOnion(localSeed);
		String remoteOnion = torRendezvousCrypto.getOnion(remoteSeed);
		TransportProperties remoteProperties = new TransportProperties();
		remoteProperties.put(PROP_ONION_V3, remoteOnion);
		try {
			@SuppressWarnings("resource")
			ServerSocket ss = new ServerSocket();
			ss.bind(new InetSocketAddress("127.0.0.1", 0));
			int port = ss.getLocalPort();
			ioExecutor.execute(() -> {
				try {
					//noinspection InfiniteLoopStatement
					while (true) {
						Socket s = ss.accept();
						incoming.handleConnection(
								new TorTransportConnection(this, s));
					}
				} catch (IOException e) {
					// This is expected when the server socket is closed
					LOG.info("Rendezvous server socket closed");
				}
			});
			tor.publishHiddenService(port, 80, blob);
			return new RendezvousEndpoint() {

				@Override
				public TransportProperties getRemoteTransportProperties() {
					return remoteProperties;
				}

				@Override
				public void close() throws IOException {
					try {
						tor.removeHiddenService(localOnion);
					} finally {
						tryToClose(ss, LOG, WARNING);
					}
				}
			};
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof SettingsUpdatedEvent) {
			SettingsUpdatedEvent s = (SettingsUpdatedEvent) e;
			if (s.getNamespace().equals(ID.getString())) {
				LOG.info("Tor settings updated");
				settings = s.getSettings();
				updateConnectionStatus(networkManager.getNetworkStatus(),
						batteryManager.isCharging());
			}
		} else if (e instanceof NetworkStatusEvent) {
			updateConnectionStatus(((NetworkStatusEvent) e).getStatus(),
					batteryManager.isCharging());
		} else if (e instanceof BatteryEvent) {
			updateConnectionStatus(networkManager.getNetworkStatus(),
					((BatteryEvent) e).isCharging());
		}
	}

	private void updateConnectionStatus(NetworkStatus status,
			boolean charging) {
		connectionStatusExecutor.execute(() -> {
			if (!tor.isTorRunning()) return;
			boolean online = status.isConnected();
			boolean wifi = status.isWifi();
			boolean ipv6Only = status.isIpv6Only();
			String country = locationUtils.getCurrentCountry();
			boolean bridgesByDefault =
					circumventionProvider.shouldUseBridges(country);
			boolean enabledByUser = settings.getBoolean(PREF_PLUGIN_ENABLE,
					DEFAULT_PREF_PLUGIN_ENABLE);
			int network = settings.getInt(PREF_TOR_NETWORK,
					DEFAULT_PREF_TOR_NETWORK);
			boolean useMobile = settings.getBoolean(PREF_TOR_MOBILE,
					DEFAULT_PREF_TOR_MOBILE);
			boolean onlyWhenCharging =
					settings.getBoolean(PREF_TOR_ONLY_WHEN_CHARGING,
							DEFAULT_PREF_TOR_ONLY_WHEN_CHARGING);
			boolean automatic = network == PREF_TOR_NETWORK_AUTOMATIC;

			if (LOG.isLoggable(INFO)) {
				LOG.info("Online: " + online + ", wifi: " + wifi
						+ ", IPv6 only: " + ipv6Only);
				if (country.isEmpty()) LOG.info("Country code unknown");
				else LOG.info("Country code: " + country);
				LOG.info("Charging: " + charging);
			}

			int reasonsDisabled = 0;
			boolean enableNetwork = false, enableConnectionPadding = false;
			List<BridgeType> bridgeTypes = emptyList();

			if (!online) {
				LOG.info("Disabling network, device is offline");
			} else {
				if (!enabledByUser) {
					LOG.info("User has disabled Tor");
					reasonsDisabled |= REASON_USER;
				}
				if (!charging && onlyWhenCharging) {
					LOG.info("Configured not to use battery");
					reasonsDisabled |= REASON_BATTERY;
				}
				if (!useMobile && !wifi) {
					LOG.info("Configured not to use mobile data");
					reasonsDisabled |= REASON_MOBILE_DATA;
				}

				if (reasonsDisabled != 0) {
					LOG.info("Disabling network due to settings");
				} else {
					LOG.info("Enabling network");
					enableNetwork = true;
					if (network == PREF_TOR_NETWORK_WITH_BRIDGES ||
							(automatic && bridgesByDefault)) {
						if (ipv6Only) {
							bridgeTypes = asList(MEEK, SNOWFLAKE);
						} else {
							bridgeTypes = circumventionProvider
									.getSuitableBridgeTypes(country);
						}
						if (LOG.isLoggable(INFO)) {
							LOG.info("Using bridge types " + bridgeTypes);
						}
					} else {
						LOG.info("Not using bridges");
					}
					if (wifi && charging) {
						LOG.info("Enabling connection padding");
						enableConnectionPadding = true;
					} else {
						LOG.info("Disabling connection padding");
					}
				}
			}

			state.setReasonsDisabled(reasonsDisabled);

			try {
				if (enableNetwork) {
					enableBridges(bridgeTypes, country);
					tor.enableConnectionPadding(enableConnectionPadding);
					tor.enableIpv6(ipv6Only);
				}
				tor.enableNetwork(enableNetwork);
			} catch (IOException e) {
				logException(LOG, WARNING, e);
			}
		});
	}

	@ThreadSafe
	@NotNullByDefault
	private class PluginState {

		@GuardedBy("this")
		private boolean settingsChecked = false;

		@GuardedBy("this")
		private int reasonsDisabled = 0;

		@GuardedBy("this")
		@Nullable
		private ServerSocket serverSocket = null;

		@Nullable
		private synchronized ServerSocket setStopped() {
			ServerSocket ss = serverSocket;
			serverSocket = null;
			return ss;
		}

		private synchronized void setReasonsDisabled(int reasons) {
			boolean wasChecked = settingsChecked;
			settingsChecked = true;
			int oldReasons = reasonsDisabled;
			reasonsDisabled = reasons;
			if (!wasChecked || reasons != oldReasons) {
				callback.pluginStateChanged(getState());
			}
		}

		// Doesn't affect getState()
		private synchronized boolean setServerSocket(ServerSocket ss) {
			if (serverSocket != null || !tor.isTorRunning()) return false;
			serverSocket = ss;
			return true;
		}

		// Doesn't affect getState()
		private synchronized void clearServerSocket(ServerSocket ss) {
			if (serverSocket == ss) serverSocket = null;
		}

		private synchronized State getState() {
			return getState(tor.getTorState());
		}

		private synchronized State getState(TorState torState) {
			// Treat TorState.STARTED as State.STARTING_STOPPING because it's
			// only seen during startup, before TorWrapper#enableNetwork() is
			// called for the first time. TorState.NOT_STARTED and
			// TorState.STOPPED are mapped to State.STARTING_STOPPING because
			// that's the State before we've started and after we've stopped.
			if (torState == TorState.NOT_STARTED ||
					torState == TorState.STARTING ||
					torState == TorState.STARTED ||
					torState == TorState.STOPPING ||
					torState == TorState.STOPPED ||
					!settingsChecked) {
				return STARTING_STOPPING;
			}
			if (reasonsDisabled != 0) return DISABLED;
			if (torState == TorState.CONNECTING) return ENABLING;
			if (torState == TorState.CONNECTED) return ACTIVE;
			// The plugin is enabled in settings but the device is offline
			return INACTIVE;
		}

		private synchronized int getReasonsDisabled() {
			return getState() == DISABLED ? reasonsDisabled : 0;
		}
	}
}
