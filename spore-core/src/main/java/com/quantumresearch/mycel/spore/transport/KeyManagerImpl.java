package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.contact.event.ContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DatabaseExecutor;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventExecutor;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.lifecycle.Service;
import com.quantumresearch.mycel.spore.api.lifecycle.ServiceException;
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig;
import com.quantumresearch.mycel.spore.api.plugin.PluginFactory;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.transport.KeyManager;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import com.quantumresearch.mycel.spore.api.transport.StreamContext;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.logging.Level.INFO;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_TRANSPORT_LATENCY;

@ThreadSafe
@NotNullByDefault
class KeyManagerImpl implements KeyManager, Service, EventListener {

	private static final Logger LOG =
			Logger.getLogger(KeyManagerImpl.class.getName());

	private final DatabaseComponent db;
	private final Executor dbExecutor;
	private final PluginConfig pluginConfig;
	private final TransportCrypto transportCrypto;

	private final ConcurrentHashMap<TransportId, TransportKeyManager> managers;
	private final AtomicBoolean used = new AtomicBoolean(false);

	@Inject
	KeyManagerImpl(DatabaseComponent db,
			@DatabaseExecutor Executor dbExecutor,
			PluginConfig pluginConfig,
			TransportCrypto transportCrypto,
			TransportKeyManagerFactory transportKeyManagerFactory) {
		this.db = db;
		this.dbExecutor = dbExecutor;
		this.pluginConfig = pluginConfig;
		this.transportCrypto = transportCrypto;
		managers = new ConcurrentHashMap<>();
		for (PluginFactory<?> f : pluginConfig.getSimplexFactories()) {
			TransportKeyManager m = transportKeyManagerFactory.
					createTransportKeyManager(f.getId(), f.getMaxLatency());
			managers.put(f.getId(), m);
		}
		for (PluginFactory<?> f : pluginConfig.getDuplexFactories()) {
			TransportKeyManager m = transportKeyManagerFactory.
					createTransportKeyManager(f.getId(), f.getMaxLatency());
			managers.put(f.getId(), m);
		}
	}

	@Override
	public void startService() throws ServiceException {
		if (used.getAndSet(true)) throw new IllegalStateException();
		try {
			db.transaction(false, txn -> {
				for (PluginFactory<?> f : pluginConfig.getSimplexFactories()) {
					addTransport(txn, f);
				}
				for (PluginFactory<?> f : pluginConfig.getDuplexFactories()) {
					addTransport(txn, f);
				}
			});
		} catch (DbException e) {
			throw new ServiceException(e);
		}
	}

	private void addTransport(Transaction txn, PluginFactory<?> f)
			throws DbException {
		long maxLatency = f.getMaxLatency();
		if (maxLatency > MAX_TRANSPORT_LATENCY) {
			throw new IllegalStateException();
		}
		db.addTransport(txn, f.getId(), maxLatency);
		managers.get(f.getId()).start(txn);
	}

	@Override
	public void stopService() {
	}

	@Override
	public KeySetId addRotationKeys(Transaction txn, ContactId c,
			TransportId t, SecretKey rootKey, long timestamp, boolean alice,
			boolean active) throws DbException {
		return withManager(t, m ->
				m.addRotationKeys(txn, c, rootKey, timestamp, alice, active));
	}

	@Override
	public Map<TransportId, KeySetId> addRotationKeys(Transaction txn,
			ContactId c, SecretKey rootKey, long timestamp, boolean alice,
			boolean active) throws DbException {
		Map<TransportId, KeySetId> ids = new HashMap<>();
		for (Entry<TransportId, TransportKeyManager> e : managers.entrySet()) {
			TransportId t = e.getKey();
			TransportKeyManager m = e.getValue();
			ids.put(t, m.addRotationKeys(txn, c, rootKey, timestamp,
					alice, active));
		}
		return ids;
	}

	@Override
	public Map<TransportId, KeySetId> addContact(Transaction txn, ContactId c,
			PublicKey theirPublicKey, KeyPair ourKeyPair)
			throws DbException, GeneralSecurityException {
		SecretKey staticMasterKey = transportCrypto
				.deriveStaticMasterKey(theirPublicKey, ourKeyPair);
		SecretKey rootKey =
				transportCrypto.deriveHandshakeRootKey(staticMasterKey, false);
		boolean alice = transportCrypto.isAlice(theirPublicKey, ourKeyPair);
		Map<TransportId, KeySetId> ids = new HashMap<>();
		for (Entry<TransportId, TransportKeyManager> e : managers.entrySet()) {
			TransportId t = e.getKey();
			TransportKeyManager m = e.getValue();
			ids.put(t, m.addHandshakeKeys(txn, c, rootKey, alice));
		}
		return ids;
	}

	@Override
	public Map<TransportId, KeySetId> addPendingContact(Transaction txn,
			PendingContactId p, PublicKey theirPublicKey, KeyPair ourKeyPair)
			throws DbException, GeneralSecurityException {
		SecretKey staticMasterKey = transportCrypto
				.deriveStaticMasterKey(theirPublicKey, ourKeyPair);
		SecretKey rootKey =
				transportCrypto.deriveHandshakeRootKey(staticMasterKey, true);
		boolean alice = transportCrypto.isAlice(theirPublicKey, ourKeyPair);
		Map<TransportId, KeySetId> ids = new HashMap<>();
		for (Entry<TransportId, TransportKeyManager> e : managers.entrySet()) {
			TransportId t = e.getKey();
			TransportKeyManager m = e.getValue();
			ids.put(t, m.addHandshakeKeys(txn, p, rootKey, alice));
		}
		return ids;
	}

	@Override
	public void activateKeys(Transaction txn, Map<TransportId, KeySetId> keys)
			throws DbException {
		for (Entry<TransportId, KeySetId> e : keys.entrySet()) {
			withManager(e.getKey(), m -> {
				m.activateKeys(txn, e.getValue());
				return null;
			});
		}
	}

	@Override
	public boolean canSendOutgoingStreams(ContactId c, TransportId t) {
		TransportKeyManager m = managers.get(t);
		return m != null && m.canSendOutgoingStreams(c);
	}

	@Override
	public boolean canSendOutgoingStreams(PendingContactId p, TransportId t) {
		TransportKeyManager m = managers.get(t);
		return m != null && m.canSendOutgoingStreams(p);
	}

	@Override
	public StreamContext getStreamContext(ContactId c, TransportId t)
			throws DbException {
		return withManager(t, m ->
				db.transactionWithNullableResult(false, txn ->
						m.getStreamContext(txn, c)));
	}

	@Override
	public StreamContext getStreamContext(PendingContactId p, TransportId t)
			throws DbException {
		return withManager(t, m ->
				db.transactionWithNullableResult(false, txn ->
						m.getStreamContext(txn, p)));
	}

	@Override
	public StreamContext getStreamContext(TransportId t, byte[] tag)
			throws DbException {
		return withManager(t, m ->
				db.transactionWithNullableResult(false, txn ->
						m.getStreamContext(txn, tag)));
	}

	@Override
	public StreamContext getStreamContextOnly(TransportId t, byte[] tag)
			throws DbException {
		return withManager(t, m ->
				db.transactionWithNullableResult(false, txn ->
						m.getStreamContextOnly(txn, tag)));
	}

	@Override
	public void markTagAsRecognised(TransportId t, byte[] tag)
			throws DbException {
		withManager(t, m -> {
			db.transaction(false, txn -> m.markTagAsRecognised(txn, tag));
			return null;
		});
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			removeContact(((ContactRemovedEvent) e).getContactId());
		} else if (e instanceof PendingContactRemovedEvent) {
			PendingContactRemovedEvent p = (PendingContactRemovedEvent) e;
			removePendingContact(p.getId());
		}
	}

	@EventExecutor
	private void removeContact(ContactId c) {
		dbExecutor.execute(() -> {
			for (TransportKeyManager m : managers.values()) m.removeContact(c);
		});
	}

	@EventExecutor
	private void removePendingContact(PendingContactId p) {
		dbExecutor.execute(() -> {
			for (TransportKeyManager m : managers.values())
				m.removePendingContact(p);
		});
	}

	@Nullable
	private <T> T withManager(TransportId t, ManagerTask<T> task)
			throws DbException {
		TransportKeyManager m = managers.get(t);
		if (m == null) {
			if (LOG.isLoggable(INFO)) LOG.info("No key manager for " + t);
			return null;
		}
		return task.run(m);
	}

	private interface ManagerTask<T> {
		@Nullable
		T run(TransportKeyManager m) throws DbException;
	}
}
