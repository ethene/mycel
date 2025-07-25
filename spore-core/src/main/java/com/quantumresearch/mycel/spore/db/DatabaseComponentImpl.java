package com.quantumresearch.mycel.spore.db;

import com.quantumresearch.mycel.spore.api.cleanup.event.CleanupTimerStartedEvent;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.contact.event.ContactAddedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.ContactAliasChangedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.ContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.ContactVerifiedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactAddedEvent;
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.db.CommitAction;
import com.quantumresearch.mycel.spore.api.db.CommitAction.Visitor;
import com.quantumresearch.mycel.spore.api.db.ContactExistsException;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbCallable;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.DbRunnable;
import com.quantumresearch.mycel.spore.api.db.EventAction;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.MigrationListener;
import com.quantumresearch.mycel.spore.api.db.NoSuchContactException;
import com.quantumresearch.mycel.spore.api.db.NoSuchGroupException;
import com.quantumresearch.mycel.spore.api.db.NoSuchIdentityException;
import com.quantumresearch.mycel.spore.api.db.NoSuchMessageException;
import com.quantumresearch.mycel.spore.api.db.NoSuchPendingContactException;
import com.quantumresearch.mycel.spore.api.db.NoSuchTransportException;
import com.quantumresearch.mycel.spore.api.db.NullableDbCallable;
import com.quantumresearch.mycel.spore.api.db.PendingContactExistsException;
import com.quantumresearch.mycel.spore.api.db.TaskAction;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.event.EventExecutor;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.Identity;
import com.quantumresearch.mycel.spore.api.identity.event.IdentityAddedEvent;
import com.quantumresearch.mycel.spore.api.identity.event.IdentityRemovedEvent;
import com.quantumresearch.mycel.spore.api.lifecycle.ShutdownManager;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.settings.Settings;
import com.quantumresearch.mycel.spore.api.settings.event.SettingsUpdatedEvent;
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
import com.quantumresearch.mycel.spore.api.sync.event.GroupAddedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.GroupRemovedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.GroupVisibilityUpdatedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageAddedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageRequestedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageSharedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageStateChangedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageToAckEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageToRequestEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessagesAckedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessagesSentEvent;
import com.quantumresearch.mycel.spore.api.sync.event.SyncVersionsUpdatedEvent;
import com.quantumresearch.mycel.spore.api.sync.validation.MessageState;
import com.quantumresearch.mycel.spore.api.transport.KeySetId;
import com.quantumresearch.mycel.spore.api.transport.TransportKeySet;
import com.quantumresearch.mycel.spore.api.transport.TransportKeys;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import static java.util.Collections.singletonList;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.INVISIBLE;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.validation.MessageState.DELIVERED;
import static com.quantumresearch.mycel.spore.api.sync.validation.MessageState.UNKNOWN;
import static com.quantumresearch.mycel.spore.db.DatabaseConstants.MAX_OFFERED_MESSAGES;
import static com.quantumresearch.mycel.spore.util.LogUtils.logDuration;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;

@ThreadSafe
@NotNullByDefault
class DatabaseComponentImpl<T> implements DatabaseComponent {

	private static final Logger LOG =
			getLogger(DatabaseComponentImpl.class.getName());

	private final Database<T> db;
	private final Class<T> txnClass;
	private final EventBus eventBus;
	private final Executor eventExecutor;
	private final ShutdownManager shutdownManager;
	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final ReentrantReadWriteLock lock =
			new ReentrantReadWriteLock(true);
	private final Visitor visitor = new CommitActionVisitor();

	@Inject
	DatabaseComponentImpl(Database<T> db, Class<T> txnClass, EventBus eventBus,
			@EventExecutor Executor eventExecutor,
			ShutdownManager shutdownManager) {
		this.db = db;
		this.txnClass = txnClass;
		this.eventBus = eventBus;
		this.eventExecutor = eventExecutor;
		this.shutdownManager = shutdownManager;
	}

	@Override
	public boolean open(SecretKey key, @Nullable MigrationListener listener)
			throws DbException {
		boolean reopened = db.open(key, listener);
		shutdownManager.addShutdownHook(() -> {
			try {
				close();
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
		});
		return reopened;
	}

	@Override
	public void close() throws DbException {
		if (closed.getAndSet(true)) return;
		db.close();
	}

	@Override
	public Transaction startTransaction(boolean readOnly) throws DbException {
		// Don't allow reentrant locking
		if (lock.getReadHoldCount() > 0) throw new IllegalStateException();
		if (lock.getWriteHoldCount() > 0) throw new IllegalStateException();
		long start = now();
		if (readOnly) {
			lock.readLock().lock();
			logDuration(LOG, "Waiting for read lock", start);
		} else {
			lock.writeLock().lock();
			logDuration(LOG, "Waiting for write lock", start);
		}
		try {
			return new Transaction(db.startTransaction(), readOnly);
		} catch (DbException | RuntimeException e) {
			if (readOnly) lock.readLock().unlock();
			else lock.writeLock().unlock();
			throw e;
		}
	}

	@Override
	public void commitTransaction(Transaction transaction) throws DbException {
		T txn = txnClass.cast(transaction.unbox());
		if (transaction.isCommitted()) throw new IllegalStateException();
		transaction.setCommitted();
		db.commitTransaction(txn);
	}

	@Override
	public void endTransaction(Transaction transaction) {
		try {
			T txn = txnClass.cast(transaction.unbox());
			if (transaction.isCommitted()) {
				for (CommitAction a : transaction.getActions())
					a.accept(visitor);
			} else {
				db.abortTransaction(txn);
			}
		} finally {
			if (transaction.isReadOnly()) lock.readLock().unlock();
			else lock.writeLock().unlock();
		}
	}

	@Override
	public <E extends Exception> void transaction(boolean readOnly,
			DbRunnable<E> task) throws DbException, E {
		Transaction txn = startTransaction(readOnly);
		try {
			task.run(txn);
			commitTransaction(txn);
		} finally {
			endTransaction(txn);
		}
	}

	@Override
	public <R, E extends Exception> R transactionWithResult(boolean readOnly,
			DbCallable<R, E> task) throws DbException, E {
		Transaction txn = startTransaction(readOnly);
		try {
			R result = task.call(txn);
			commitTransaction(txn);
			return result;
		} finally {
			endTransaction(txn);
		}
	}

	@Override
	public <R, E extends Exception> R transactionWithNullableResult(
			boolean readOnly, NullableDbCallable<R, E> task)
			throws DbException, E {
		Transaction txn = startTransaction(readOnly);
		try {
			R result = task.call(txn);
			commitTransaction(txn);
			return result;
		} finally {
			endTransaction(txn);
		}
	}

	private T unbox(Transaction transaction) {
		if (transaction.isCommitted()) throw new IllegalStateException();
		return txnClass.cast(transaction.unbox());
	}

	@Override
	public ContactId addContact(Transaction transaction, Author remote,
			AuthorId local, @Nullable PublicKey handshake, boolean verified)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, local))
			throw new NoSuchIdentityException();
		if (db.containsIdentity(txn, remote.getId()))
			throw new ContactExistsException(local, remote);
		if (db.containsContact(txn, remote.getId(), local))
			throw new ContactExistsException(local, remote);
		ContactId c = db.addContact(txn, remote, local, handshake, verified);
		transaction.attach(new ContactAddedEvent(c, verified));
		return c;
	}

	@Override
	public void addGroup(Transaction transaction, Group g) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g.getId())) {
			db.addGroup(txn, g);
			transaction.attach(new GroupAddedEvent(g));
		}
	}

	@Override
	public void addIdentity(Transaction transaction, Identity i)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, i.getId())) {
			db.addIdentity(txn, i);
			transaction.attach(new IdentityAddedEvent(i.getId()));
		}
	}

	@Override
	public void addLocalMessage(Transaction transaction, Message m,
			Metadata meta, boolean shared, boolean temporary)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, m.getGroupId()))
			throw new NoSuchGroupException();
		if (!db.containsMessage(txn, m.getId())) {
			db.addMessage(txn, m, DELIVERED, shared, temporary, null);
			transaction.attach(new MessageAddedEvent(m, null));
			transaction.attach(new MessageStateChangedEvent(m.getId(), true,
					DELIVERED));
			if (shared) {
				Map<ContactId, Boolean> visibility =
						db.getGroupVisibility(txn, m.getGroupId());
				transaction.attach(new MessageSharedEvent(m.getId(),
						m.getGroupId(), visibility));
			}
		}
		db.mergeMessageMetadata(txn, m.getId(), meta);
	}

	@Override
	public void addPendingContact(Transaction transaction, PendingContact p,
			AuthorId local) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		Contact contact = db.getContact(txn, p.getPublicKey(), local);
		if (contact != null)
			throw new ContactExistsException(local, contact.getAuthor());
		if (db.containsPendingContact(txn, p.getId())) {
			PendingContact existing = db.getPendingContact(txn, p.getId());
			throw new PendingContactExistsException(existing);
		}
		db.addPendingContact(txn, p);
		transaction.attach(new PendingContactAddedEvent(p));
	}

	@Override
	public void addTransport(Transaction transaction, TransportId t,
			long maxLatency) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			db.addTransport(txn, t, maxLatency);
	}

	@Override
	public KeySetId addTransportKeys(Transaction transaction, ContactId c,
			TransportKeys k) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (!db.containsTransport(txn, k.getTransportId()))
			throw new NoSuchTransportException();
		return db.addTransportKeys(txn, c, k);
	}

	@Override
	public KeySetId addTransportKeys(Transaction transaction,
			PendingContactId p, TransportKeys k) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsPendingContact(txn, p))
			throw new NoSuchPendingContactException();
		if (!db.containsTransport(txn, k.getTransportId()))
			throw new NoSuchTransportException();
		return db.addTransportKeys(txn, p, k);
	}

	@Override
	public boolean containsAcksToSend(Transaction transaction, ContactId c)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.containsAcksToSend(txn, c);
	}

	@Override
	public boolean containsContact(Transaction transaction, AuthorId remote,
			AuthorId local) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, local))
			throw new NoSuchIdentityException();
		return db.containsContact(txn, remote, local);
	}

	@Override
	public boolean containsGroup(Transaction transaction, GroupId g)
			throws DbException {
		T txn = unbox(transaction);
		return db.containsGroup(txn, g);
	}

	@Override
	public boolean containsIdentity(Transaction transaction, AuthorId a)
			throws DbException {
		T txn = unbox(transaction);
		return db.containsIdentity(txn, a);
	}

	@Override
	public boolean containsMessagesToSend(Transaction transaction, ContactId c,
			long maxLatency, boolean eager) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.containsMessagesToSend(txn, c, maxLatency, eager);
	}

	@Override
	public boolean containsPendingContact(Transaction transaction,
			PendingContactId p) throws DbException {
		T txn = unbox(transaction);
		return db.containsPendingContact(txn, p);
	}

	@Override
	public boolean containsTransportKeys(Transaction transaction, ContactId c,
			TransportId t) throws DbException {
		T txn = unbox(transaction);
		return db.containsTransportKeys(txn, c, t);
	}

	@Override
	public void deleteMessage(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.deleteMessage(txn, m);
	}

	@Override
	public void deleteMessageMetadata(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.deleteMessageMetadata(txn, m);
	}

	@Nullable
	@Override
	public Ack generateAck(Transaction transaction, ContactId c,
			int maxMessages) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> ids = db.getMessagesToAck(txn, c, maxMessages);
		if (ids.isEmpty()) return null;
		db.lowerAckFlag(txn, c, ids);
		return new Ack(ids);
	}

	@Nullable
	@Override
	public Collection<Message> generateBatch(Transaction transaction,
			ContactId c, long capacity, long maxLatency) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> ids =
				db.getMessagesToSend(txn, c, capacity, maxLatency);
		if (ids.isEmpty()) return null;
		long totalLength = 0;
		List<Message> messages = new ArrayList<>(ids.size());
		for (MessageId m : ids) {
			Message message = db.getMessage(txn, m);
			totalLength += message.getRawLength();
			messages.add(message);
			db.updateRetransmissionData(txn, c, m, maxLatency);
		}
		db.lowerRequestedFlag(txn, c, ids);
		transaction.attach(new MessagesSentEvent(c, ids, totalLength));
		return messages;
	}

	@Nullable
	@Override
	public Offer generateOffer(Transaction transaction, ContactId c,
			int maxMessages, long maxLatency) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> ids =
				db.getMessagesToOffer(txn, c, maxMessages, maxLatency);
		if (ids.isEmpty()) return null;
		for (MessageId m : ids)
			db.updateRetransmissionData(txn, c, m, maxLatency);
		return new Offer(ids);
	}

	@Nullable
	@Override
	public Request generateRequest(Transaction transaction, ContactId c,
			int maxMessages) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> ids = db.getMessagesToRequest(txn, c,
				maxMessages);
		if (ids.isEmpty()) return null;
		db.removeOfferedMessages(txn, c, ids);
		return new Request(ids);
	}

	@Nullable
	@Override
	public Collection<Message> generateRequestedBatch(Transaction transaction,
			ContactId c, long capacity, long maxLatency) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> ids =
				db.getRequestedMessagesToSend(txn, c, capacity, maxLatency);
		if (ids.isEmpty()) return null;
		long totalLength = 0;
		List<Message> messages = new ArrayList<>(ids.size());
		for (MessageId m : ids) {
			Message message = db.getMessage(txn, m);
			totalLength += message.getRawLength();
			messages.add(message);
			db.updateRetransmissionData(txn, c, m, maxLatency);
		}
		db.lowerRequestedFlag(txn, c, ids);
		transaction.attach(new MessagesSentEvent(c, ids, totalLength));
		return messages;
	}

	@Override
	public Contact getContact(Transaction transaction, ContactId c)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getContact(txn, c);
	}

	@Override
	public Collection<Contact> getContacts(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getContacts(txn);
	}

	@Override
	public Collection<Contact> getContactsByAuthorId(Transaction transaction,
			AuthorId remote) throws DbException {
		T txn = unbox(transaction);
		return db.getContactsByAuthorId(txn, remote);
	}

	@Override
	public Collection<ContactId> getContacts(Transaction transaction,
			AuthorId local) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, local))
			throw new NoSuchIdentityException();
		return db.getContacts(txn, local);
	}

	@Override
	public Group getGroup(Transaction transaction, GroupId g)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getGroup(txn, g);
	}

	@Override
	public GroupId getGroupId(Transaction transaction, MessageId m)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getGroupId(txn, m);
	}

	@Override
	public Metadata getGroupMetadata(Transaction transaction, GroupId g)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getGroupMetadata(txn, g);
	}

	@Override
	public Collection<Group> getGroups(Transaction transaction, ClientId c,
			int majorVersion) throws DbException {
		T txn = unbox(transaction);
		return db.getGroups(txn, c, majorVersion);
	}

	@Override
	public Visibility getGroupVisibility(Transaction transaction, ContactId c,
			GroupId g) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getGroupVisibility(txn, c, g);
	}

	@Override
	public Identity getIdentity(Transaction transaction, AuthorId a)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, a))
			throw new NoSuchIdentityException();
		return db.getIdentity(txn, a);
	}

	@Override
	public Collection<Identity> getIdentities(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getIdentities(txn);
	}

	@Override
	public Message getMessage(Transaction transaction, MessageId m)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessage(txn, m);
	}

	@Override
	public Collection<MessageId> getMessageIds(Transaction transaction,
			GroupId g) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getMessageIds(txn, g);
	}

	@Override
	public Collection<MessageId> getMessageIds(Transaction transaction,
			GroupId g, Metadata query) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getMessageIds(txn, g, query);
	}

	@Override
	public Collection<MessageId> getMessagesToAck(Transaction transaction,
			ContactId c) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getMessagesToAck(txn, c, Integer.MAX_VALUE);
	}

	@Override
	public Collection<MessageId> getMessagesToSend(Transaction transaction,
			ContactId c, long capacity, long maxLatency) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getMessagesToSend(txn, c, capacity, maxLatency);
	}

	@Override
	public Collection<MessageId> getMessagesToValidate(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getMessagesToValidate(txn);
	}

	@Override
	public Collection<MessageId> getPendingMessages(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getPendingMessages(txn);
	}

	@Override
	public Collection<MessageId> getMessagesToShare(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getMessagesToShare(txn);
	}

	@Override
	public Map<GroupId, Collection<MessageId>> getMessagesToDelete(
			Transaction transaction) throws DbException {
		T txn = unbox(transaction);
		return db.getMessagesToDelete(txn);
	}

	@Override
	public Map<MessageId, Metadata> getMessageMetadata(Transaction transaction,
			GroupId g) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getMessageMetadata(txn, g);
	}

	@Override
	public Map<MessageId, Metadata> getMessageMetadata(Transaction transaction,
			GroupId g, Metadata query) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		return db.getMessageMetadata(txn, g, query);
	}

	@Override
	public Metadata getMessageMetadata(Transaction transaction, MessageId m)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessageMetadata(txn, m);
	}

	@Override
	public Metadata getMessageMetadataForValidator(Transaction transaction,
			MessageId m)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessageMetadataForValidator(txn, m);
	}

	@Override
	public MessageState getMessageState(Transaction transaction, MessageId m)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessageState(txn, m);
	}

	@Override
	public Collection<MessageStatus> getMessageStatus(Transaction transaction,
			ContactId c, GroupId g) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		if (db.getGroupVisibility(txn, c, g) == INVISIBLE) {
			// No status rows exist - return default statuses
			Collection<MessageStatus> statuses = new ArrayList<>();
			for (MessageId m : db.getMessageIds(txn, g))
				statuses.add(new MessageStatus(m, c, false, false));
			return statuses;
		}
		return db.getMessageStatus(txn, c, g);
	}

	@Override
	public MessageStatus getMessageStatus(Transaction transaction, ContactId c,
			MessageId m) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		MessageStatus status = db.getMessageStatus(txn, c, m);
		if (status == null) return new MessageStatus(m, c, false, false);
		return status;
	}

	@Nullable
	@Override
	public Message getMessageToSend(Transaction transaction, ContactId c,
			MessageId m, long maxLatency, boolean markAsSent)
			throws DbException {
		if (markAsSent && transaction.isReadOnly()) {
			throw new IllegalArgumentException();
		}
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (!db.containsVisibleMessage(txn, c, m)) return null;
		Message message = db.getMessage(txn, m);
		if (markAsSent) {
			db.updateRetransmissionData(txn, c, m, maxLatency);
			db.lowerRequestedFlag(txn, c, singletonList(m));
			transaction.attach(new MessagesSentEvent(c, singletonList(m),
					message.getRawLength()));
		}
		return message;
	}

	@Override
	public Collection<MessageId> getUnackedMessagesToSend(
			Transaction transaction, ContactId c) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getUnackedMessagesToSend(txn, c);
	}

	@Override
	public void resetUnackedMessagesToSend(Transaction transaction, ContactId c)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		db.resetUnackedMessagesToSend(txn, c);
	}

	@Override
	public long getUnackedMessageBytesToSend(Transaction transaction,
			ContactId c) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getUnackedMessageBytesToSend(txn, c);
	}

	@Override
	public Map<MessageId, MessageState> getMessageDependencies(
			Transaction transaction, MessageId m) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessageDependencies(txn, m);
	}

	@Override
	public Map<MessageId, MessageState> getMessageDependents(
			Transaction transaction, MessageId m) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		return db.getMessageDependents(txn, m);
	}

	@Override
	public long getNextCleanupDeadline(Transaction transaction)
			throws DbException {
		T txn = unbox(transaction);
		return db.getNextCleanupDeadline(txn);
	}

	@Override
	public long getNextSendTime(Transaction transaction, ContactId c,
			long maxLatency) throws DbException {
		T txn = unbox(transaction);
		return db.getNextSendTime(txn, c, maxLatency);
	}

	@Override
	public PendingContact getPendingContact(Transaction transaction,
			PendingContactId p) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsPendingContact(txn, p))
			throw new NoSuchPendingContactException();
		return db.getPendingContact(txn, p);
	}

	@Override
	public Collection<PendingContact> getPendingContacts(
			Transaction transaction) throws DbException {
		T txn = unbox(transaction);
		return db.getPendingContacts(txn);
	}

	@Override
	public Settings getSettings(Transaction transaction, String namespace)
			throws DbException {
		T txn = unbox(transaction);
		return db.getSettings(txn, namespace);
	}

	@Override
	public List<Byte> getSyncVersions(Transaction transaction, ContactId c)
			throws DbException {
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		return db.getSyncVersions(txn, c);
	}

	@Override
	public Collection<TransportKeySet> getTransportKeys(Transaction transaction,
			TransportId t) throws DbException {
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		return db.getTransportKeys(txn, t);
	}

	@Override
	public Map<ContactId, Collection<TransportId>> getTransportsWithKeys(
			Transaction transaction) throws DbException {
		T txn = unbox(transaction);
		return db.getTransportsWithKeys(txn);
	}

	@Override
	public void incrementStreamCounter(Transaction transaction, TransportId t,
			KeySetId k) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		db.incrementStreamCounter(txn, t, k);
	}

	@Override
	public void mergeGroupMetadata(Transaction transaction, GroupId g,
			Metadata meta) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		db.mergeGroupMetadata(txn, g, meta);
	}

	@Override
	public void mergeMessageMetadata(Transaction transaction, MessageId m,
			Metadata meta) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.mergeMessageMetadata(txn, m, meta);
	}

	@Override
	public void mergeSettings(Transaction transaction, Settings s,
			String namespace) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		Settings old = db.getSettings(txn, namespace);
		Settings merged = new Settings();
		merged.putAll(old);
		merged.putAll(s);
		if (!merged.equals(old)) {
			db.mergeSettings(txn, s, namespace);
			transaction.attach(new SettingsUpdatedEvent(namespace, merged));
		}
	}

	@Override
	public void receiveAck(Transaction transaction, ContactId c, Ack a)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		Collection<MessageId> acked = new ArrayList<>();
		for (MessageId m : a.getMessageIds()) {
			if (db.containsVisibleMessage(txn, c, m)) {
				if (db.raiseSeenFlag(txn, c, m)) {
					// This is the first time the message has been acked by
					// this contact. Start the cleanup timer (a no-op unless
					// a cleanup deadline has been set for this message)
					long deadline = db.startCleanupTimer(txn, m);
					if (deadline != TIMER_NOT_STARTED) {
						transaction.attach(new CleanupTimerStartedEvent(m,
								deadline));
					}
					acked.add(m);
				}
			}
		}
		if (acked.size() > 0) {
			transaction.attach(new MessagesAckedEvent(c, acked));
		}
	}

	@Override
	public void receiveMessage(Transaction transaction, ContactId c, Message m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (db.getGroupVisibility(txn, c, m.getGroupId()) != INVISIBLE) {
			if (db.containsMessage(txn, m.getId())) {
				db.raiseSeenFlag(txn, c, m.getId());
				db.raiseAckFlag(txn, c, m.getId());
			} else {
				db.addMessage(txn, m, UNKNOWN, false, false, c);
				transaction.attach(new MessageAddedEvent(m, c));
			}
			transaction.attach(new MessageToAckEvent(c));
		}
	}

	@Override
	public void receiveOffer(Transaction transaction, ContactId c, Offer o)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		boolean ack = false, request = false;
		int count = db.countOfferedMessages(txn, c);
		for (MessageId m : o.getMessageIds()) {
			if (db.containsVisibleMessage(txn, c, m)) {
				db.raiseSeenFlag(txn, c, m);
				db.raiseAckFlag(txn, c, m);
				ack = true;
			} else if (count < MAX_OFFERED_MESSAGES) {
				db.addOfferedMessage(txn, c, m);
				request = true;
				count++;
			}
		}
		if (ack) transaction.attach(new MessageToAckEvent(c));
		if (request) transaction.attach(new MessageToRequestEvent(c));
	}

	@Override
	public void receiveRequest(Transaction transaction, ContactId c, Request r)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		boolean requested = false;
		for (MessageId m : r.getMessageIds()) {
			if (db.containsVisibleMessage(txn, c, m)) {
				db.raiseRequestedFlag(txn, c, m);
				db.resetExpiryTime(txn, c, m);
				requested = true;
			}
		}
		if (requested) transaction.attach(new MessageRequestedEvent(c));
	}

	@Override
	public void removeContact(Transaction transaction, ContactId c)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		db.removeContact(txn, c);
		transaction.attach(new ContactRemovedEvent(c));
	}

	@Override
	public void removeGroup(Transaction transaction, Group g)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		GroupId id = g.getId();
		if (!db.containsGroup(txn, id))
			throw new NoSuchGroupException();
		Collection<ContactId> affected =
				db.getGroupVisibility(txn, id).keySet();
		db.removeGroup(txn, id);
		transaction.attach(new GroupRemovedEvent(g));
		transaction.attach(new GroupVisibilityUpdatedEvent(INVISIBLE,
				affected));
	}

	@Override
	public void removeIdentity(Transaction transaction, AuthorId a)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, a))
			throw new NoSuchIdentityException();
		db.removeIdentity(txn, a);
		transaction.attach(new IdentityRemovedEvent(a));
	}

	@Override
	public void removeMessage(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		// TODO: Don't allow messages with dependents to be removed
		db.removeMessage(txn, m);
	}

	@Override
	public void removePendingContact(Transaction transaction,
			PendingContactId p) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsPendingContact(txn, p))
			throw new NoSuchPendingContactException();
		db.removePendingContact(txn, p);
		transaction.attach(new PendingContactRemovedEvent(p));
	}

	@Override
	public void removeTemporaryMessages(Transaction transaction)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		db.removeTemporaryMessages(txn);
	}

	@Override
	public void removeTransport(Transaction transaction, TransportId t)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		db.removeTransport(txn, t);
	}

	@Override
	public void removeTransportKeys(Transaction transaction, TransportId t,
			KeySetId k) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		db.removeTransportKeys(txn, t, k);
	}

	@Override
	public void setAckSent(Transaction transaction, ContactId c,
			Collection<MessageId> acked) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		db.lowerAckFlag(txn, c, acked);
	}

	@Override
	public void setCleanupTimerDuration(Transaction transaction, MessageId m,
			long duration) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.setCleanupTimerDuration(txn, m, duration);
	}

	@Override
	public void setContactVerified(Transaction transaction, ContactId c)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		db.setContactVerified(txn, c);
		transaction.attach(new ContactVerifiedEvent(c));
	}

	@Override
	public void setContactAlias(Transaction transaction, ContactId c,
			@Nullable String alias) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		transaction.attach(new ContactAliasChangedEvent(c, alias));
		db.setContactAlias(txn, c, alias);
	}

	@Override
	public void setGroupVisibility(Transaction transaction, ContactId c,
			GroupId g, Visibility v) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		if (!db.containsGroup(txn, g))
			throw new NoSuchGroupException();
		Visibility old = db.getGroupVisibility(txn, c, g);
		if (old == v) return;
		if (old == INVISIBLE) db.addGroupVisibility(txn, c, g, v == SHARED);
		else if (v == INVISIBLE) db.removeGroupVisibility(txn, c, g);
		else db.setGroupVisibility(txn, c, g, v == SHARED);
		List<ContactId> affected = singletonList(c);
		transaction.attach(new GroupVisibilityUpdatedEvent(v, affected));
	}

	@Override
	public void setMessagePermanent(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.setMessagePermanent(txn, m);
	}

	@Override
	public void setMessageNotShared(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.setMessageShared(txn, m, false);
	}

	@Override
	public void setMessageShared(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		if (db.getMessageState(txn, m) != DELIVERED)
			throw new IllegalArgumentException("Shared undelivered message");
		db.setMessageShared(txn, m, true);
		GroupId g = db.getGroupId(txn, m);
		Map<ContactId, Boolean> visibility = db.getGroupVisibility(txn, g);
		transaction.attach(new MessageSharedEvent(m, g, visibility));
	}

	@Override
	public void setMessageState(Transaction transaction, MessageId m,
			MessageState state) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.setMessageState(txn, m, state);
		transaction.attach(new MessageStateChangedEvent(m, false, state));
	}

	@Override
	public void setMessagesSent(Transaction transaction, ContactId c,
			Collection<MessageId> sent, long maxLatency) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		long totalLength = 0;
		List<MessageId> visible = new ArrayList<>(sent.size());
		for (MessageId m : sent) {
			if (db.containsVisibleMessage(txn, c, m)) {
				visible.add(m);
				totalLength += db.getMessageLength(txn, m);
				db.updateRetransmissionData(txn, c, m, maxLatency);
			}
		}
		db.lowerRequestedFlag(txn, c, visible);
		if (!visible.isEmpty()) {
			transaction.attach(new MessagesSentEvent(c, visible, totalLength));
		}
	}

	@Override
	public void addMessageDependencies(Transaction transaction,
			Message dependent, Collection<MessageId> dependencies)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, dependent.getId()))
			throw new NoSuchMessageException();
		MessageState dependentState =
				db.getMessageState(txn, dependent.getId());
		for (MessageId dependency : dependencies) {
			db.addMessageDependency(txn, dependent, dependency, dependentState);
		}
	}

	@Override
	public void setHandshakeKeyPair(Transaction transaction, AuthorId local,
			PublicKey publicKey, PrivateKey privateKey) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsIdentity(txn, local))
			throw new NoSuchIdentityException();
		db.setHandshakeKeyPair(txn, local, publicKey, privateKey);
	}

	@Override
	public void setReorderingWindow(Transaction transaction, KeySetId k,
			TransportId t, long timePeriod, long base, byte[] bitmap)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		db.setReorderingWindow(txn, k, t, timePeriod, base, bitmap);
	}

	@Override
	public void setSyncVersions(Transaction transaction, ContactId c,
			List<Byte> supported) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsContact(txn, c))
			throw new NoSuchContactException();
		db.setSyncVersions(txn, c, supported);
		transaction.attach(new SyncVersionsUpdatedEvent(c, supported));
	}

	@Override
	public void setTransportKeysActive(Transaction transaction, TransportId t,
			KeySetId k) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsTransport(txn, t))
			throw new NoSuchTransportException();
		db.setTransportKeysActive(txn, t, k);
	}

	@Override
	public long startCleanupTimer(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		long deadline = db.startCleanupTimer(txn, m);
		if (deadline != TIMER_NOT_STARTED) {
			transaction.attach(new CleanupTimerStartedEvent(m, deadline));
		}
		return deadline;
	}

	@Override
	public void stopCleanupTimer(Transaction transaction, MessageId m)
			throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		if (!db.containsMessage(txn, m))
			throw new NoSuchMessageException();
		db.stopCleanupTimer(txn, m);
	}

	@Override
	public void updateTransportKeys(Transaction transaction,
			Collection<TransportKeySet> keys) throws DbException {
		if (transaction.isReadOnly()) throw new IllegalArgumentException();
		T txn = unbox(transaction);
		for (TransportKeySet ks : keys) {
			TransportId t = ks.getKeys().getTransportId();
			if (db.containsTransport(txn, t))
				db.updateTransportKeys(txn, ks);
		}
	}

	private class CommitActionVisitor implements Visitor {

		@Override
		public void visit(EventAction a) {
			eventBus.broadcast(a.getEvent());
		}

		@Override
		public void visit(TaskAction a) {
			eventExecutor.execute(a.getTask());
		}
	}
}
