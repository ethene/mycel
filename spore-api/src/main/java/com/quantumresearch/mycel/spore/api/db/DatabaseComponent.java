package com.quantumresearch.mycel.spore.api.db;

import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import com.quantumresearch.mycel.spore.api.sync.Ack;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.MessageStatus;
import com.quantumresearch.mycel.spore.api.sync.Offer;
import com.quantumresearch.mycel.spore.api.sync.Request;
import com.quantumresearch.mycel.spore.api.sync.validation.MessageState;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import com.quantumresearch.mycel.spore.api.transport.TransportKeySet;
import com.quantumresearch.mycel.spore.api.transport.TransportKeys;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Encapsulates the database implementation and exposes high-level operations
 * to other components.
 * <p>
 * With the exception of the {@link #open(SecretKey, MigrationListener)} and
 * {@link #close()} methods, which must not be called concurrently, the
 * database can be accessed from any thread. See {@link TransactionManager}
 * for locking behaviour.
 */
@ThreadSafe
@NotNullByDefault
public interface DatabaseComponent extends TransactionManager {

	/**
	 * Return value for {@link #getNextCleanupDeadline(Transaction)} if
	 * no messages are scheduled to be deleted.
	 */
	long NO_CLEANUP_DEADLINE = -1;

	/**
	 * Return value for {@link #startCleanupTimer(Transaction, MessageId)}
	 * if the cleanup timer was not started.
	 */
	long TIMER_NOT_STARTED = -1;

	/**
	 * Opens the database and returns true if the database already existed.
	 *
	 * @throws DataTooNewException if the data uses a newer schema than the
	 * current code
	 * @throws DataTooOldException if the data uses an older schema than the
	 * current code and cannot be migrated
	 */
	boolean open(SecretKey key, @Nullable MigrationListener listener)
			throws DbException;

	/**
	 * Waits for any open transactions to finish and closes the database.
	 */
	void close() throws DbException;

	/**
	 * Stores a contact associated with the given local and remote pseudonyms,
	 * and returns an ID for the contact.
	 */
	ContactId addContact(Transaction txn, Author remote, AuthorId local,
			@Nullable PublicKey handshake, boolean verified) throws DbException;

	/**
	 * Stores a group.
	 */
	void addGroup(Transaction txn, Group g) throws DbException;

	/**
	 * Stores an identity.
	 */
	void addIdentity(Transaction txn, Identity i) throws DbException;

	/**
	 * Stores a local message.
	 */
	void addLocalMessage(Transaction txn, Message m, Metadata meta,
			boolean shared, boolean temporary) throws DbException;

	/**
	 * Stores a pending contact.
	 */
	void addPendingContact(Transaction txn, PendingContact p, AuthorId local)
			throws DbException;

	/**
	 * Stores a transport.
	 */
	void addTransport(Transaction txn, TransportId t, long maxLatency)
			throws DbException;

	/**
	 * Stores the given transport keys for the given contact and returns a
	 * key set ID.
	 */
	KeySetId addTransportKeys(Transaction txn, ContactId c, TransportKeys k)
			throws DbException;

	/**
	 * Stores the given transport keys for the given pending contact and
	 * returns a key set ID.
	 */
	KeySetId addTransportKeys(Transaction txn, PendingContactId p,
			TransportKeys k) throws DbException;

	/**
	 * Returns true if there are any acks to send to the given contact.
	 * <p/>
	 * Read-only.
	 */
	boolean containsAcksToSend(Transaction txn, ContactId c) throws DbException;

	/**
	 * Returns true if the database contains the given contact for the given
	 * local pseudonym.
	 * <p/>
	 * Read-only.
	 */
	boolean containsContact(Transaction txn, AuthorId remote, AuthorId local)
			throws DbException;

	/**
	 * Returns true if the database contains the given group.
	 * <p/>
	 * Read-only.
	 */
	boolean containsGroup(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns true if the database contains an identity for the given
	 * pseudonym.
	 * <p/>
	 * Read-only.
	 */
	boolean containsIdentity(Transaction txn, AuthorId a) throws DbException;

	/**
	 * Returns true if there are any messages to send to the given contact
	 * over a transport with the given maximum latency.
	 * <p/>
	 * Read-only.
	 *
	 * @param eager True if messages that are not yet due for retransmission
	 * should be included
	 */
	boolean containsMessagesToSend(Transaction txn, ContactId c,
			long maxLatency, boolean eager) throws DbException;

	/**
	 * Returns true if the database contains the given pending contact.
	 * <p/>
	 * Read-only.
	 */
	boolean containsPendingContact(Transaction txn, PendingContactId p)
			throws DbException;

	/**
	 * Returns true if the database contains keys for communicating with the
	 * given contact over the given transport. Handshake mode and rotation mode
	 * keys are included, whether activated or not.
	 * <p/>
	 * Read-only.
	 */
	boolean containsTransportKeys(Transaction txn, ContactId c, TransportId t)
			throws DbException;

	/**
	 * Deletes the message with the given ID. Unlike
	 * {@link #removeMessage(Transaction, MessageId)}, the message ID,
	 * dependencies, metadata, and any other associated state are not deleted.
	 */
	void deleteMessage(Transaction txn, MessageId m) throws DbException;

	/**
	 * Deletes any metadata associated with the given message.
	 */
	void deleteMessageMetadata(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns an acknowledgement for the given contact, or null if there are
	 * no messages to acknowledge.
	 */
	@Nullable
	Ack generateAck(Transaction txn, ContactId c, int maxMessages)
			throws DbException;

	/**
	 * Returns a batch of messages for the given contact, for transmission over
	 * a transport with the given maximum latency. The total length of the
	 * messages, including record headers, will be no more than the given
	 * capacity. Returns null if there are no sendable messages that would fit
	 * in the given capacity.
	 */
	@Nullable
	Collection<Message> generateBatch(Transaction txn, ContactId c,
			long capacity, long maxLatency) throws DbException;

	/**
	 * Returns an offer for the given contact for transmission over a
	 * transport with the given maximum latency, or null if there are no
	 * messages to offer.
	 */
	@Nullable
	Offer generateOffer(Transaction txn, ContactId c, int maxMessages,
			long maxLatency) throws DbException;

	/**
	 * Returns a request for the given contact, or null if there are no
	 * messages to request.
	 */
	@Nullable
	Request generateRequest(Transaction txn, ContactId c, int maxMessages)
			throws DbException;

	/**
	 * Returns a batch of messages for the given contact, for transmission over
	 * a transport with the given maximum latency. Only messages that have been
	 * requested by the contact are returned. The total length of the messages,
	 * including record headers, will be no more than the given capacity.
	 * Returns null if there are no sendable messages that have been requested
	 * by the contact and would fit in the given capacity.
	 */
	@Nullable
	Collection<Message> generateRequestedBatch(Transaction txn, ContactId c,
			long capacity, long maxLatency) throws DbException;

	/**
	 * Returns the contact with the given ID.
	 * <p/>
	 * Read-only.
	 */
	Contact getContact(Transaction txn, ContactId c) throws DbException;

	/**
	 * Returns all contacts.
	 * <p/>
	 * Read-only.
	 */
	Collection<Contact> getContacts(Transaction txn) throws DbException;

	/**
	 * Returns a possibly empty collection of contacts with the given author ID.
	 * <p/>
	 * Read-only.
	 */
	Collection<Contact> getContactsByAuthorId(Transaction txn, AuthorId remote)
			throws DbException;

	/**
	 * Returns all contacts associated with the given local pseudonym.
	 * <p/>
	 * Read-only.
	 */
	Collection<ContactId> getContacts(Transaction txn, AuthorId local)
			throws DbException;

	/**
	 * Returns the group with the given ID.
	 * <p/>
	 * Read-only.
	 */
	Group getGroup(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns the ID of the group containing the given message.
	 * <p/>
	 * Read-only.
	 */
	GroupId getGroupId(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns the metadata for the given group.
	 * <p/>
	 * Read-only.
	 */
	Metadata getGroupMetadata(Transaction txn, GroupId g) throws DbException;

	/**
	 * Returns all groups belonging to the given client.
	 * <p/>
	 * Read-only.
	 */
	Collection<Group> getGroups(Transaction txn, ClientId c, int majorVersion)
			throws DbException;

	/**
	 * Returns the given group's visibility to the given contact, or
	 * {@link Visibility INVISIBLE} if the group is not in the database.
	 * <p/>
	 * Read-only.
	 */
	Visibility getGroupVisibility(Transaction txn, ContactId c, GroupId g)
			throws DbException;

	/**
	 * Returns the identity for the local pseudonym with the given ID.
	 * <p/>
	 * Read-only.
	 */
	Identity getIdentity(Transaction txn, AuthorId a) throws DbException;

	/**
	 * Returns the identities for all local pseudonyms.
	 * <p/>
	 * Read-only.
	 */
	Collection<Identity> getIdentities(Transaction txn) throws DbException;

	/**
	 * Returns the message with the given ID.
	 * <p/>
	 * Read-only.
	 *
	 * @throws MessageDeletedException if the message has been deleted
	 */
	Message getMessage(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns the IDs of all delivered messages in the given group.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessageIds(Transaction txn, GroupId g)
			throws DbException;

	/**
	 * Returns the IDs of any delivered messages in the given group with
	 * metadata that matches all entries in the given query. If the query is
	 * empty, the IDs of all delivered messages are returned.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessageIds(Transaction txn, GroupId g,
			Metadata query) throws DbException;

	/**
	 * Returns the IDs of all messages received from the given contact that
	 * need to be acknowledged.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessagesToAck(Transaction txn, ContactId c)
			throws DbException;

	/**
	 * Returns the IDs of some messages that are eligible to be sent to the
	 * given contact over a transport with the given maximum latency. The total
	 * length of the messages including record headers will be no more than the
	 * given capacity.
	 * <p/>
	 * Unlike {@link #getUnackedMessagesToSend(Transaction, ContactId)} this
	 * method does not return messages that have already been sent unless they
	 * are due for retransmission.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessagesToSend(Transaction txn, ContactId c,
			long capacity, long maxLatency) throws DbException;

	/**
	 * Returns the IDs of any messages that need to be validated.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessagesToValidate(Transaction txn)
			throws DbException;

	/**
	 * Returns the IDs of any messages that are pending delivery due to
	 * dependencies on other messages.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getPendingMessages(Transaction txn)
			throws DbException;

	/**
	 * Returns the IDs of any messages that have shared dependents but have
	 * not yet been shared themselves.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getMessagesToShare(Transaction txn)
			throws DbException;

	/**
	 * Returns the IDs of any messages of any messages that are due for
	 * deletion, along with their group IDs.
	 * <p/>
	 * Read-only.
	 */
	Map<GroupId, Collection<MessageId>> getMessagesToDelete(Transaction txn)
			throws DbException;

	/**
	 * Returns the metadata for all delivered messages in the given group.
	 * <p/>
	 * Read-only.
	 */
	Map<MessageId, Metadata> getMessageMetadata(Transaction txn, GroupId g)
			throws DbException;

	/**
	 * Returns the metadata for any delivered messages in the given group with
	 * metadata that matches all entries in the given query. If the query is
	 * empty, the metadata for all delivered messages is returned.
	 * <p/>
	 * Read-only.
	 */
	Map<MessageId, Metadata> getMessageMetadata(Transaction txn, GroupId g,
			Metadata query) throws DbException;

	/**
	 * Returns the metadata for the given delivered message.
	 * <p/>
	 * Read-only.
	 */
	Metadata getMessageMetadata(Transaction txn, MessageId m)
			throws DbException;

	/**
	 * Returns the metadata for the given delivered or pending message.
	 * This is only meant to be used by the ValidationManager.
	 * <p/>
	 * Read-only.
	 */
	Metadata getMessageMetadataForValidator(Transaction txn, MessageId m)
			throws DbException;

	/**
	 * Returns the status of all delivered messages in the given group with
	 * respect to the given contact.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageStatus> getMessageStatus(Transaction txn, ContactId c,
			GroupId g) throws DbException;

	/**
	 * Returns the IDs and states of all dependencies of the given message.
	 * For missing dependencies and dependencies in other groups, the state
	 * {@link MessageState UNKNOWN} is returned.
	 * <p/>
	 * Read-only.
	 */
	Map<MessageId, MessageState> getMessageDependencies(Transaction txn,
			MessageId m) throws DbException;

	/**
	 * Returns the IDs and states of all dependents of the given message.
	 * Dependents in other groups are not returned. If the given message is
	 * missing, no dependents are returned.
	 * <p/>
	 * Read-only.
	 */
	Map<MessageId, MessageState> getMessageDependents(Transaction txn,
			MessageId m) throws DbException;

	/**
	 * Gets the validation and delivery state of the given message.
	 * <p/>
	 * Read-only.
	 */
	MessageState getMessageState(Transaction txn, MessageId m)
			throws DbException;

	/**
	 * Returns the status of the given delivered message with respect to the
	 * given contact.
	 * <p/>
	 * Read-only.
	 */
	MessageStatus getMessageStatus(Transaction txn, ContactId c, MessageId m)
			throws DbException;

	/**
	 * Returns the message with the given ID for transmission to the given
	 * contact over a transport with the given maximum latency. Returns null
	 * if the message is no longer visible to the contact.
	 * <p/>
	 * Read-only if {@code markAsSent} is false.
	 *
	 * @param markAsSent True if the message should be marked as sent.
	 * If false it can be marked as sent by calling
	 * {@link #setMessagesSent(Transaction, ContactId, Collection, long)}.
	 */
	@Nullable
	Message getMessageToSend(Transaction txn, ContactId c, MessageId m,
			long maxLatency, boolean markAsSent) throws DbException;

	/**
	 * Returns the IDs of all messages that are eligible to be sent to the
	 * given contact.
	 * <p>
	 * Unlike {@link #getMessagesToSend(Transaction, ContactId, long, long)}
	 * this method may return messages that have already been sent and are
	 * not yet due for retransmission.
	 * <p/>
	 * Read-only.
	 */
	Collection<MessageId> getUnackedMessagesToSend(Transaction txn,
			ContactId c) throws DbException;

	/**
	 * Resets the transmission count, expiry time and max latency of all messages
	 * that are eligible to be sent to the given contact. This includes messages
	 * that have already been sent and are not yet due for retransmission.
	 */
	void resetUnackedMessagesToSend(Transaction txn, ContactId c)
			throws DbException;

	/**
	 * Returns the total length, including headers, of all messages that are
	 * eligible to be sent to the given contact. This may include messages
	 * that have already been sent and are not yet due for retransmission.
	 * <p/>
	 * Read-only.
	 */
	long getUnackedMessageBytesToSend(Transaction txn, ContactId c)
			throws DbException;

	/**
	 * Returns the next time (in milliseconds since the Unix epoch) when a
	 * message is due to be deleted, or {@link #NO_CLEANUP_DEADLINE}
	 * if no messages are scheduled to be deleted.
	 * <p/>
	 * Read-only.
	 */
	long getNextCleanupDeadline(Transaction txn) throws DbException;

	/**
	 * Returns the next time (in milliseconds since the Unix epoch) when a
	 * message is due to be sent to the given contact over a transport with
	 * the given latency.
	 * <p>
	 * The returned value may be zero if a message is due to be sent
	 * immediately, or Long.MAX_VALUE if no messages are scheduled to be sent.
	 * <p/>
	 * Read-only.
	 */
	long getNextSendTime(Transaction txn, ContactId c, long maxLatency)
			throws DbException;

	/**
	 * Returns the pending contact with the given ID.
	 * <p/>
	 * Read-only.
	 */
	PendingContact getPendingContact(Transaction txn, PendingContactId p)
			throws DbException;

	/**
	 * Returns all pending contacts.
	 * <p/>
	 * Read-only.
	 */
	Collection<PendingContact> getPendingContacts(Transaction txn)
			throws DbException;

	/**
	 * Returns all settings in the given namespace.
	 * <p/>
	 * Read-only.
	 */
	Settings getSettings(Transaction txn, String namespace) throws DbException;

	/**
	 * Returns the versions of the sync protocol supported by the given contact.
	 * <p/>
	 * Read-only.
	 */
	List<Byte> getSyncVersions(Transaction txn, ContactId c) throws DbException;

	/**
	 * Returns all transport keys for the given transport.
	 * <p/>
	 * Read-only.
	 */
	Collection<TransportKeySet> getTransportKeys(Transaction txn, TransportId t)
			throws DbException;

	/**
	 * Returns the contact IDs and transport IDs for which the DB contains
	 * at least one set of transport keys. Handshake mode and rotation mode
	 * keys are included, whether activated or not.
	 * <p/>
	 * Read-only.
	 */
	Map<ContactId, Collection<TransportId>> getTransportsWithKeys(
			Transaction txn) throws DbException;

	/**
	 * Increments the outgoing stream counter for the given transport keys.
	 */
	void incrementStreamCounter(Transaction txn, TransportId t, KeySetId k)
			throws DbException;

	/**
	 * Merges the given metadata with the existing metadata for the given
	 * group.
	 */
	void mergeGroupMetadata(Transaction txn, GroupId g, Metadata meta)
			throws DbException;

	/**
	 * Merges the given metadata with the existing metadata for the given
	 * message.
	 */
	void mergeMessageMetadata(Transaction txn, MessageId m, Metadata meta)
			throws DbException;

	/**
	 * Merges the given settings with the existing settings in the given
	 * namespace.
	 */
	void mergeSettings(Transaction txn, Settings s, String namespace)
			throws DbException;

	/**
	 * Processes an ack from the given contact.
	 */
	void receiveAck(Transaction txn, ContactId c, Ack a) throws DbException;

	/**
	 * Processes a message from the given contact.
	 */
	void receiveMessage(Transaction txn, ContactId c, Message m)
			throws DbException;

	/**
	 * Processes an offer from the given contact.
	 */
	void receiveOffer(Transaction txn, ContactId c, Offer o) throws DbException;

	/**
	 * Processes a request from the given contact.
	 */
	void receiveRequest(Transaction txn, ContactId c, Request r)
			throws DbException;

	/**
	 * Removes a contact (and all associated state) from the database.
	 */
	void removeContact(Transaction txn, ContactId c) throws DbException;

	/**
	 * Removes a group (and all associated state) from the database.
	 */
	void removeGroup(Transaction txn, Group g) throws DbException;

	/**
	 * Removes an identity (and all associated state) from the database.
	 */
	void removeIdentity(Transaction txn, AuthorId a) throws DbException;

	/**
	 * Removes a message (and all associated state) from the database.
	 */
	void removeMessage(Transaction txn, MessageId m) throws DbException;

	/**
	 * Removes a pending contact (and all associated state) from the database.
	 */
	void removePendingContact(Transaction txn, PendingContactId p)
			throws DbException;

	/**
	 * Removes all temporary messages (and all associated state) from the
	 * database.
	 */
	void removeTemporaryMessages(Transaction txn) throws DbException;

	/**
	 * Removes a transport (and all associated state) from the database.
	 */
	void removeTransport(Transaction txn, TransportId t) throws DbException;

	/**
	 * Removes the given transport keys from the database.
	 */
	void removeTransportKeys(Transaction txn, TransportId t, KeySetId k)
			throws DbException;

	/**
	 * Records an ack for the given messages as having been sent to the given
	 * contact.
	 */
	void setAckSent(Transaction txn, ContactId c, Collection<MessageId> acked)
			throws DbException;

	/**
	 * Sets the cleanup timer duration for the given message. This does not
	 * start the message's cleanup timer.
	 */
	void setCleanupTimerDuration(Transaction txn, MessageId m, long duration)
			throws DbException;

	/**
	 * Marks the given contact as verified.
	 */
	void setContactVerified(Transaction txn, ContactId c) throws DbException;

	/**
	 * Sets an alias name for the contact or unsets it if alias is null.
	 */
	void setContactAlias(Transaction txn, ContactId c, @Nullable String alias)
			throws DbException;

	/**
	 * Sets the given group's visibility to the given contact.
	 */
	void setGroupVisibility(Transaction txn, ContactId c, GroupId g,
			Visibility v) throws DbException;

	/**
	 * Marks the given message as permanent, i.e. not temporary.
	 */
	void setMessagePermanent(Transaction txn, MessageId m) throws DbException;

	/**
	 * Marks the given message as not shared. This method is only meant for
	 * testing.
	 */
	void setMessageNotShared(Transaction txn, MessageId m) throws DbException;

	/**
	 * Marks the given message as shared.
	 */
	void setMessageShared(Transaction txn, MessageId m) throws DbException;

	/**
	 * Sets the validation and delivery state of the given message.
	 */
	void setMessageState(Transaction txn, MessageId m, MessageState state)
			throws DbException;

	/**
	 * Records the given messages as having been sent to the given contact
	 * over a transport with the given maximum latency.
	 */
	void setMessagesSent(Transaction txn, ContactId c,
			Collection<MessageId> sent, long maxLatency) throws DbException;

	/**
	 * Adds dependencies for a message
	 */
	void addMessageDependencies(Transaction txn, Message dependent,
			Collection<MessageId> dependencies) throws DbException;

	/**
	 * Sets the handshake key pair for the identity with the given ID.
	 */
	void setHandshakeKeyPair(Transaction txn, AuthorId local,
			PublicKey publicKey, PrivateKey privateKey) throws DbException;

	/**
	 * Sets the reordering window for the given transport keys in the given
	 * time period.
	 */
	void setReorderingWindow(Transaction txn, KeySetId k, TransportId t,
			long timePeriod, long base, byte[] bitmap) throws DbException;

	/**
	 * Sets the versions of the sync protocol supported by the given contact.
	 */
	void setSyncVersions(Transaction txn, ContactId c, List<Byte> supported)
			throws DbException;

	/**
	 * Marks the given transport keys as usable for outgoing streams.
	 */
	void setTransportKeysActive(Transaction txn, TransportId t, KeySetId k)
			throws DbException;

	/**
	 * Starts the cleanup timer for the given message, if a timer duration
	 * has been set and the timer has not already been started.
	 *
	 * @return The cleanup deadline, or {@link #TIMER_NOT_STARTED} if no
	 * timer duration has been set for this message or its timer has already
	 * been started.
	 */
	long startCleanupTimer(Transaction txn, MessageId m) throws DbException;

	/**
	 * Stops the cleanup timer for the given message, if the timer has been
	 * started.
	 */
	void stopCleanupTimer(Transaction txn, MessageId m) throws DbException;

	/**
	 * Stores the given transport keys, deleting any keys they have replaced.
	 */
	void updateTransportKeys(Transaction txn, Collection<TransportKeySet> keys)
			throws DbException;
}
