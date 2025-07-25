package com.quantumresearch.mycel.app.avatar;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.Pair;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager.ContactHook;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.OpenDatabaseHook;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import com.quantumresearch.mycel.spore.api.sync.GroupFactory;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.InvalidMessageException;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager.ClientVersioningHook;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import com.quantumresearch.mycel.app.api.avatar.AvatarManager;
import com.quantumresearch.mycel.app.api.avatar.AvatarMessageEncoder;
import com.quantumresearch.mycel.app.api.avatar.event.AvatarUpdatedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.sync.validation.IncomingMessageHook.DeliveryAction.ACCEPT_DO_NOT_SHARE;
import static com.quantumresearch.mycel.app.api.attachment.MediaConstants.MSG_KEY_CONTENT_TYPE;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.GROUP_KEY_CONTACT_ID;
import static com.quantumresearch.mycel.app.avatar.AvatarConstants.MSG_KEY_VERSION;

@Immutable
@NotNullByDefault
class AvatarManagerImpl implements AvatarManager, OpenDatabaseHook, ContactHook,
		ClientVersioningHook, IncomingMessageHook {

	private final DatabaseComponent db;
	private final IdentityManager identityManager;
	private final ClientHelper clientHelper;
	private final ClientVersioningManager clientVersioningManager;
	private final MetadataParser metadataParser;
	private final GroupFactory groupFactory;
	private final AvatarMessageEncoder avatarMessageEncoder;

	@Inject
	AvatarManagerImpl(
			DatabaseComponent db,
			IdentityManager identityManager,
			ClientHelper clientHelper,
			ClientVersioningManager clientVersioningManager,
			MetadataParser metadataParser,
			GroupFactory groupFactory,
			AvatarMessageEncoder avatarMessageEncoder) {
		this.db = db;
		this.identityManager = identityManager;
		this.clientHelper = clientHelper;
		this.clientVersioningManager = clientVersioningManager;
		this.metadataParser = metadataParser;
		this.groupFactory = groupFactory;
		this.avatarMessageEncoder = avatarMessageEncoder;
	}

	@Override
	public void onDatabaseOpened(Transaction txn) throws DbException {
		// Create our avatar group if necessary
		LocalAuthor a = identityManager.getLocalAuthor(txn);
		Group ourGroup = getGroup(a.getId());
		if (db.containsGroup(txn, ourGroup.getId())) return;
		db.addGroup(txn, ourGroup);

		// Set things up for any pre-existing contacts
		for (Contact c : db.getContacts(txn)) addingContact(txn, c);
	}

	@Override
	public void addingContact(Transaction txn, Contact c) throws DbException {
		// Create a group to share with the contact
		Group theirGroup = getGroup(c.getAuthor().getId());
		db.addGroup(txn, theirGroup);
		// Attach the contact ID to the group
		BdfDictionary d = new BdfDictionary();
		d.put(GROUP_KEY_CONTACT_ID, c.getId().getInt());
		try {
			clientHelper.mergeGroupMetadata(txn, theirGroup.getId(), d);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
		// Apply the client's visibility to our and their group
		Group ourGroup = getOurGroup(txn);
		Visibility client = clientVersioningManager.getClientVisibility(txn,
				c.getId(), CLIENT_ID, MAJOR_VERSION);
		db.setGroupVisibility(txn, c.getId(), ourGroup.getId(), client);
		db.setGroupVisibility(txn, c.getId(), theirGroup.getId(), client);
	}

	@Override
	public void removingContact(Transaction txn, Contact c) throws DbException {
		db.removeGroup(txn, getGroup(c.getAuthor().getId()));
	}

	@Override
	public void onClientVisibilityChanging(Transaction txn, Contact c,
			Visibility v) throws DbException {
		// Apply the client's visibility to our and the contact group
		Group ourGroup = getOurGroup(txn);
		Group theirGroup = getGroup(c.getAuthor().getId());
		db.setGroupVisibility(txn, c.getId(), ourGroup.getId(), v);
		db.setGroupVisibility(txn, c.getId(), theirGroup.getId(), v);
	}

	@Override
	public DeliveryAction incomingMessage(Transaction txn, Message m,
			Metadata meta) throws DbException, InvalidMessageException {
		Group ourGroup = getOurGroup(txn);
		if (m.getGroupId().equals(ourGroup.getId())) {
			throw new InvalidMessageException(
					"Received incoming message in my avatar group");
		}
		try {
			// Find the latest update, if any
			BdfDictionary d = metadataParser.parse(meta);
			LatestUpdate latest = findLatest(txn, m.getGroupId());
			if (latest != null) {
				if (d.getLong(MSG_KEY_VERSION) > latest.version) {
					// This update is newer - delete the previous update
					db.deleteMessage(txn, latest.messageId);
					db.deleteMessageMetadata(txn, latest.messageId);
				} else {
					// We've already received a newer update - delete this one
					db.deleteMessage(txn, m.getId());
					db.deleteMessageMetadata(txn, m.getId());
					return ACCEPT_DO_NOT_SHARE;
				}
			}
			ContactId contactId = getContactId(txn, m.getGroupId());
			String contentType = d.getString(MSG_KEY_CONTENT_TYPE);
			AttachmentHeader a = new AttachmentHeader(m.getGroupId(), m.getId(),
					contentType);
			txn.attach(new AvatarUpdatedEvent(contactId, a));
		} catch (FormatException e) {
			throw new InvalidMessageException(e);
		}
		return ACCEPT_DO_NOT_SHARE;
	}

	@Override
	public AttachmentHeader addAvatar(String contentType, InputStream in)
			throws DbException, IOException {
		// find latest avatar
		GroupId groupId;
		LatestUpdate latest;
		Transaction txn = db.startTransaction(true);
		try {
			groupId = getOurGroup(txn).getId();
			latest = findLatest(txn, groupId);
			db.commitTransaction(txn);
		} finally {
			db.endTransaction(txn);
		}
		long version = latest == null ? 0 : latest.version + 1;
		// encode message and metadata
		Pair<Message, BdfDictionary> encodedMessage = avatarMessageEncoder
				.encodeUpdateMessage(groupId, version, contentType, in);
		Message m = encodedMessage.getFirst();
		BdfDictionary meta = encodedMessage.getSecond();
		// save/send avatar and delete old one
		return db.transactionWithResult(false, txn2 -> {
			// re-query latest update as it might have changed since last query
			LatestUpdate newLatest = findLatest(txn2, groupId);
			if (newLatest != null && newLatest.version > version) {
				// latest update is newer than our own
				// no need to store or delete anything, just return latest
				return new AttachmentHeader(groupId, newLatest.messageId,
						newLatest.contentType);
			} else if (newLatest != null) {
				// delete latest update if it has the same or lower version
				db.deleteMessage(txn2, newLatest.messageId);
				db.deleteMessageMetadata(txn2, newLatest.messageId);
			}
			clientHelper.addLocalMessage(txn2, m, meta, true, false);
			return new AttachmentHeader(groupId, m.getId(), contentType);
		});
	}

	@Nullable
	@Override
	public AttachmentHeader getAvatarHeader(Transaction txn, Contact c)
			throws DbException {
		try {
			Group g = getGroup(c.getAuthor().getId());
			return getAvatarHeader(txn, g.getId());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Nullable
	@Override
	public AttachmentHeader getMyAvatarHeader(Transaction txn)
			throws DbException {
		try {
			Group g = getOurGroup(txn);
			return getAvatarHeader(txn, g.getId());
		} catch (FormatException e) {
			throw new DbException(e);
		}
	}

	@Nullable
	private AttachmentHeader getAvatarHeader(Transaction txn, GroupId groupId)
			throws DbException, FormatException {
		LatestUpdate latest = findLatest(txn, groupId);
		if (latest == null) return null;
		return new AttachmentHeader(groupId, latest.messageId,
				latest.contentType);
	}

	@Nullable
	private LatestUpdate findLatest(Transaction txn, GroupId g)
			throws DbException, FormatException {
		Map<MessageId, BdfDictionary> metadata =
				clientHelper.getMessageMetadataAsDictionary(txn, g);
		for (Map.Entry<MessageId, BdfDictionary> e : metadata.entrySet()) {
			BdfDictionary meta = e.getValue();
			long version = meta.getLong(MSG_KEY_VERSION);
			String contentType = meta.getString(MSG_KEY_CONTENT_TYPE);
			return new LatestUpdate(e.getKey(), version, contentType);
		}
		return null;
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

	private Group getOurGroup(Transaction txn) throws DbException {
		LocalAuthor a = identityManager.getLocalAuthor(txn);
		return getGroup(a.getId());
	}

	private Group getGroup(AuthorId authorId) {
		return groupFactory
				.createGroup(CLIENT_ID, MAJOR_VERSION, authorId.getBytes());
	}

	private static class LatestUpdate {

		private final MessageId messageId;
		private final long version;
		private final String contentType;

		private LatestUpdate(MessageId messageId, long version,
				String contentType) {
			this.messageId = messageId;
			this.version = version;
			this.contentType = contentType;
		}
	}

}
