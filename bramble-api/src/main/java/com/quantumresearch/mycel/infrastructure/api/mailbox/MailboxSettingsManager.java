package com.quantumresearch.mycel.infrastructure.api.mailbox;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.List;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MailboxSettingsManager {

	/**
	 * Registers a hook to be called when a mailbox has been paired or unpaired.
	 * This method should be called before
	 * {@link LifecycleManager#startServices(SecretKey)}.
	 */
	void registerMailboxHook(MailboxHook hook);

	@Nullable
	MailboxProperties getOwnMailboxProperties(Transaction txn)
			throws DbException;

	void setOwnMailboxProperties(Transaction txn, MailboxProperties p)
			throws DbException;

	void removeOwnMailboxProperties(Transaction txn) throws DbException;

	MailboxStatus getOwnMailboxStatus(Transaction txn) throws DbException;

	void recordSuccessfulConnection(Transaction txn, long now,
			List<MailboxVersion> versions) throws DbException;

	void recordFailedConnectionAttempt(Transaction txn, long now)
			throws DbException;

	void setPendingUpload(Transaction txn, ContactId id,
			@Nullable String filename) throws DbException;

	@Nullable
	String getPendingUpload(Transaction txn, ContactId id) throws DbException;

	interface MailboxHook {
		/**
		 * Called when Briar is paired with a mailbox.
		 *
		 * @param txn A read-write transaction
		 */
		void mailboxPaired(Transaction txn, MailboxProperties p)
				throws DbException;

		/**
		 * Called when the mailbox is unpaired.
		 *
		 * @param txn A read-write transaction
		 */
		void mailboxUnpaired(Transaction txn) throws DbException;

		/**
		 * Called when we receive our mailbox's server-supported API versions.
		 * This happens whenever we successfully check the connectivity of
		 * our mailbox, so this hook may be called frequently.
		 *
		 * @param txn A read-write transaction
		 */
		void serverSupportedVersionsReceived(Transaction txn,
				List<MailboxVersion> serverSupports) throws DbException;
	}
}
