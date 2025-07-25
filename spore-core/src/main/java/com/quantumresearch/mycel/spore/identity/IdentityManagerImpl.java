package com.quantumresearch.mycel.spore.identity;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.system.Clock;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.util.LogUtils.logDuration;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@ThreadSafe
@NotNullByDefault
class IdentityManagerImpl implements IdentityManager, OpenDatabaseHook {

	private static final Logger LOG =
			getLogger(IdentityManagerImpl.class.getName());

	private final DatabaseComponent db;
	private final CryptoComponent crypto;
	private final AuthorFactory authorFactory;
	private final Clock clock;

	/**
	 * The user's identity, or null if no identity has been registered or
	 * loaded. If non-null, this identity always has handshake keys.
	 */
	@Nullable
	private volatile Identity cachedIdentity = null;

	/**
	 * True if {@code cachedIdentity} was registered via
	 * {@link #registerIdentity(Identity)} and should be stored when
	 * {@link #onDatabaseOpened(Transaction)} is called.
	 */

	private volatile boolean shouldStoreIdentity = false;

	/**
	 * True if the handshake keys in {@code cachedIdentity} were generated
	 * when the identity was loaded and should be stored when
	 * {@link #onDatabaseOpened(Transaction)} is called.
	 */
	private volatile boolean shouldStoreKeys = false;

	@Inject
	IdentityManagerImpl(DatabaseComponent db, CryptoComponent crypto,
			AuthorFactory authorFactory, Clock clock) {
		this.db = db;
		this.crypto = crypto;
		this.authorFactory = authorFactory;
		this.clock = clock;
	}

	@Override
	public Identity createIdentity(String name) {
		long start = now();
		LocalAuthor localAuthor = authorFactory.createLocalAuthor(name);
		KeyPair handshakeKeyPair = crypto.generateAgreementKeyPair();
		PublicKey handshakePub = handshakeKeyPair.getPublic();
		PrivateKey handshakePriv = handshakeKeyPair.getPrivate();
		logDuration(LOG, "Creating identity", start);
		return new Identity(localAuthor, handshakePub, handshakePriv,
				clock.currentTimeMillis());
	}

	@Override
	public void registerIdentity(Identity i) {
		if (!i.hasHandshakeKeyPair()) throw new IllegalArgumentException();
		cachedIdentity = i;
		shouldStoreIdentity = true;
		LOG.info("Identity registered");
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		Identity cached = getCachedIdentity(txn);
		if (shouldStoreIdentity) {
			// The identity was registered at startup - store it
			db.addIdentity(txn, cached);
			LOG.info("Identity stored");
		} else if (shouldStoreKeys) {
			// Handshake keys were generated when loading the identity -
			// store them
			PublicKey handshakePub =
					requireNonNull(cached.getHandshakePublicKey());
			PrivateKey handshakePriv =
					requireNonNull(cached.getHandshakePrivateKey());
			db.setHandshakeKeyPair(txn, cached.getId(), handshakePub,
					handshakePriv);
			LOG.info("Handshake key pair stored");
		}
	}

	@Override
	public LocalAuthor getLocalAuthor() throws DbException {
		Identity cached = cachedIdentity;
		if (cached == null)
			cached = db.transactionWithResult(true, this::getCachedIdentity);
		return cached.getLocalAuthor();
	}

	@Override
	public LocalAuthor getLocalAuthor(Transaction txn) throws DbException {
		return getCachedIdentity(txn).getLocalAuthor();
	}

	@Override
	public KeyPair getHandshakeKeys(Transaction txn) throws DbException {
		Identity cached = getCachedIdentity(txn);
		PublicKey handshakePub = requireNonNull(cached.getHandshakePublicKey());
		PrivateKey handshakePriv =
				requireNonNull(cached.getHandshakePrivateKey());
		return new KeyPair(handshakePub, handshakePriv);
	}

	/**
	 * Loads the identity if necessary and returns it. If
	 * {@code cachedIdentity} was not already set by calling
	 * {@link #registerIdentity(Identity)}, this method sets it. If
	 * {@code cachedIdentity} was already set, either by calling
	 * {@link #registerIdentity(Identity)} or by a previous call to this
	 * method, then this method returns the cached identity without hitting
	 * the database.
	 */
	private Identity getCachedIdentity(Transaction txn) throws DbException {
		Identity cached = cachedIdentity;
		if (cached == null)
			cachedIdentity = cached = loadIdentityWithKeyPair(txn);
		return cached;
	}

	/**
	 * Loads and returns the identity, generating a handshake key pair if
	 * necessary and setting {@code shouldStoreKeys} if a handshake key pair
	 * was generated.
	 */
	private Identity loadIdentityWithKeyPair(Transaction txn)
			throws DbException {
		Collection<Identity> identities = db.getIdentities(txn);
		if (identities.size() != 1) throw new DbException();
		Identity i = identities.iterator().next();
		LOG.info("Identity loaded");
		if (i.hasHandshakeKeyPair()) return i;
		KeyPair handshakeKeyPair = crypto.generateAgreementKeyPair();
		PublicKey handshakePub = handshakeKeyPair.getPublic();
		PrivateKey handshakePriv = handshakeKeyPair.getPrivate();
		LOG.info("Handshake key pair generated");
		shouldStoreKeys = true;
		return new Identity(i.getLocalAuthor(), handshakePub, handshakePriv,
				i.getTimeCreated());
	}
}
