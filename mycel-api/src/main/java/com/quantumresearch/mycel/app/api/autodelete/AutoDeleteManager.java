package com.quantumresearch.mycel.app.api.autodelete;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import org.briarproject.nullsafety.NotNullByDefault;

import static java.util.concurrent.TimeUnit.DAYS;

@NotNullByDefault
public interface AutoDeleteManager {

	/**
	 * The unique ID of the auto-delete client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.autodelete");

	/**
	 * The current major version of the auto-delete client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the auto-delete client.
	 */
	int MINOR_VERSION = 0;

	/**
	 * The default auto-delete timer duration.
	 */
	long DEFAULT_TIMER_DURATION = DAYS.toMillis(7);

	/**
	 * Returns the auto-delete timer duration for the given contact. Use
	 * {@link #getAutoDeleteTimer(Transaction, ContactId, long)} if the timer
	 * will be used in an outgoing message.
	 */
	long getAutoDeleteTimer(Transaction txn, ContactId c) throws DbException;

	/**
	 * Returns the auto-delete timer duration for the given contact, for use in
	 * a message with the given timestamp. The timestamp is stored. This method
	 * requires a read-write transaction.
	 */
	long getAutoDeleteTimer(Transaction txn, ContactId c, long timestamp)
			throws DbException;

	/**
	 * Sets the auto-delete timer duration for the given contact.
	 */
	void setAutoDeleteTimer(Transaction txn, ContactId c, long timer)
			throws DbException;

	/**
	 * Receives an auto-delete timer duration from the given contact, carried
	 * in a message with the given timestamp. The local timer is set to the
	 * same duration unless it has been
	 * {@link #setAutoDeleteTimer(Transaction, ContactId, long) changed} more
	 * recently than the remote timer.
	 */
	void receiveAutoDeleteTimer(Transaction txn, ContactId c, long timer,
			long timestamp) throws DbException;
}
