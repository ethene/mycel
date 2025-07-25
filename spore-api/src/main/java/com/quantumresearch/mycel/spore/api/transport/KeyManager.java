package com.quantumresearch.mycel.spore.api.transport;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;

import java.security.GeneralSecurityException;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Responsible for managing transport keys and recognising the pseudo-random
 * tags of incoming streams.
 */
public interface KeyManager {

	/**
	 * Derives and stores a set of rotation mode transport keys for
	 * communicating with the given contact over the given transport and
	 * returns the key set ID, or null if the transport is not supported.
	 * <p/>
	 * {@link StreamContext StreamContexts} for the contact can be created
	 * after this method has returned.
	 *
	 * @param alice True if the local party is Alice
	 * @param active Whether the derived keys can be used for outgoing streams
	 */
	@Nullable
	KeySetId addRotationKeys(Transaction txn, ContactId c, TransportId t,
			SecretKey rootKey, long timestamp, boolean alice,
			boolean active) throws DbException;

	/**
	 * Derives and stores a set of rotation mode transport keys for
	 * communicating with the given contact over each supported transport and
	 * returns the key set IDs.
	 * <p/>
	 * {@link StreamContext StreamContexts} for the contact can be created
	 * after this method has returned.
	 *
	 * @param alice True if the local party is Alice
	 * @param active Whether the derived keys can be used for outgoing streams
	 */
	Map<TransportId, KeySetId> addRotationKeys(Transaction txn,
			ContactId c, SecretKey rootKey, long timestamp, boolean alice,
			boolean active) throws DbException;

	/**
	 * Informs the key manager that a new contact has been added. Derives and
	 * stores a set of handshake mode transport keys for communicating with the
	 * contact over each transport and returns the key set IDs.
	 * <p/>
	 * {@link StreamContext StreamContexts} for the contact can be created
	 * after this method has returned.
	 */
	Map<TransportId, KeySetId> addContact(Transaction txn, ContactId c,
			PublicKey theirPublicKey, KeyPair ourKeyPair)
			throws DbException, GeneralSecurityException;

	/**
	 * Informs the key manager that a new pending contact has been added.
	 * Derives and stores a set of handshake mode transport keys for
	 * communicating with the pending contact over each transport and returns
	 * the key set IDs.
	 * <p/>
	 * {@link StreamContext StreamContexts} for the pending contact can be
	 * created after this method has returned.
	 */
	Map<TransportId, KeySetId> addPendingContact(Transaction txn,
			PendingContactId p, PublicKey theirPublicKey, KeyPair ourKeyPair)
			throws DbException, GeneralSecurityException;

	/**
	 * Marks the given transport keys as usable for outgoing streams.
	 */
	void activateKeys(Transaction txn, Map<TransportId, KeySetId> keys)
			throws DbException;

	/**
	 * Returns true if we have keys that can be used for outgoing streams to
	 * the given contact over the given transport.
	 */
	boolean canSendOutgoingStreams(ContactId c, TransportId t);

	/**
	 * Returns true if we have keys that can be used for outgoing streams to
	 * the given pending contact over the given transport.
	 */
	boolean canSendOutgoingStreams(PendingContactId p, TransportId t);

	/**
	 * Returns a {@link StreamContext} for sending a stream to the given
	 * contact over the given transport, or null if an error occurs.
	 */
	@Nullable
	StreamContext getStreamContext(ContactId c, TransportId t)
			throws DbException;

	/**
	 * Returns a {@link StreamContext} for sending a stream to the given
	 * pending contact over the given transport, or null if an error occurs.
	 */
	@Nullable
	StreamContext getStreamContext(PendingContactId p, TransportId t)
			throws DbException;

	/**
	 * Looks up the given tag and returns a {@link StreamContext} for reading
	 * from the corresponding stream, or null if an error occurs or the tag was
	 * unexpected. Marks the tag as recognised and updates the reordering
	 * window.
	 */
	@Nullable
	StreamContext getStreamContext(TransportId t, byte[] tag)
			throws DbException;

	/**
	 * Looks up the given tag and returns a {@link StreamContext} for reading
	 * from the corresponding stream, or null if an error occurs or the tag was
	 * unexpected. Only returns the StreamContext; does not mark the tag as
	 * recognised.
	 */
	@Nullable
	StreamContext getStreamContextOnly(TransportId t, byte[] tag)
			throws DbException;

	/**
	 * Marks the tag as recognised and updates the reordering window.
	 */
	void markTagAsRecognised(TransportId t, byte[] tag) throws DbException;
}
