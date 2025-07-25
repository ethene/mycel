package com.quantumresearch.mycel.spore.plugin;

import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.Service;
import com.quantumresearch.mycel.spore.api.lifecycle.ServiceException;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.Plugin.State;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig;
import com.quantumresearch.mycel.spore.api.plugin.PluginException;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportStateEvent;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import com.quantumresearch.mycel.spore.api.settings.SettingsManager;
import com.quantumresearch.mycel.spore.api.system.WakefulIoExecutor;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.PREF_PLUGIN_ENABLE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.ACTIVE;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.DISABLED;
import static com.quantumresearch.mycel.spore.api.plugin.Plugin.State.STARTING_STOPPING;
import static com.quantumresearch.mycel.spore.util.LogUtils.logDuration;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;

@ThreadSafe
@NotNullByDefault
class PluginManagerImpl implements PluginManager, Service {

	private static final Logger LOG =
			getLogger(PluginManagerImpl.class.getName());

	private final Executor ioExecutor, wakefulIoExecutor;
	private final EventBus eventBus;
	private final PluginConfig pluginConfig;
	private final ConnectionManager connectionManager;
	private final SettingsManager settingsManager;
	private final TransportPropertyManager transportPropertyManager;
	private final Map<TransportId, Plugin> plugins;
	private final List<SimplexPlugin> simplexPlugins;
	private final List<DuplexPlugin> duplexPlugins;
	private final Map<TransportId, CountDownLatch> startLatches;
	private final AtomicBoolean used = new AtomicBoolean(false);

	@Inject
	PluginManagerImpl(@IoExecutor Executor ioExecutor,
			@WakefulIoExecutor Executor wakefulIoExecutor,
			EventBus eventBus,
			PluginConfig pluginConfig,
			ConnectionManager connectionManager,
			SettingsManager settingsManager,
			TransportPropertyManager transportPropertyManager) {
		this.ioExecutor = ioExecutor;
		this.wakefulIoExecutor = wakefulIoExecutor;
		this.eventBus = eventBus;
		this.pluginConfig = pluginConfig;
		this.connectionManager = connectionManager;
		this.settingsManager = settingsManager;
		this.transportPropertyManager = transportPropertyManager;
		plugins = new ConcurrentHashMap<>();
		simplexPlugins = new CopyOnWriteArrayList<>();
		duplexPlugins = new CopyOnWriteArrayList<>();
		startLatches = new ConcurrentHashMap<>();
	}

	@Override
	public void startService() {
		if (used.getAndSet(true)) throw new IllegalStateException();
		// Instantiate the simplex plugins and start them asynchronously
		LOG.info("Starting simplex plugins");
		for (SimplexPluginFactory f : pluginConfig.getSimplexFactories()) {
			TransportId t = f.getId();
			SimplexPlugin s = f.createPlugin(new Callback(t));
			if (s == null) {
				if (LOG.isLoggable(WARNING))
					LOG.warning("Could not create plugin for " + t);
			} else {
				plugins.put(t, s);
				simplexPlugins.add(s);
				CountDownLatch startLatch = new CountDownLatch(1);
				startLatches.put(t, startLatch);
				wakefulIoExecutor.execute(new PluginStarter(s, startLatch));
			}
		}
		// Instantiate the duplex plugins and start them asynchronously
		LOG.info("Starting duplex plugins");
		for (DuplexPluginFactory f : pluginConfig.getDuplexFactories()) {
			TransportId t = f.getId();
			DuplexPlugin d = f.createPlugin(new Callback(t));
			if (d == null) {
				if (LOG.isLoggable(WARNING))
					LOG.warning("Could not create plugin for " + t);
			} else {
				plugins.put(t, d);
				duplexPlugins.add(d);
				CountDownLatch startLatch = new CountDownLatch(1);
				startLatches.put(t, startLatch);
				wakefulIoExecutor.execute(new PluginStarter(d, startLatch));
			}
		}
	}

	@Override
	public void stopService() throws ServiceException {
		CountDownLatch stopLatch = new CountDownLatch(plugins.size());
		// Stop the simplex plugins
		LOG.info("Stopping simplex plugins");
		for (SimplexPlugin s : simplexPlugins) {
			CountDownLatch startLatch = startLatches.get(s.getId());
			// Don't need the wakeful executor here as we wait for the plugin
			// to stop before returning
			ioExecutor.execute(new PluginStopper(s, startLatch, stopLatch));
		}
		// Stop the duplex plugins
		LOG.info("Stopping duplex plugins");
		for (DuplexPlugin d : duplexPlugins) {
			CountDownLatch startLatch = startLatches.get(d.getId());
			// Don't need the wakeful executor here as we wait for the plugin
			// to stop before returning
			ioExecutor.execute(new PluginStopper(d, startLatch, stopLatch));
		}
		// Wait for all the plugins to stop
		try {
			LOG.info("Waiting for all the plugins to stop");
			stopLatch.await();
		} catch (InterruptedException e) {
			throw new ServiceException(e);
		}
	}

	@Override
	public Plugin getPlugin(TransportId t) {
		return plugins.get(t);
	}

	@Override
	public Collection<SimplexPlugin> getSimplexPlugins() {
		return new ArrayList<>(simplexPlugins);
	}

	@Override
	public Collection<DuplexPlugin> getDuplexPlugins() {
		return new ArrayList<>(duplexPlugins);
	}

	@Override
	public Collection<DuplexPlugin> getKeyAgreementPlugins() {
		List<DuplexPlugin> supported = new ArrayList<>();
		for (DuplexPlugin d : duplexPlugins)
			if (d.supportsKeyAgreement()) supported.add(d);
		return supported;
	}

	@Override
	public Collection<DuplexPlugin> getRendezvousPlugins() {
		List<DuplexPlugin> supported = new ArrayList<>();
		for (DuplexPlugin d : duplexPlugins)
			if (d.supportsRendezvous()) supported.add(d);
		return supported;
	}

	@Override
	public void setPluginEnabled(TransportId t, boolean enabled) {
		Plugin plugin = plugins.get(t);
		if (plugin == null) return;

		Settings s = new Settings();
		s.putBoolean(PREF_PLUGIN_ENABLE, enabled);
		ioExecutor.execute(() -> mergeSettings(s, t.getString()));
	}

	private void mergeSettings(Settings s, String namespace) {
		try {
			long start = now();
			settingsManager.mergeSettings(s, namespace);
			logDuration(LOG, "Merging settings", start);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
		}
	}

	private static class PluginStarter implements Runnable {

		private final Plugin plugin;
		private final CountDownLatch startLatch;

		private PluginStarter(Plugin plugin, CountDownLatch startLatch) {
			this.plugin = plugin;
			this.startLatch = startLatch;
		}

		@Override
		public void run() {
			try {
				long start = now();
				plugin.start();
				if (LOG.isLoggable(FINE)) {
					logDuration(LOG, "Starting plugin " + plugin.getId(),
							start);
				}
			} catch (PluginException e) {
				if (LOG.isLoggable(WARNING)) {
					LOG.warning("Plugin " + plugin.getId() + " did not start");
					logException(LOG, WARNING, e);
				}
			} finally {
				startLatch.countDown();
			}
		}
	}

	private static class PluginStopper implements Runnable {

		private final Plugin plugin;
		private final CountDownLatch startLatch, stopLatch;

		private PluginStopper(Plugin plugin, CountDownLatch startLatch,
				CountDownLatch stopLatch) {
			this.plugin = plugin;
			this.startLatch = startLatch;
			this.stopLatch = stopLatch;
		}

		@Override
		public void run() {
			if (LOG.isLoggable(INFO))
				LOG.info("Trying to stop plugin " + plugin.getId());
			try {
				// Wait for the plugin to finish starting
				startLatch.await();
				// Stop the plugin
				long start = now();
				plugin.stop();
				if (LOG.isLoggable(FINE)) {
					logDuration(LOG, "Stopping plugin " + plugin.getId(),
							start);
				}
			} catch (InterruptedException e) {
				LOG.warning("Interrupted while waiting for plugin to stop");
				// This task runs on an executor, so don't reset the interrupt
			} catch (PluginException e) {
				if (LOG.isLoggable(WARNING)) {
					LOG.warning("Plugin " + plugin.getId() + " did not stop");
					logException(LOG, WARNING, e);
				}
			} finally {
				stopLatch.countDown();
			}
		}
	}

	private class Callback implements PluginCallback {

		private final TransportId id;
		private final Object stateLock = new Object();

		@GuardedBy("lock")
		private State state = STARTING_STOPPING;

		private Callback(TransportId id) {
			this.id = id;
		}

		@Override
		public Settings getSettings() {
			try {
				return settingsManager.getSettings(id.getString());
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				return new Settings();
			}
		}

		@Override
		public TransportProperties getLocalProperties() {
			try {
				return transportPropertyManager.getLocalProperties(id);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				return new TransportProperties();
			}
		}

		@Override
		public Collection<TransportProperties> getRemoteProperties() {
			try {
				Map<ContactId, TransportProperties> remote =
						transportPropertyManager.getRemoteProperties(id);
				return remote.values();
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				return emptyList();
			}
		}

		@Override
		public void mergeSettings(Settings s) {
			PluginManagerImpl.this.mergeSettings(s, id.getString());
		}

		@Override
		public void mergeLocalProperties(TransportProperties p) {
			try {
				transportPropertyManager.mergeLocalProperties(id, p);
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		}

		@Override
		public void pluginStateChanged(State newState) {
			synchronized (stateLock) {
				if (newState != state) {
					State oldState = state;
					state = newState;
					if (LOG.isLoggable(INFO)) {
						LOG.info(id + " changed from state " + oldState
								+ " to " + newState);
					}
					eventBus.broadcast(new TransportStateEvent(id, newState));
					if (newState == ACTIVE) {
						eventBus.broadcast(new TransportActiveEvent(id));
					} else if (oldState == ACTIVE) {
						eventBus.broadcast(new TransportInactiveEvent(id));
					}
				} else if (newState == DISABLED) {
					// Broadcast an event even though the state hasn't changed,
					// as the reasons for the plugin being disabled may have
					// changed
					eventBus.broadcast(new TransportStateEvent(id, newState));
				}
			}
		}

		@Override
		public void handleConnection(DuplexTransportConnection d) {
			connectionManager.manageIncomingConnection(id, d);
		}

		@Override
		public void handleReader(TransportConnectionReader r) {
			connectionManager.manageIncomingConnection(id, r);
		}

		@Override
		public void handleWriter(TransportConnectionWriter w) {
			// TODO: Support simplex plugins that write to incoming connections
			throw new UnsupportedOperationException();
		}
	}
}
