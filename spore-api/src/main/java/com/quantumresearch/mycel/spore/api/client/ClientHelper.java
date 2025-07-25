package com.quantumresearch.mycel.spore.api.client;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@NotNullByDefault
public interface ClientHelper {

	void addLocalMessage(Message m, BdfDictionary metadata, boolean shared)
			throws DbException, FormatException;

	void addLocalMessage(Transaction txn, Message m, BdfDictionary metadata,
			boolean shared, boolean temporary)
			throws DbException, FormatException;

	Message createMessage(GroupId g, long timestamp, byte[] body);

	Message createMessage(GroupId g, long timestamp, BdfList body)
			throws FormatException;

	Message createMessageForStoringMetadata(GroupId g);

	Message getMessage(MessageId m) throws DbException;

	Message getMessage(Transaction txn, MessageId m) throws DbException;

	BdfList getMessageAsList(MessageId m) throws DbException, FormatException;

	BdfList getMessageAsList(Transaction txn, MessageId m) throws DbException,
			FormatException;

	/**
	 * Transitional alternative to
	 * {@link #getMessageAsList(Transaction, MessageId)} that allows the
	 * message to be in non-canonical form, for backward compatibility.
	 *
	 * @param canonical True if the message must be in canonical form (a
	 * {@link FormatException} will be thrown if it's not.
	 */
	@Deprecated
	BdfList getMessageAsList(Transaction txn, MessageId m, boolean canonical)
			throws DbException, FormatException;

	BdfDictionary getGroupMetadataAsDictionary(GroupId g) throws DbException,
			FormatException;

	BdfDictionary getGroupMetadataAsDictionary(Transaction txn, GroupId g)
			throws DbException, FormatException;

	Collection<MessageId> getMessageIds(Transaction txn, GroupId g,
			BdfDictionary query) throws DbException, FormatException;

	BdfDictionary getMessageMetadataAsDictionary(MessageId m)
			throws DbException, FormatException;

	BdfDictionary getMessageMetadataAsDictionary(Transaction txn, MessageId m)
			throws DbException, FormatException;

	Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(GroupId g)
			throws DbException, FormatException;

	Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			Transaction txn, GroupId g) throws DbException, FormatException;

	Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(GroupId g,
			BdfDictionary query) throws DbException, FormatException;

	Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			Transaction txn, GroupId g, BdfDictionary query) throws DbException,
			FormatException;

	void mergeGroupMetadata(GroupId g, BdfDictionary metadata)
			throws DbException, FormatException;

	void mergeGroupMetadata(Transaction txn, GroupId g, BdfDictionary metadata)
			throws DbException, FormatException;

	void mergeMessageMetadata(MessageId m, BdfDictionary metadata)
			throws DbException, FormatException;

	void mergeMessageMetadata(Transaction txn, MessageId m,
			BdfDictionary metadata) throws DbException, FormatException;

	byte[] toByteArray(BdfDictionary dictionary) throws FormatException;

	byte[] toByteArray(BdfList list) throws FormatException;

	BdfDictionary toDictionary(byte[] b, int off, int len)
			throws FormatException;

	BdfDictionary toDictionary(TransportProperties transportProperties);

	BdfDictionary toDictionary(Map<TransportId, TransportProperties> map);

	BdfList toList(byte[] b, int off, int len) throws FormatException;

	BdfList toList(byte[] b) throws FormatException;

	BdfList toList(Message m) throws FormatException;

	/**
	 * Transitional alternative to {@link #toList(Message)} that allows the
	 * message to be in non-canonical form, for backward compatibility.
	 *
	 * @param canonical True if the message must be in canonical form (a
	 * {@link FormatException} will be thrown if it's not.
	 */
	@Deprecated
	BdfList toList(Message m, boolean canonical) throws FormatException;

	BdfList toList(Author a);

	byte[] sign(String label, BdfList toSign, PrivateKey privateKey)
			throws FormatException, GeneralSecurityException;

	void verifySignature(byte[] signature, String label, BdfList signed,
			PublicKey publicKey)
			throws FormatException, GeneralSecurityException;

	Author parseAndValidateAuthor(BdfList author) throws FormatException;

	PublicKey parseAndValidateAgreementPublicKey(byte[] publicKeyBytes)
			throws FormatException;

	TransportProperties parseAndValidateTransportProperties(
			BdfDictionary properties) throws FormatException;

	Map<TransportId, TransportProperties> parseAndValidateTransportPropertiesMap(
			BdfDictionary properties) throws FormatException;

	/**
	 * Parse and validate the elements of a Mailbox update message.
	 *
	 * @return the parsed update message
	 * @throws FormatException if the message elements are invalid
	 */
	MailboxUpdate parseAndValidateMailboxUpdate(BdfList clientSupports,
			BdfList serverSupports, BdfDictionary properties)
			throws FormatException;

	List<MailboxVersion> parseMailboxVersionList(BdfList bdfList)
			throws FormatException;

	/**
	 * Retrieves the contact ID from the group metadata of the given contact
	 * group.
	 */
	ContactId getContactId(Transaction txn, GroupId contactGroupId)
			throws DbException;

	/**
	 * Stores the given contact ID in the group metadata of the given contact
	 * group.
	 */
	void setContactId(Transaction txn, GroupId contactGroupId, ContactId c)
			throws DbException;
}
