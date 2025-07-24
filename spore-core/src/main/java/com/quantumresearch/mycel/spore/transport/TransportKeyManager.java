package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import com.quantumresearch.mycel.spore.api.transport.StreamContext;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

@NotNullByDefault
interface TransportKeyManager {

	void start(Transaction txn) throws DbException;

	KeySetId addRotationKeys(Transaction txn, ContactId c,
			SecretKey rootKey, long timestamp, boolean alice, boolean active)
			throws DbException;

	KeySetId addHandshakeKeys(Transaction txn, ContactId c,
			SecretKey rootKey, boolean alice) throws DbException;

	KeySetId addHandshakeKeys(Transaction txn, PendingContactId p,
			SecretKey rootKey, boolean alice) throws DbException;

	void activateKeys(Transaction txn, KeySetId k) throws DbException;

	void removeContact(ContactId c);

	void removePendingContact(PendingContactId p);

	boolean canSendOutgoingStreams(ContactId c);

	boolean canSendOutgoingStreams(PendingContactId p);

	@Nullable
	StreamContext getStreamContext(Transaction txn, ContactId c)
			throws DbException;

	@Nullable
	StreamContext getStreamContext(Transaction txn, PendingContactId p)
			throws DbException;

	@Nullable
	StreamContext getStreamContext(Transaction txn, byte[] tag)
			throws DbException;

	@Nullable
	StreamContext getStreamContextOnly(Transaction txn, byte[] tag);

	void markTagAsRecognised(Transaction txn, byte[] tag) throws DbException;

}
