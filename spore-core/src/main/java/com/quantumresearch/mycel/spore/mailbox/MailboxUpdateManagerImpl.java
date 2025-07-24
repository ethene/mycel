package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager.ContactHook;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxAuthToken;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxSettingsManager.MailboxHook;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateWithMailbox;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.mailbox.event.MailboxPairedEvent;
import com.quantumresearch.mycel.spore.api.mailbox.event.MailboxUnpairedEvent;
import com.quantumresearch.mycel.spore.api.mailbox.event.MailboxUpdateSentToNewContactEvent;
import com.quantumresearch.mycel.spore.api.mailbox.event.RemoteMailboxUpdateEvent;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager.ClientVersioningHook;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;
import javax.inject.Inject;

import static java.util.Collections.emptyList;
import static com.quantumresearch.mycel.spore.api.data.BdfDictionary.NULL_VALUE;
import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class MailboxUpdateManagerImpl implements MailboxUpdateManager,
		OpenDatabaseHook, ContactHook, ClientVersioningHook,
		IncomingMessageHook, MailboxHook {

	private final List<MailboxVersion> clientSupports;
	private final DatabaseComponent db;
	private final ClientHelper clientHelper;
	private final ClientVersioningManager clientVersioningManager;
	private final MetadataParser metadataParser;
	private final ContactGroupFactory contactGroupFactory;
	private final Clock clock;
	private final MailboxSettingsManager mailboxSettingsManager;
	private final CryptoComponent crypto;
	private final Group localGroup;

	@Inject
	MailboxUpdateManagerImpl(List<MailboxVersion> clientSupports,
			DatabaseComponent db, ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser,
			ContactGroupFactory contactGroupFactory, Clock clock,
			MailboxSettingsManager mailboxSettingsManager,
			CryptoComponent crypto) {
		this.clientSupports = clientSupports;
		this.db = db;
		this.clientHelper = clientHelper;
		this.clientVersioningManager = clientVersioningManager;
		this.metadataParser = metadataParser;
		this.contactGroupFactory = contactGroupFactory;
		this.clock = clock;
		this.mailboxSettingsManager = mailboxSettingsManager;
		this.crypto = crypto;
		localGroup = contactGroupFactory.createLocalGroup(CLIENT_ID,
				MAJOR_VERSION);
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		if (db.containsGroup(txn, localGroup.getId())) {
			try {
				BdfDictionary meta = clientHelper.getGroupMetadataAsDictionary(
						txn, localGroup.getId());
				BdfList sent = meta.getList(GROUP_KEY_SENT_CLIENT_SUPPORTS);
				if (clientHelper.parseMailboxVersionList(sent)
						.equals(clientSupports)) {
					return;
				}
			} catch (FormatException e) {
				throw new DbException();
			}
			// Our current clientSupports list has changed compared to what we
			// last sent out.
			for (Contact c : db.getContacts(txn)) {
				MailboxUpdate latest = getLocalUpdate(txn, c.getId());
				MailboxUpdate updated;
				if (latest.hasMailbox()) {
					updated = new MailboxUpdateWithMailbox(
							(MailboxUpdateWithMailbox) latest, clientSupports);
				} else {
					updated = new MailboxUpdate(clientSupports);
				}
				Group g = getContactGroup(c);
				storeMessageReplaceLatest(txn, g.getId(), updated);
			}
		} else {
			db.addGroup(txn, localGroup);
			// Set things up for any pre-existing contacts
			for (Contact c : db.getContacts(txn)) {
				addingContact(txn, c, false);
			}
		}

		try {
			BdfDictionary meta = BdfDictionary.of(new BdfEntry(
					GROUP_KEY_SENT_CLIENT_SUPPORTS,
					encodeSupportsList(clientSupports)));
			clientHelper.mergeGroupMetadata(txn, localGroup.getId(), meta);
		} catch (FormatException e) {
			throw new DbException();
		}
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		addingContact(txn, c, true);
	}

	/**
	 * @param attachEvent True if a {@link MailboxUpdateSentToNewContactEvent}
	 * should be attached to the transaction. We should only do this when
	 * adding a new contact, not when setting up this client for an existing
	 * contact.
	 */
	private void addingContact(Transaction txn, Contact c, boolean attachEvent)
			throws DbException {
		// Create a group to share with the contact
		Group g = getContactGroup(c);
		db.addGroup(txn, g);
		// Apply the client's visibility to the contact group
		Visibility client = clientVersioningManager
				.getClientVisibility(txn, c.getId(), CLIENT_ID, MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), g.getId(), client);
		// Attach the contact ID to the group
		clientHelper.setContactId(txn, g.getId(), c.getId());
		MailboxProperties ownProps =
				mailboxSettingsManager.getOwnMailboxProperties(txn);
		MailboxUpdate u;
		if (ownProps != null) {
			// We are paired, create and send props to the newly added contact
			u = createAndSendUpdateWithMailbox(txn, c,
					ownProps.getServerSupports(), ownProps.getOnion());
		} else {
			// Not paired, but we still want to get our clientSupports sent
			u = sendUpdateNoMailbox(txn, c);
		}
		if (attachEvent) {
			txn.attach(new MailboxUpdateSentToNewContactEvent(c.getId(), u));
		}
	}

	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		db.removeGroup(txn, getContactGroup(c));
	}

	@Override
	public void mailboxPaired(Transaction txn, MailboxProperties p)
			throws DbException {
		Map<ContactId, MailboxUpdateWithMailbox> localUpdates = new HashMap<>();
		for (Contact c : db.getContacts(txn)) {
			MailboxUpdateWithMailbox u = createAndSendUpdateWithMailbox(txn, c,
					p.getServerSupports(), p.getOnion());
			localUpdates.put(c.getId(), u);
		}
		txn.attach(new MailboxPairedEvent(p, localUpdates));
		// Store the list of server-supported versions
		try {
			storeSentServerSupports(txn, p.getServerSupports());
		} catch (FormatException e) {
			throw new DbException();
		}
	}

	@Override
	public void mailboxUnpaired(Transaction txn) throws DbException {
		Map<ContactId, MailboxUpdate> localUpdates = new HashMap<>();
		for (Contact c : db.getContacts(txn)) {
			MailboxUpdate u = sendUpdateNoMailbox(txn, c);
			localUpdates.put(c.getId(), u);
		}
		txn.attach(new MailboxUnpairedEvent(localUpdates));
		// Remove the list of server-supported versions
		try {
			BdfDictionary meta = BdfDictionary.of(new BdfEntry(
					GROUP_KEY_SENT_SERVER_SUPPORTS, NULL_VALUE));
			clientHelper.mergeGroupMetadata(txn, localGroup.getId(), meta);
		} catch (FormatException e) {
			throw new DbException();
		}
	}

	@Override
	public void serverSupportedVersionsReceived(Transaction txn,
			List<MailboxVersion> serverSupports) throws DbException {
		try {
			List<MailboxVersion> oldServerSupports =
					loadSentServerSupports(txn);
			if (serverSupports.equals(oldServerSupports)) return;
			storeSentServerSupports(txn, serverSupports);
			for (Contact c : db.getContacts(txn)) {
				Group contactGroup = getContactGroup(c);
				LatestUpdate latest =
						findLatest(txn, contactGroup.getId(), true);
				// This method should only be called when we have a mailbox,
				// in which case we should have sent a local update to every
				// contact
				if (latest == null) throw new DbException();
				BdfList body =
						clientHelper.getMessageAsList(txn, latest.messageId);
				MailboxUpdate oldUpdate = parseUpdate(body);
				if (!oldUpdate.hasMailbox()) throw new DbException();
				MailboxUpdateWithMailbox newUpdate = updateServerSupports(
						(MailboxUpdateWithMailbox) oldUpdate, serverSupports);
				storeMessageReplaceLatest(txn, contactGroup.getId(), newUpdate,
						latest);
			}
		} catch (FormatException e) {
			throw new DbException();
		}
	}

	private void storeSentServerSupports(Transaction txn,
			List<MailboxVersion> serverSupports)
			throws DbException, FormatException {
		BdfDictionary meta = BdfDictionary.of(new BdfEntry(
				GROUP_KEY_SENT_SERVER_SUPPORTS,
				encodeSupportsList(serverSupports)));
		clientHelper.mergeGroupMetadata(txn, localGroup.getId(), meta);
	}

	private List<MailboxVersion> loadSentServerSupports(Transaction txn)
			throws DbException, FormatException {
		BdfDictionary meta = clientHelper.getGroupMetadataAsDictionary(txn,
				localGroup.getId());
		BdfList serverSupports =
				meta.getOptionalList(GROUP_KEY_SENT_SERVER_SUPPORTS);
		if (serverSupports == null) return emptyList();
		return clientHelper.parseMailboxVersionList(serverSupports);
	}

	/**
	 * Returns a new {@link MailboxUpdateWithMailbox} that updates the list
	 * of server-supported API versions in the given
	 * {@link MailboxUpdateWithMailbox}.
	 */
	private MailboxUpdateWithMailbox updateServerSupports(
			MailboxUpdateWithMailbox old, List<MailboxVersion> serverSupports) {
		MailboxProperties oldProps = old.getMailboxProperties();
		MailboxProperties newProps = new MailboxProperties(oldProps.getOnion(),
				oldProps.getAuthToken(), serverSupports,
				requireNonNull(oldProps.getInboxId()),
				requireNonNull(oldProps.getOutboxId()));
		return new MailboxUpdateWithMailbox(old.getClientSupports(), newProps);
	}

	@Override
	public void onClientVisibilityChanging(Transaction txn, Contact c,
			Visibility v) throws DbException {
		// Apply the client's visibility to the contact group
		Group g = getContactGroup(c);
		db.setGroupVisibility(txn, c.getId(), g.getId(), v);
	}

	@Override
	public DeliveryAction incomingMessage(Transaction txn, Message m,
			Metadata meta) throws DbException, InvalidMessageException {
		try {
			BdfDictionary d = metadataParser.parse(meta);
			// Get latest non-local update in the same group (from same contact)
			LatestUpdate latest = findLatest(txn, m.getGroupId(), false);
			if (latest != null) {
				if (d.getLong(MSG_KEY_VERSION) > latest.version) {
					db.deleteMessage(txn, latest.messageId);
					db.deleteMessageMetadata(txn, latest.messageId);
				} else {
					// Delete this update, we already have a newer one
					db.deleteMessage(txn, m.getId());
					db.deleteMessageMetadata(txn, m.getId());
					return ACCEPT_DO_NOT_SHARE;
				}
			}
			ContactId c = clientHelper.getContactId(txn, m.getGroupId());
			BdfList body = clientHelper.getMessageAsList(txn, m.getId());
			MailboxUpdate u = parseUpdate(body);
			txn.attach(new RemoteMailboxUpdateEvent(c, u));
			// Reset message retransmission timers for the contact. Avoiding
			// messages getting stranded:
			// - on our mailbox, if they now have a mailbox but didn't before
			// - on the contact's old mailbox, if they removed their mailbox
			// - on the contact's old mailbox, if they replaced their mailbox
			db.resetUnackedMessagesToSend(txn, c);
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
		return ACCEPT_DO_NOT_SHARE;
	}

	@Override
	public MailboxUpdate getLocalUpdate(Transaction txn, ContactId c)
			throws DbException {
		MailboxUpdate local = getUpdate(txn, db.getContact(txn, c), true);
		// An update (with or without mailbox) is created when contact is added
		if (local == null) {
			throw new DbException();
		}
		return local;
	}

	@Override
	@Nullable
	public MailboxUpdate getRemoteUpdate(Transaction txn, ContactId c)
			throws DbException {
		return getUpdate(txn, db.getContact(txn, c), false);
	}

	/**
	 * Creates and sends an update message to the given contact. The message
	 * holds our own mailbox's onion, generated unique properties, and lists of
	 * supported Mailbox API version(s). All of which the contact needs to
	 * communicate with our Mailbox.
	 */
	private MailboxUpdateWithMailbox createAndSendUpdateWithMailbox(
			Transaction txn, Contact c, List<MailboxVersion> serverSupports,
			String ownOnion) throws DbException {
		MailboxProperties properties = new MailboxProperties(ownOnion,
				new MailboxAuthToken(crypto.generateUniqueId().getBytes()),
				serverSupports,
				new MailboxFolderId(crypto.generateUniqueId().getBytes()),
				new MailboxFolderId(crypto.generateUniqueId().getBytes()));
		MailboxUpdateWithMailbox u =
				new MailboxUpdateWithMailbox(clientSupports, properties);
		Group g = getContactGroup(c);
		storeMessageReplaceLatest(txn, g.getId(), u);
		return u;
	}

	/**
	 * Sends an update message with empty properties to the given contact. The
	 * empty update indicates for the receiving contact that we don't have any
	 * Mailbox that they can use. It still includes the list of Mailbox API
	 * version(s) that we support as a client.
	 */
	private MailboxUpdate sendUpdateNoMailbox(Transaction txn, Contact c)
			throws DbException {
		Group g = getContactGroup(c);
		MailboxUpdate u = new MailboxUpdate(clientSupports);
		storeMessageReplaceLatest(txn, g.getId(), u);
		return u;
	}

	@Nullable
	private MailboxUpdate getUpdate(Transaction txn, Contact c, boolean local)
			throws DbException {
		MailboxUpdate u = null;
		Group g = getContactGroup(c);
		try {
			LatestUpdate latest = findLatest(txn, g.getId(), local);
			if (latest != null) {
				BdfList body =
						clientHelper.getMessageAsList(txn, latest.messageId);
				u = parseUpdate(body);
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return u;
	}

	private void storeMessageReplaceLatest(Transaction txn, GroupId g,
			MailboxUpdate u) throws DbException {
		try {
			LatestUpdate latest = findLatest(txn, g, true);
			storeMessageReplaceLatest(txn, g, u, latest);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private void storeMessageReplaceLatest(Transaction txn, GroupId g,
			MailboxUpdate u, @Nullable LatestUpdate latest)
			throws DbException, FormatException {
		long version = latest == null ? 1 : latest.version + 1;
		Message m = clientHelper.createMessage(g, clock.currentTimeMillis(),
				encodeProperties(version, u));
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_VERSION, version);
		meta.put(MSG_KEY_LOCAL, true);
		clientHelper.addLocalMessage(txn, m, meta, true, false);
		if (latest != null) {
			db.removeMessage(txn, latest.messageId);
		}
	}

	@Nullable
	private LatestUpdate findLatest(Transaction txn, GroupId g, boolean local)
			throws DbException, FormatException {
		Map<MessageId, BdfDictionary> metadata =
				clientHelper.getMessageMetadataAsDictionary(txn, g);
		// We should have at most 1 local and 1 remote
		if (metadata.size() > 2) {
			throw new IllegalStateException();
		}
		for (Entry<MessageId, BdfDictionary> e : metadata.entrySet()) {
			BdfDictionary meta = e.getValue();
			if (meta.getBoolean(MSG_KEY_LOCAL) == local) {
				return new LatestUpdate(e.getKey(),
						meta.getLong(MSG_KEY_VERSION));
			}
		}
		return null;
	}

	private BdfList encodeProperties(long version, MailboxUpdate u) {
		BdfDictionary dict = new BdfDictionary();
		BdfList serverSupports = new BdfList();
		if (u.hasMailbox()) {
			MailboxUpdateWithMailbox um = (MailboxUpdateWithMailbox) u;
			MailboxProperties properties = um.getMailboxProperties();
			serverSupports = encodeSupportsList(properties.getServerSupports());
			dict.put(PROP_KEY_ONION, properties.getOnion());
			dict.put(PROP_KEY_AUTHTOKEN, properties.getAuthToken());
			dict.put(PROP_KEY_INBOXID, properties.getInboxId());
			dict.put(PROP_KEY_OUTBOXID, properties.getOutboxId());
		}
		return BdfList.of(version, encodeSupportsList(u.getClientSupports()),
				serverSupports, dict);
	}

	private BdfList encodeSupportsList(List<MailboxVersion> supportsList) {
		BdfList supports = new BdfList();
		for (MailboxVersion version : supportsList) {
			supports.add(BdfList.of(version.getMajor(), version.getMinor()));
		}
		return supports;
	}

	private MailboxUpdate parseUpdate(BdfList body) throws FormatException {
		BdfList clientSupports = body.getList(1);
		BdfList serverSupports = body.getList(2);
		BdfDictionary dict = body.getDictionary(3);
		return clientHelper.parseAndValidateMailboxUpdate(clientSupports,
				serverSupports, dict);
	}

	private Group getContactGroup(Contact c) {
		return contactGroupFactory.createContactGroup(CLIENT_ID, MAJOR_VERSION,
				c);
	}

	private static class LatestUpdate {

		private final MessageId messageId;
		private final long version;

		private LatestUpdate(MessageId messageId, long version) {
			this.messageId = messageId;
			this.version = version;
		}
	}
}
