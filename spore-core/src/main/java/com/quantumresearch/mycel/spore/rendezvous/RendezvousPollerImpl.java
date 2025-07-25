package com.quantumresearch.mycel.spore.rendezvous;

import com.quantumresearch.mycel.spore.PoliteExecutor;
import com.quantumresearch.mycel.spore.api.Cancellable;
import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactState;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactAddedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactStateChangedEvent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.event.EventExecutor;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.Service;
import com.quantumresearch.mycel.spore.api.lifecycle.ServiceException;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPlugin;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportActiveEvent;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.rendezvous.KeyMaterialSource;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousEndpoint;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousPoller;
import com.quantumresearch.mycel.spore.api.rendezvous.event.RendezvousConnectionClosedEvent;
import com.quantumresearch.mycel.spore.api.rendezvous.event.RendezvousConnectionOpenedEvent;
import com.quantumresearch.mycel.spore.api.rendezvous.event.RendezvousPollEvent;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.system.TaskScheduler;
import com.quantumresearch.mycel.spore.api.system.Wakeful;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.contact.PendingContactState.ADDING_CONTACT;
import static com.quantumresearch.mycel.spore.api.contact.PendingContactState.FAILED;
import static com.quantumresearch.mycel.spore.api.contact.PendingContactState.OFFLINE;
import static com.quantumresearch.mycel.spore.api.contact.PendingContactState.WAITING_FOR_CONNECTION;
import static com.quantumresearch.mycel.spore.rendezvous.RendezvousConstants.POLLING_INTERVAL_MS;
import static com.quantumresearch.mycel.spore.rendezvous.RendezvousConstants.RENDEZVOUS_TIMEOUT_MS;
import static com.quantumresearch.mycel.spore.util.IoUtils.tryToClose;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;
import static org.briarproject.nullsafety.NullSafety.requireNull;

@NotNullByDefault
class RendezvousPollerImpl implements RendezvousPoller, Service, EventListener {

	private static final Logger LOG =
			getLogger(RendezvousPollerImpl.class.getName());

	private final TaskScheduler scheduler;
	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final TransportCrypto transportCrypto;
	private final RendezvousCrypto rendezvousCrypto;
	private final PluginManager pluginManager;
	private final ConnectionManager connectionManager;
	private final EventBus eventBus;
	private final Clock clock;

	private final AtomicBoolean used = new AtomicBoolean(false);
	private final Map<PendingContactId, Long> lastPollTimes =
			new ConcurrentHashMap<>();

	// Executor that runs one task at a time
	private final Executor worker;
	// The following fields are only accessed on the worker
	private final Map<TransportId, PluginState> pluginStates = new HashMap<>();
	private final Map<PendingContactId, CryptoState> cryptoStates =
			new HashMap<>();
	@Nullable
	private KeyPair handshakeKeyPair = null;
	@Nullable
	private Cancellable pollTask = null;

	@Inject
	RendezvousPollerImpl(@IoExecutor Executor ioExecutor,
			TaskScheduler scheduler,
			DatabaseComponent db,
			IdentityManager identityManager,
			TransportCrypto transportCrypto,
			RendezvousCrypto rendezvousCrypto,
			PluginManager pluginManager,
			ConnectionManager connectionManager,
			EventBus eventBus,
			Clock clock) {
		this.scheduler = scheduler;
		this.db = db;
		this.identityManager = identityManager;
		this.transportCrypto = transportCrypto;
		this.rendezvousCrypto = rendezvousCrypto;
		this.pluginManager = pluginManager;
		this.connectionManager = connectionManager;
		this.eventBus = eventBus;
		this.clock = clock;
		worker = new PoliteExecutor("RendezvousPoller", ioExecutor, 1);
	}

	@Override
	public long getLastPollTime(PendingContactId p) {
		Long time = lastPollTimes.get(p);
		return time == null ? 0 : time;
	}

	@Override
	public void startService() throws ServiceException {
		if (used.getAndSet(true)) throw new IllegalStateException();
		try {
			db.transaction(true, txn -> {
				Collection<PendingContact> pending = db.getPendingContacts(txn);
				// Use a commit action to prevent races with add/remove events
				txn.attach(() -> addPendingContactsAsync(pending));
			});
		} catch (DbException e) {
			throw new ServiceException(e);
		}
	}

	@EventExecutor
	private void addPendingContactsAsync(Collection<PendingContact> pending) {
		worker.execute(() -> {
			for (PendingContact p : pending) addPendingContact(p);
		});
	}

	// Worker
	private void addPendingContact(PendingContact p) {
		long now = clock.currentTimeMillis();
		long expiry = p.getTimestamp() + RENDEZVOUS_TIMEOUT_MS;
		if (expiry <= now) {
			broadcastState(p.getId(), FAILED);
			return;
		}
		try {
			if (handshakeKeyPair == null) {
				handshakeKeyPair = db.transactionWithResult(true,
						identityManager::getHandshakeKeys);
			}
			SecretKey staticMasterKey = transportCrypto
					.deriveStaticMasterKey(p.getPublicKey(), handshakeKeyPair);
			SecretKey rendezvousKey = rendezvousCrypto
					.deriveRendezvousKey(staticMasterKey);
			boolean alice = transportCrypto
					.isAlice(p.getPublicKey(), handshakeKeyPair);
			CryptoState cs = new CryptoState(rendezvousKey, alice, expiry);
			requireNull(cryptoStates.put(p.getId(), cs));
			for (PluginState ps : pluginStates.values()) {
				RendezvousEndpoint endpoint =
						createEndpoint(ps.plugin, p.getId(), cs);
				if (endpoint != null) {
					requireNull(ps.endpoints.put(p.getId(), endpoint));
					cs.numEndpoints++;
				}
			}
			if (cs.numEndpoints == 0) broadcastState(p.getId(), OFFLINE);
			else broadcastState(p.getId(), WAITING_FOR_CONNECTION);
			if (cryptoStates.size() == 1) {
				LOG.info("Starting poller");
				requireNull(pollTask);
				pollTask = scheduler.scheduleWithFixedDelay(this::poll, worker,
						POLLING_INTERVAL_MS, POLLING_INTERVAL_MS, MILLISECONDS);
			}
		} catch (DbException | GeneralSecurityException e) {
			logException(LOG, WARNING, e);
		}
	}

	private void broadcastState(PendingContactId p, PendingContactState state) {
		eventBus.broadcast(new PendingContactStateChangedEvent(p, state));
	}

	@Nullable
	private RendezvousEndpoint createEndpoint(DuplexPlugin plugin,
			PendingContactId p, CryptoState cs) {
		TransportId t = plugin.getId();
		KeyMaterialSource k =
				rendezvousCrypto.createKeyMaterialSource(cs.rendezvousKey, t);
		Handler h = new Handler(p, t, true);
		return plugin.createRendezvousEndpoint(k, cs.alice, h);
	}

	// Worker
	@Wakeful
	private void poll() {
		LOG.info("Polling");
		removeExpiredPendingContacts();
		for (PluginState ps : pluginStates.values()) poll(ps);
	}

	// Worker
	private void removeExpiredPendingContacts() {
		long now = clock.currentTimeMillis();
		List<PendingContactId> expired = new ArrayList<>();
		for (Entry<PendingContactId, CryptoState> e : cryptoStates.entrySet()) {
			if (e.getValue().expiry <= now) expired.add(e.getKey());
		}
		for (PendingContactId p : expired) {
			removePendingContact(p);
			broadcastState(p, FAILED);
		}
	}

	// Worker
	private void removePendingContact(PendingContactId p) {
		// We can come here twice if a pending contact expires and is removed
		if (cryptoStates.remove(p) == null) return;
		lastPollTimes.remove(p);
		for (PluginState ps : pluginStates.values()) {
			RendezvousEndpoint endpoint = ps.endpoints.remove(p);
			if (endpoint != null) tryToClose(endpoint, LOG, INFO);
		}
		if (cryptoStates.isEmpty()) {
			LOG.info("Stopping poller");
			requireNonNull(pollTask).cancel();
			pollTask = null;
		}
	}

	// Worker
	@Wakeful
	private void poll(PluginState ps) {
		if (ps.endpoints.isEmpty()) return;
		TransportId t = ps.plugin.getId();
		List<Pair<TransportProperties, ConnectionHandler>> properties =
				new ArrayList<>();
		for (Entry<PendingContactId, RendezvousEndpoint> e :
				ps.endpoints.entrySet()) {
			TransportProperties props =
					e.getValue().getRemoteTransportProperties();
			Handler h = new Handler(e.getKey(), t, false);
			properties.add(new Pair<>(props, h));
		}
		List<PendingContactId> polled = new ArrayList<>(ps.endpoints.keySet());
		long now = clock.currentTimeMillis();
		for (PendingContactId p : polled) lastPollTimes.put(p, now);
		eventBus.broadcast(new RendezvousPollEvent(t, polled));
		ps.plugin.poll(properties);
	}

	@Override
	public void stopService() {
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof PendingContactAddedEvent) {
			PendingContactAddedEvent p = (PendingContactAddedEvent) e;
			addPendingContactAsync(p.getPendingContact());
		} else if (e instanceof PendingContactRemovedEvent) {
			PendingContactRemovedEvent p = (PendingContactRemovedEvent) e;
			removePendingContactAsync(p.getId());
		} else if (e instanceof TransportActiveEvent) {
			TransportActiveEvent t = (TransportActiveEvent) e;
			addTransportAsync(t.getTransportId());
		} else if (e instanceof TransportInactiveEvent) {
			TransportInactiveEvent t = (TransportInactiveEvent) e;
			removeTransportAsync(t.getTransportId());
		} else if (e instanceof RendezvousConnectionOpenedEvent) {
			RendezvousConnectionOpenedEvent r =
					(RendezvousConnectionOpenedEvent) e;
			connectionOpenedAsync(r.getPendingContactId());
		} else if (e instanceof RendezvousConnectionClosedEvent) {
			RendezvousConnectionClosedEvent r =
					(RendezvousConnectionClosedEvent) e;
			if (!r.isSuccess()) connectionFailedAsync(r.getPendingContactId());
		}
	}

	@EventExecutor
	private void addPendingContactAsync(PendingContact p) {
		worker.execute(() -> {
			addPendingContact(p);
			poll(p.getId());
		});
	}

	// Worker
	private void poll(PendingContactId p) {
		for (PluginState ps : pluginStates.values()) {
			RendezvousEndpoint endpoint = ps.endpoints.get(p);
			if (endpoint != null) {
				TransportId t = ps.plugin.getId();
				TransportProperties props =
						endpoint.getRemoteTransportProperties();
				Handler h = new Handler(p, t, false);
				lastPollTimes.put(p, clock.currentTimeMillis());
				eventBus.broadcast(
						new RendezvousPollEvent(t, singletonList(p)));
				ps.plugin.poll(singletonList(new Pair<>(props, h)));
			}
		}
	}

	@EventExecutor
	private void removePendingContactAsync(PendingContactId p) {
		worker.execute(() -> removePendingContact(p));
	}

	@EventExecutor
	private void addTransportAsync(TransportId t) {
		Plugin p = pluginManager.getPlugin(t);
		if (p instanceof DuplexPlugin) {
			DuplexPlugin d = (DuplexPlugin) p;
			if (d.supportsRendezvous())
				worker.execute(() -> addTransport(d));
		}
	}

	// Worker
	private void addTransport(DuplexPlugin plugin) {
		TransportId t = plugin.getId();
		Map<PendingContactId, RendezvousEndpoint> endpoints = new HashMap<>();
		for (Entry<PendingContactId, CryptoState> e : cryptoStates.entrySet()) {
			PendingContactId p = e.getKey();
			CryptoState cs = e.getValue();
			RendezvousEndpoint endpoint = createEndpoint(plugin, p, cs);
			if (endpoint != null) {
				endpoints.put(p, endpoint);
				if (++cs.numEndpoints == 1)
					broadcastState(p, WAITING_FOR_CONNECTION);
			}
		}
		requireNull(pluginStates.put(t, new PluginState(plugin, endpoints)));
	}

	@EventExecutor
	private void removeTransportAsync(TransportId t) {
		worker.execute(() -> removeTransport(t));
	}

	// Worker
	private void removeTransport(TransportId t) {
		PluginState ps = pluginStates.remove(t);
		if (ps != null) {
			for (Entry<PendingContactId, RendezvousEndpoint> e :
					ps.endpoints.entrySet()) {
				tryToClose(e.getValue(), LOG, INFO);
				CryptoState cs = cryptoStates.get(e.getKey());
				if (--cs.numEndpoints == 0) broadcastState(e.getKey(), OFFLINE);
			}
		}
	}

	@EventExecutor
	private void connectionOpenedAsync(PendingContactId p) {
		worker.execute(() -> connectionOpened(p));
	}

	// Worker
	private void connectionOpened(PendingContactId p) {
		// Check that the pending contact hasn't expired
		if (cryptoStates.containsKey(p)) broadcastState(p, ADDING_CONTACT);
	}

	@EventExecutor
	private void connectionFailedAsync(PendingContactId p) {
		worker.execute(() -> connectionFailed(p));
	}

	// Worker
	private void connectionFailed(PendingContactId p) {
		// Check that the pending contact hasn't expired
		if (cryptoStates.containsKey(p))
			broadcastState(p, WAITING_FOR_CONNECTION);
	}

	private static class PluginState {

		private final DuplexPlugin plugin;
		private final Map<PendingContactId, RendezvousEndpoint> endpoints;

		private PluginState(DuplexPlugin plugin,
				Map<PendingContactId, RendezvousEndpoint> endpoints) {
			this.plugin = plugin;
			this.endpoints = endpoints;
		}
	}

	private static class CryptoState {

		private final SecretKey rendezvousKey;
		private final boolean alice;
		private final long expiry;

		private int numEndpoints = 0;

		private CryptoState(SecretKey rendezvousKey, boolean alice,
				long expiry) {
			this.rendezvousKey = rendezvousKey;
			this.alice = alice;
			this.expiry = expiry;
		}
	}

	private class Handler implements ConnectionHandler {

		private final PendingContactId pendingContactId;
		private final TransportId transportId;
		private final boolean incoming;

		private Handler(PendingContactId pendingContactId,
				TransportId transportId, boolean incoming) {
			this.pendingContactId = pendingContactId;
			this.transportId = transportId;
			this.incoming = incoming;
		}

		@Override
		public void handleConnection(DuplexTransportConnection c) {
			if (incoming) {
				connectionManager.manageIncomingConnection(pendingContactId,
						transportId, c);
			} else {
				connectionManager.manageOutgoingConnection(pendingContactId,
						transportId, c);
			}
		}

		@Override
		public void handleReader(TransportConnectionReader r) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void handleWriter(TransportConnectionWriter w) {
			throw new UnsupportedOperationException();
		}
	}
}
