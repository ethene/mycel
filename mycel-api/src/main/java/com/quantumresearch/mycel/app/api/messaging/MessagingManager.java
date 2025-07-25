package com.quantumresearch.mycel.app.api.messaging;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.sync.ClientId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.attachment.AttachmentHeader;
import com.quantumresearch.mycel.app.api.attachment.FileTooBigException;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager.ConversationClient;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

@NotNullByDefault
public interface MessagingManager extends ConversationClient {

	/**
	 * The unique ID of the messaging client.
	 */
	ClientId CLIENT_ID = new ClientId("com.quantumresearch.mycel.app.messaging");

	/**
	 * The current major version of the messaging client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the messaging client.
	 */
	int MINOR_VERSION = 3;

	/**
	 * Stores a local private message.
	 */
	void addLocalMessage(PrivateMessage m) throws DbException;

	/**
	 * Stores a local private message.
	 */
	void addLocalMessage(Transaction txn, PrivateMessage m) throws DbException;

	/**
	 * Stores a local attachment message.
	 *
	 * @throws FileTooBigException If the attachment is too big
	 */
	AttachmentHeader addLocalAttachment(GroupId groupId, long timestamp,
			String contentType, InputStream is) throws DbException, IOException;

	/**
	 * Removes an unsent attachment.
	 */
	void removeAttachment(AttachmentHeader header) throws DbException;

	/**
	 * Returns the ID of the contact with the given private conversation.
	 */
	ContactId getContactId(GroupId g) throws DbException;

	/**
	 * Returns the ID of the private conversation with the given contact.
	 */
	GroupId getConversationId(ContactId c) throws DbException;

	/**
	 * Returns the ID of the private conversation with the given contact.
	 */
	GroupId getConversationId(Transaction txn, ContactId c) throws DbException;

	/**
	 * Returns the text of the private message with the given ID, or null if
	 * the private message has no text.
	 */
	@Nullable
	String getMessageText(MessageId m) throws DbException;

	/**
	 * Returns the text of the private message with the given ID, or null if
	 * the private message has no text.
	 */
	@Nullable
	String getMessageText(Transaction txn, MessageId m) throws DbException;

	/**
	 * Returns the private message format supported by the given contact.
	 */
	PrivateMessageFormat getContactMessageFormat(Transaction txn, ContactId c)
			throws DbException;
}
