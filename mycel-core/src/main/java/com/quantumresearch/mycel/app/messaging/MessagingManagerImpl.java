package com.quantumresearch.mycel.app.messaging;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.cleanup.CleanupHook;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager.ContactHook;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.NoSuchMessageException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.MessageStatus;
import com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager.ClientVersioningHook;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import com.quantumresearch.mycel.app.api.attachment.FileTooBigException;
import com.quantumresearch.mycel.app.api.autodelete.AutoDeleteManager;
import com.quantumresearch.mycel.app.api.autodelete.event.ConversationMessagesDeletedEvent;
import com.quantumresearch.mycel.app.api.client.MessageTracker;
import com.quantumresearch.mycel.app.api.client.MessageTracker.GroupCount;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader;
import com.quantumresearch.mycel.app.api.conversation.DeletionResult;
import com.quantumresearch.mycel.app.api.messaging.MessagingManager;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessage;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageFormat;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageHeader;
import com.quantumresearch.mycel.app.api.messaging.event.AttachmentReceivedEvent;
import com.quantumresearch.mycel.app.api.messaging.event.PrivateMessageReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.Collections.emptyList;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.client.ContactGroupConstants.GROUP_KEY_CONTACT_ID;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static com.quantumresearch.mycel.spore.util.IoUtils.copyAndClose;
import static com.quantumresearch.mycel.spore.util.LogUtils.logDuration;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_DESCRIPTOR_LENGTH;
import static com.quantumresearch.mycel.app.api.autodelete.AutoDeleteConstants.NO_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.api.messaging.PrivateMessageFormat.TEXT_IMAGES;
import static com.quantumresearch.mycel.app.api.messaging.PrivateMessageFormat.TEXT_IMAGES_AUTO_DELETE;
import static com.quantumresearch.mycel.app.api.messaging.PrivateMessageFormat.TEXT_ONLY;
import static com.quantumresearch.mycel.app.client.MessageTrackerConstants.MSG_KEY_READ;
import static com.quantumresearch.mycel.app.messaging.MessageTypes.ATTACHMENT;
import static com.quantumresearch.mycel.app.messaging.MessageTypes.PRIVATE_MESSAGE;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MISSING_ATTACHMENT_CLEANUP_DURATION_MS;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_ATTACHMENT_HEADERS;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_AUTO_DELETE_TIMER;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_HAS_TEXT;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_LOCAL;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_MSG_TYPE;
import static com.quantumresearch.mycel.app.messaging.MessagingConstants.MSG_KEY_TIMESTAMP;

@Immutable
@NotNullByDefault
class MessagingManagerImpl implements MessagingManager, IncomingMessageHook,
		ConversationClient, OpenDatabaseHook, ContactHook,
		ClientVersioningHook, CleanupHook {

	private static final Logger LOG =
			getLogger(MessagingManagerImpl.class.getName());

	private final DatabaseComponent db;
	private final ClientHelper clientHelper;
	private final MetadataParser metadataParser;
	private final ConversationManager conversationManager;
	private final MessageTracker messageTracker;
	private final ClientVersioningManager clientVersioningManager;
	private final ContactGroupFactory contactGroupFactory;
	private final AutoDeleteManager autoDeleteManager;

	@Inject
	MessagingManagerImpl(
			DatabaseComponent db,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser,
			ConversationManager conversationManager,
			MessageTracker messageTracker,
			ContactGroupFactory contactGroupFactory,
			AutoDeleteManager autoDeleteManager) {
		this.db = db;
		this.clientHelper = clientHelper;
		this.metadataParser = metadataParser;
		this.conversationManager = conversationManager;
		this.messageTracker = messageTracker;
		this.clientVersioningManager = clientVersioningManager;
		this.contactGroupFactory = contactGroupFactory;
		this.autoDeleteManager = autoDeleteManager;
	}

	@Override
	public GroupCount getGroupCount(Transaction txn, ContactId contactId)
			throws DbException {
		Contact contact = db.getContact(txn, contactId);
		GroupId groupId = getContactGroup(contact).getId();
		return messageTracker.getGroupCount(txn, groupId);
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		// Create a local group to indicate that we've set this client up
		Group localGroup = contactGroupFactory.createLocalGroup(CLIENT_ID,
				MAJOR_VERSION);
		if (db.containsGroup(txn, localGroup.getId())) return;
		db.addGroup(txn, localGroup);
		// Set things up for any pre-existing contacts
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		// Create a group to share with the contact
		Group g = getContactGroup(c);
		db.addGroup(txn, g);
		// Apply the client's visibility to the contact group
		Visibility client = clientVersioningManager.getClientVisibility(txn,
				c.getId(), CLIENT_ID, MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), g.getId(), client);
		// Attach the contact ID to the group
		clientHelper.setContactId(txn, g.getId(), c.getId());
		// Initialize the group count with current time
		messageTracker.initializeGroupCount(txn, g.getId());
	}

	@Override
	public Group getContactGroup(Contact c) {
		return contactGroupFactory.createContactGroup(CLIENT_ID,
				MAJOR_VERSION, c);
	}

	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		db.removeGroup(txn, getContactGroup(c));
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
			BdfDictionary metaDict = metadataParser.parse(meta);
			// Message type is null for version 0.0 private messages
			Integer messageType = metaDict.getOptionalInt(MSG_KEY_MSG_TYPE);
			if (messageType == null) {
				incomingPrivateMessage(txn, m, metaDict, true, emptyList());
			} else if (messageType == PRIVATE_MESSAGE) {
				boolean hasText = metaDict.getBoolean(MSG_KEY_HAS_TEXT);
				List<AttachmentHeader> headers =
						parseAttachmentHeaders(m.getGroupId(), metaDict);
				incomingPrivateMessage(txn, m, metaDict, hasText, headers);
			} else if (messageType == ATTACHMENT) {
				incomingAttachment(txn, m);
			} else {
				throw new InvalidMessageException();
			}
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
		return ACCEPT_DO_NOT_SHARE;
	}

	private void incomingPrivateMessage(Transaction txn, Message m,
			BdfDictionary meta, boolean hasText, List<AttachmentHeader> headers)
			throws DbException, FormatException {
		long start = now();
		GroupId groupId = m.getGroupId();
		long timestamp = meta.getLong(MSG_KEY_TIMESTAMP);
		boolean local = meta.getBoolean(MSG_KEY_LOCAL);
		boolean read = meta.getBoolean(MSG_KEY_READ);
		long timer = meta.getLong(MSG_KEY_AUTO_DELETE_TIMER,
				NO_AUTO_DELETE_TIMER);
		PrivateMessageHeader header =
				new PrivateMessageHeader(m.getId(), groupId, timestamp, local,
						read, false, false, hasText, headers, timer);
		ContactId contactId = getContactId(txn, groupId);
		PrivateMessageReceivedEvent event =
				new PrivateMessageReceivedEvent(header, contactId);
		txn.attach(event);
		conversationManager.trackIncomingMessage(txn, m);
		if (timer != NO_AUTO_DELETE_TIMER) {
			db.setCleanupTimerDuration(txn, m.getId(), timer);
		}
		autoDeleteManager.receiveAutoDeleteTimer(txn, contactId, timer,
				timestamp);
		if (!headers.isEmpty()) stopAttachmentCleanupTimers(txn, m, headers);
		logDuration(LOG, "Receiving private message", start);
	}

	private List<AttachmentHeader> parseAttachmentHeaders(GroupId g,
			BdfDictionary meta) throws FormatException {
		BdfList attachmentHeaders = meta.getList(MSG_KEY_ATTACHMENT_HEADERS);
		int length = attachmentHeaders.size();
		List<AttachmentHeader> headers = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			BdfList header = attachmentHeaders.getList(i);
			MessageId m = new MessageId(header.getRaw(0));
			String contentType = header.getString(1);
			headers.add(new AttachmentHeader(g, m, contentType));
		}
		return headers;
	}

	private void stopAttachmentCleanupTimers(Transaction txn, Message m,
			List<AttachmentHeader> headers)
			throws DbException, FormatException {
		// Fetch the IDs of all remote attachments
		BdfDictionary query = BdfDictionary.of(
				new BdfEntry(MSG_KEY_MSG_TYPE, ATTACHMENT),
				new BdfEntry(MSG_KEY_LOCAL, false));
		Collection<MessageId> results =
				clientHelper.getMessageIds(txn, m.getGroupId(), query);
		// Stop the cleanup timers of any attachments that have already
		// been delivered
		for (AttachmentHeader h : headers) {
			MessageId id = h.getMessageId();
			if (results.contains(id)) db.stopCleanupTimer(txn, id);
		}
	}

	private void incomingAttachment(Transaction txn, Message m)
			throws DbException {
		long start = now();
		ContactId contactId = getContactId(txn, m.getGroupId());
		txn.attach(new AttachmentReceivedEvent(m.getId(), contactId));
		// If no private messages that list this attachment have been
		// delivered, start the cleanup timer. It will be stopped when a
		// private message that lists this attachment is delivered
		BdfDictionary query = BdfDictionary.of(
				new BdfEntry(MSG_KEY_MSG_TYPE, PRIVATE_MESSAGE),
				new BdfEntry(MSG_KEY_LOCAL, false));
		try {
			Map<MessageId, BdfDictionary> results = clientHelper
					.getMessageMetadataAsDictionary(txn, m.getGroupId(), query);
			for (BdfDictionary meta : results.values()) {
				List<AttachmentHeader> headers =
						parseAttachmentHeaders(m.getGroupId(), meta);
				for (AttachmentHeader h : headers) {
					if (h.getMessageId().equals(m.getId())) return;
				}
			}
			// No private messages list this attachment - start the timer
			db.setCleanupTimerDuration(txn, m.getId(),
					MISSING_ATTACHMENT_CLEANUP_DURATION_MS);
			db.startCleanupTimer(txn, m.getId());
		} catch (FormatException e) {
			throw new DbException(e);
		}
		logDuration(LOG, "Receiving attachment", start);
	}

	@Override
	public void addLocalMessage(PrivateMessage m) throws DbException {
		db.transaction(false, txn -> addLocalMessage(txn, m));
	}

	@Override
	public void addLocalMessage(Transaction txn, PrivateMessage m)
			throws DbException {
		try {
			long timer = m.getAutoDeleteTimer();
			BdfDictionary meta = new BdfDictionary();
			meta.put(MSG_KEY_TIMESTAMP, m.getMessage().getTimestamp());
			meta.put(MSG_KEY_LOCAL, true);
			meta.put(MSG_KEY_READ, true);
			if (m.getFormat() != TEXT_ONLY) {
				meta.put(MSG_KEY_MSG_TYPE, PRIVATE_MESSAGE);
				meta.put(MSG_KEY_HAS_TEXT, m.hasText());
				BdfList headers = new BdfList();
				for (AttachmentHeader a : m.getAttachmentHeaders()) {
					headers.add(
							BdfList.of(a.getMessageId(), a.getContentType()));
				}
				meta.put(MSG_KEY_ATTACHMENT_HEADERS, headers);
				if (m.getFormat() == TEXT_IMAGES_AUTO_DELETE
						&& timer != NO_AUTO_DELETE_TIMER) {
					meta.put(MSG_KEY_AUTO_DELETE_TIMER, timer);
				}
			}
			// Mark attachments as shared and permanent now we're ready to send
			for (AttachmentHeader a : m.getAttachmentHeaders()) {
				db.setMessageShared(txn, a.getMessageId());
				db.setMessagePermanent(txn, a.getMessageId());
			}
			clientHelper.addLocalMessage(txn, m.getMessage(), meta, true,
					false);
			if (timer != NO_AUTO_DELETE_TIMER) {
				db.setCleanupTimerDuration(txn, m.getMessage().getId(), timer);
			}
			conversationManager.trackOutgoingMessage(txn, m.getMessage());
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public AttachmentHeader addLocalAttachment(GroupId groupId, long timestamp,
			String contentType, InputStream in)
			throws DbException, IOException {
		// TODO: Support large messages
		ByteArrayOutputStream bodyOut = new ByteArrayOutputStream();
		byte[] descriptor =
				clientHelper.toByteArray(BdfList.of(ATTACHMENT, contentType));
		bodyOut.write(descriptor);
		copyAndClose(in, bodyOut);
		if (bodyOut.size() > MAX_MESSAGE_BODY_LENGTH)
			throw new FileTooBigException();
		byte[] body = bodyOut.toByteArray();
		BdfDictionary meta = new BdfDictionary();
		meta.put(MSG_KEY_TIMESTAMP, timestamp);
		meta.put(MSG_KEY_LOCAL, true);
		meta.put(MSG_KEY_MSG_TYPE, ATTACHMENT);
		meta.put(MSG_KEY_CONTENT_TYPE, contentType);
		meta.put(MSG_KEY_DESCRIPTOR_LENGTH, descriptor.length);
		Message m = clientHelper.createMessage(groupId, timestamp, body);
		// Mark attachments as temporary, not shared until we're ready to send
		db.transaction(false, txn ->
				clientHelper.addLocalMessage(txn, m, meta, false, true));
		return new AttachmentHeader(groupId, m.getId(), contentType);
	}

	@Override
	public void removeAttachment(AttachmentHeader header) throws DbException {
		db.transaction(false,
				txn -> db.removeMessage(txn, header.getMessageId()));
	}

	private ContactId getContactId(Transaction txn, GroupId g)
			throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getGroupMetadataAsDictionary(txn, g);
			return new ContactId(meta.getInt(GROUP_KEY_CONTACT_ID));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public ContactId getContactId(GroupId g) throws DbException {
		try {
			BdfDictionary meta = clientHelper.getGroupMetadataAsDictionary(g);
			return new ContactId(meta.getInt(GROUP_KEY_CONTACT_ID));
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public GroupId getConversationId(ContactId c) throws DbException {
		return db.transactionWithResult(true,
				txn -> getConversationId(txn, c));
	}

	@Override
	public GroupId getConversationId(Transaction txn, ContactId c) throws DbException {
		Contact contact = db.getContact(txn, c);
		return getContactGroup(contact).getId();
	}

	@Override
	public Collection<ConversationMessageHeader> getMessageHeaders(
			Transaction txn, ContactId c) throws DbException {
		Map<MessageId, BdfDictionary> metadata;
		Collection<MessageStatus> statuses;
		GroupId g;
		try {
			g = getContactGroup(db.getContact(txn, c)).getId();
			metadata = clientHelper.getMessageMetadataAsDictionary(txn, g);
			statuses = db.getMessageStatus(txn, c, g);
		} catch (FormatException e) {
			throw new DbException(e);
		}
		Collection<ConversationMessageHeader> headers = new ArrayList<>();
		for (MessageStatus s : statuses) {
			MessageId id = s.getMessageId();
			BdfDictionary meta = metadata.get(id);
			if (meta == null) continue;
			try {
				// Message type is null for version 0.0 private messages
				Integer messageType = meta.getOptionalInt(MSG_KEY_MSG_TYPE);
				if (messageType != null && messageType != PRIVATE_MESSAGE)
					continue;
				long timestamp = meta.getLong(MSG_KEY_TIMESTAMP);
				boolean local = meta.getBoolean(MSG_KEY_LOCAL);
				boolean read = meta.getBoolean(MSG_KEY_READ);
				if (messageType == null) {
					headers.add(new PrivateMessageHeader(id, g, timestamp,
							local, read, s.isSent(), s.isSeen(), true,
							emptyList(), NO_AUTO_DELETE_TIMER));
				} else {
					boolean hasText = meta.getBoolean(MSG_KEY_HAS_TEXT);
					long timer = meta.getLong(MSG_KEY_AUTO_DELETE_TIMER,
							NO_AUTO_DELETE_TIMER);
					headers.add(new PrivateMessageHeader(id, g, timestamp,
							local, read, s.isSent(), s.isSeen(), hasText,
							parseAttachmentHeaders(g, meta), timer));
				}
			} catch (FormatException e) {
				throw new DbException(e);
			}
		}
		return headers;
	}

	@Override
	public Set<MessageId> getMessageIds(Transaction txn, ContactId c)
			throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		Set<MessageId> result = new HashSet<>();
		try {
			Map<MessageId, BdfDictionary> messages =
					clientHelper.getMessageMetadataAsDictionary(txn, g);
			for (Entry<MessageId, BdfDictionary> entry : messages.entrySet()) {
				Integer type =
						entry.getValue().getOptionalInt(MSG_KEY_MSG_TYPE);
				if (type == null || type == PRIVATE_MESSAGE)
					result.add(entry.getKey());
			}
		} catch (FormatException e) {
			throw new DbException(e);
		}
		return result;
	}

	@Override
	public String getMessageText(MessageId m) throws DbException {
		return db.transactionWithNullableResult(true, txn ->
				getMessageText(txn, m));
	}

	@Override
	public String getMessageText(Transaction txn, MessageId m) throws DbException {
		try {
			BdfList body = clientHelper.getMessageAsList(txn, m);
			if (body.size() == 1) return body.getString(0); // Legacy format
			else return body.getOptionalString(1);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Override
	public PrivateMessageFormat getContactMessageFormat(Transaction txn,
			ContactId c) throws DbException {
		int minorVersion = clientVersioningManager
				.getClientMinorVersion(txn, c, CLIENT_ID, 0);
		if (minorVersion >= 3) return TEXT_IMAGES_AUTO_DELETE;
		else if (minorVersion >= 1) return TEXT_IMAGES;
		else return TEXT_ONLY;
	}

	@Override
	public DeletionResult deleteAllMessages(Transaction txn, ContactId c)
			throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		// this indiscriminately deletes all raw messages in this group
		// also attachments
		for (MessageId messageId : db.getMessageIds(txn, g)) {
			db.deleteMessage(txn, messageId);
			db.deleteMessageMetadata(txn, messageId);
		}
		messageTracker.initializeGroupCount(txn, g);
		return new DeletionResult();
	}

	@Override
	public DeletionResult deleteMessages(Transaction txn, ContactId c,
			Set<MessageId> messageIds) throws DbException {
		GroupId g = getContactGroup(db.getContact(txn, c)).getId();
		for (MessageId m : messageIds) deleteMessage(txn, g, m);
		recalculateGroupCount(txn, g);
		return new DeletionResult();
	}

	@Override
	public void deleteMessages(Transaction txn, GroupId g,
			Collection<MessageId> messageIds) throws DbException {
		for (MessageId m : messageIds) deleteMessage(txn, g, m);
		recalculateGroupCount(txn, g);
		ContactId c = getContactId(txn, g);
		txn.attach(new ConversationMessagesDeletedEvent(c, messageIds));
	}

	private void deleteMessage(Transaction txn, GroupId g, MessageId m)
			throws DbException {
		try {
			BdfDictionary meta =
					clientHelper.getMessageMetadataAsDictionary(txn, m);
			Integer messageType = meta.getOptionalInt(MSG_KEY_MSG_TYPE);
			if (messageType != null && messageType == PRIVATE_MESSAGE) {
				for (AttachmentHeader h : parseAttachmentHeaders(g, meta)) {
					try {
						db.deleteMessage(txn, h.getMessageId());
						db.deleteMessageMetadata(txn, h.getMessageId());
					} catch (NoSuchMessageException e) {
						// Continue
					}
				}
			}
			db.deleteMessage(txn, m);
			db.deleteMessageMetadata(txn, m);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	private void recalculateGroupCount(Transaction txn, GroupId g)
			throws DbException {
		try {
			Map<MessageId, BdfDictionary> metadata =
					clientHelper.getMessageMetadataAsDictionary(txn, g);
			int msgCount = 0;
			int unreadCount = 0;
			for (Entry<MessageId, BdfDictionary> entry : metadata.entrySet()) {
				BdfDictionary meta = entry.getValue();
				Integer messageType = meta.getOptionalInt(MSG_KEY_MSG_TYPE);
				if (messageType == null || messageType == PRIVATE_MESSAGE) {
					msgCount++;
					if (!meta.getBoolean(MSG_KEY_READ)) unreadCount++;
				}
			}
			messageTracker.resetGroupCount(txn, g, msgCount, unreadCount);
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}
}
