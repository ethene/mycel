package com.quantumresearch.mycel.spore.client;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.UniqueId;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyParser;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfEntry;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.data.BdfReader;
import com.quantumresearch.mycel.spore.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.spore.api.data.BdfWriter;
import com.quantumresearch.mycel.spore.api.data.BdfWriterFactory;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxAuthToken;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxFolderId;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxProperties;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdate;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateWithMailbox;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxVersion;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.util.Base32;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.Collections.sort;
import static com.quantumresearch.mycel.spore.api.client.ContactGroupConstants.GROUP_KEY_CONTACT_ID;
import static com.quantumresearch.mycel.spore.api.identity.Author.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_AUTHOR_NAME_LENGTH;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_PUBLIC_KEY_LENGTH;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_COUNT;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_AUTHTOKEN;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_INBOXID;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_ONION;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_KEY_OUTBOXID;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager.PROP_ONION_LENGTH;
import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyConstants.MAX_PROPERTIES_PER_TRANSPORT;
import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyConstants.MAX_PROPERTY_LENGTH;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class ClientHelperImpl implements ClientHelper {

	/**
	 * Length in bytes of the random salt used for creating local messages for
	 * storing metadata.
	 */
	private static final int SALT_LENGTH = 32;

	private final DatabaseComponent db;
	private final MessageFactory messageFactory;
	private final BdfReaderFactory bdfReaderFactory;
	private final BdfWriterFactory bdfWriterFactory;
	private final MetadataParser metadataParser;
	private final MetadataEncoder metadataEncoder;
	private final CryptoComponent crypto;
	private final AuthorFactory authorFactory;

	@Inject
	ClientHelperImpl(DatabaseComponent db, MessageFactory messageFactory,
			BdfReaderFactory bdfReaderFactory,
			BdfWriterFactory bdfWriterFactory, MetadataParser metadataParser,
			MetadataEncoder metadataEncoder, CryptoComponent crypto,
			AuthorFactory authorFactory) {
		this.db = db;
		this.messageFactory = messageFactory;
		this.bdfReaderFactory = bdfReaderFactory;
		this.bdfWriterFactory = bdfWriterFactory;
		this.metadataParser = metadataParser;
		this.metadataEncoder = metadataEncoder;
		this.crypto = crypto;
		this.authorFactory = authorFactory;
	}

	@Override
	public void addLocalMessage(Message m, BdfDictionary metadata,
			boolean shared) throws DbException, FormatException {
		db.transaction(false, txn -> addLocalMessage(txn, m, metadata, shared,
				false));
	}

	@Override
	public void addLocalMessage(Transaction txn, Message m,
			BdfDictionary metadata, boolean shared, boolean temporary)
			throws DbException, FormatException {
		db.addLocalMessage(txn, m, metadataEncoder.encode(metadata), shared,
				temporary);
	}

	@Override
	public Message createMessage(GroupId g, long timestamp, byte[] body) {
		return messageFactory.createMessage(g, timestamp, body);
	}

	@Override
	public Message createMessage(GroupId g, long timestamp, BdfList body)
			throws FormatException {
		return messageFactory.createMessage(g, timestamp, toByteArray(body));
	}

	@Override
	public Message createMessageForStoringMetadata(GroupId g) {
		byte[] salt = new byte[SALT_LENGTH];
		crypto.getSecureRandom().nextBytes(salt);
		return messageFactory.createMessage(g, 0, salt);
	}

	@Override
	public Message getMessage(MessageId m) throws DbException {
		return db.transactionWithResult(true, txn -> getMessage(txn, m));
	}

	@Override
	public Message getMessage(Transaction txn, MessageId m) throws DbException {
		return db.getMessage(txn, m);
	}

	@Override
	public BdfList getMessageAsList(MessageId m) throws DbException,
			FormatException {
		return db.transactionWithResult(true, txn -> getMessageAsList(txn, m));
	}

	@Override
	public BdfList getMessageAsList(Transaction txn, MessageId m)
			throws DbException, FormatException {
		return getMessageAsList(txn, m, true);
	}

	@Override
	public BdfList getMessageAsList(Transaction txn, MessageId m,
			boolean canonical) throws DbException, FormatException {
		return toList(db.getMessage(txn, m), canonical);
	}

	@Override
	public BdfDictionary getGroupMetadataAsDictionary(GroupId g)
			throws DbException, FormatException {
		return db.transactionWithResult(true, txn ->
				getGroupMetadataAsDictionary(txn, g));
	}

	@Override
	public BdfDictionary getGroupMetadataAsDictionary(Transaction txn,
			GroupId g) throws DbException, FormatException {
		Metadata metadata = db.getGroupMetadata(txn, g);
		return metadataParser.parse(metadata);
	}

	@Override
	public Collection<MessageId> getMessageIds(Transaction txn, GroupId g,
			BdfDictionary query) throws DbException, FormatException {
		return db.getMessageIds(txn, g, metadataEncoder.encode(query));
	}

	@Override
	public BdfDictionary getMessageMetadataAsDictionary(MessageId m)
			throws DbException, FormatException {
		return db.transactionWithResult(true, txn ->
				getMessageMetadataAsDictionary(txn, m));
	}

	@Override
	public BdfDictionary getMessageMetadataAsDictionary(Transaction txn,
			MessageId m) throws DbException, FormatException {
		Metadata metadata = db.getMessageMetadata(txn, m);
		return metadataParser.parse(metadata);
	}

	@Override
	public Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			GroupId g) throws DbException, FormatException {
		return db.transactionWithResult(true, txn ->
				getMessageMetadataAsDictionary(txn, g));
	}

	@Override
	public Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			Transaction txn, GroupId g) throws DbException, FormatException {
		Map<MessageId, Metadata> raw = db.getMessageMetadata(txn, g);
		Map<MessageId, BdfDictionary> parsed = new HashMap<>(raw.size());
		for (Entry<MessageId, Metadata> e : raw.entrySet())
			parsed.put(e.getKey(), metadataParser.parse(e.getValue()));
		return parsed;
	}

	@Override
	public Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			GroupId g, BdfDictionary query) throws DbException,
			FormatException {
		return db.transactionWithResult(true, txn ->
				getMessageMetadataAsDictionary(txn, g, query));
	}

	@Override
	public Map<MessageId, BdfDictionary> getMessageMetadataAsDictionary(
			Transaction txn, GroupId g, BdfDictionary query) throws DbException,
			FormatException {
		Metadata metadata = metadataEncoder.encode(query);
		Map<MessageId, Metadata> raw = db.getMessageMetadata(txn, g, metadata);
		Map<MessageId, BdfDictionary> parsed = new HashMap<>(raw.size());
		for (Entry<MessageId, Metadata> e : raw.entrySet())
			parsed.put(e.getKey(), metadataParser.parse(e.getValue()));
		return parsed;
	}

	@Override
	public void mergeGroupMetadata(GroupId g, BdfDictionary metadata)
			throws DbException, FormatException {
		db.transaction(false, txn -> mergeGroupMetadata(txn, g, metadata));
	}

	@Override
	public void mergeGroupMetadata(Transaction txn, GroupId g,
			BdfDictionary metadata) throws DbException, FormatException {
		db.mergeGroupMetadata(txn, g, metadataEncoder.encode(metadata));
	}

	@Override
	public void mergeMessageMetadata(MessageId m, BdfDictionary metadata)
			throws DbException, FormatException {
		db.transaction(false, txn -> mergeMessageMetadata(txn, m, metadata));
	}

	@Override
	public void mergeMessageMetadata(Transaction txn, MessageId m,
			BdfDictionary metadata) throws DbException, FormatException {
		db.mergeMessageMetadata(txn, m, metadataEncoder.encode(metadata));
	}

	@Override
	public byte[] toByteArray(BdfDictionary dictionary) throws FormatException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BdfWriter writer = bdfWriterFactory.createWriter(out);
		try {
			writer.writeDictionary(dictionary);
		} catch (FormatException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}

	@Override
	public byte[] toByteArray(BdfList list) throws FormatException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		BdfWriter writer = bdfWriterFactory.createWriter(out);
		try {
			writer.writeList(list);
		} catch (FormatException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return out.toByteArray();
	}

	@Override
	public BdfDictionary toDictionary(byte[] b, int off, int len)
			throws FormatException {
		ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
		BdfReader reader = bdfReaderFactory.createReader(in);
		try {
			BdfDictionary dictionary = reader.readDictionary();
			if (!reader.eof()) throw new FormatException();
			return dictionary;
		} catch (FormatException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BdfDictionary toDictionary(TransportProperties transportProperties) {
		return new BdfDictionary(transportProperties);
	}

	@Override
	public BdfDictionary toDictionary(
			Map<TransportId, TransportProperties> map) {
		BdfDictionary d = new BdfDictionary();
		for (Entry<TransportId, TransportProperties> e : map.entrySet())
			d.put(e.getKey().getString(), new BdfDictionary(e.getValue()));
		return d;
	}

	@Override
	public BdfList toList(byte[] b, int off, int len) throws FormatException {
		return toList(b, off, len, true);
	}

	private BdfList toList(byte[] b, int off, int len, boolean canonical)
			throws FormatException {
		ByteArrayInputStream in = new ByteArrayInputStream(b, off, len);
		BdfReader reader = bdfReaderFactory.createReader(in, canonical);
		try {
			BdfList list = reader.readList();
			if (!reader.eof()) throw new FormatException();
			return list;
		} catch (FormatException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public BdfList toList(byte[] b) throws FormatException {
		return toList(b, 0, b.length, true);
	}

	@Override
	public BdfList toList(Message m) throws FormatException {
		return toList(m.getBody());
	}

	@Override
	public BdfList toList(Message m, boolean canonical) throws FormatException {
		byte[] b = m.getBody();
		return toList(b, 0, b.length, canonical);
	}

	@Override
	public BdfList toList(Author a) {
		return BdfList.of(a.getFormatVersion(), a.getName(), a.getPublicKey());
	}

	@Override
	public byte[] sign(String label, BdfList toSign, PrivateKey privateKey)
			throws FormatException, GeneralSecurityException {
		return crypto.sign(label, toByteArray(toSign), privateKey);
	}

	@Override
	public void verifySignature(byte[] signature, String label, BdfList signed,
			PublicKey publicKey)
			throws FormatException, GeneralSecurityException {
		if (!crypto.verifySignature(signature, label, toByteArray(signed),
				publicKey)) {
			throw new GeneralSecurityException("Invalid signature");
		}
	}

	@Override
	public Author parseAndValidateAuthor(BdfList author)
			throws FormatException {
		checkSize(author, 3);
		int formatVersion = author.getInt(0);
		if (formatVersion != FORMAT_VERSION) throw new FormatException();
		String name = author.getString(1);
		checkLength(name, 1, MAX_AUTHOR_NAME_LENGTH);
		byte[] publicKeyBytes = author.getRaw(2);
		checkLength(publicKeyBytes, 1, MAX_PUBLIC_KEY_LENGTH);
		KeyParser parser = crypto.getSignatureKeyParser();
		PublicKey publicKey;
		try {
			publicKey = parser.parsePublicKey(publicKeyBytes);
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
		return authorFactory.createAuthor(formatVersion, name, publicKey);
	}

	@Override
	public PublicKey parseAndValidateAgreementPublicKey(byte[] publicKeyBytes)
			throws FormatException {
		KeyParser parser = crypto.getAgreementKeyParser();
		try {
			return parser.parsePublicKey(publicKeyBytes);
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
	}

	@Override
	public TransportProperties parseAndValidateTransportProperties(
			BdfDictionary properties) throws FormatException {
		checkSize(properties, 0, MAX_PROPERTIES_PER_TRANSPORT);
		TransportProperties p = new TransportProperties();
		for (String key : properties.keySet()) {
			checkLength(key, 1, MAX_PROPERTY_LENGTH);
			String value = properties.getString(key);
			checkLength(value, 1, MAX_PROPERTY_LENGTH);
			p.put(key, value);
		}
		return p;
	}

	@Override
	public Map<TransportId, TransportProperties> parseAndValidateTransportPropertiesMap(
			BdfDictionary properties) throws FormatException {
		Map<TransportId, TransportProperties> tpMap = new HashMap<>();
		for (String key : properties.keySet()) {
			TransportId transportId = new TransportId(key);
			TransportProperties transportProperties =
					parseAndValidateTransportProperties(
							properties.getDictionary(key));
			tpMap.put(transportId, transportProperties);
		}
		return tpMap;
	}

	@Override
	public MailboxUpdate parseAndValidateMailboxUpdate(BdfList clientSupports,
			BdfList serverSupports, BdfDictionary properties)
			throws FormatException {
		List<MailboxVersion> clientSupportsList =
				parseMailboxVersionList(clientSupports);
		List<MailboxVersion> serverSupportsList =
				parseMailboxVersionList(serverSupports);

		// We must always learn what Mailbox API version(s) the client supports
		if (clientSupports.isEmpty()) {
			throw new FormatException();
		}
		if (properties.isEmpty()) {
			// No mailbox -- cannot claim to support any API versions!
			if (!serverSupports.isEmpty()) {
				throw new FormatException();
			}
			return new MailboxUpdate(clientSupportsList);
		}
		// Mailbox must be accompanied by the Mailbox API version(s) it supports
		if (serverSupports.isEmpty()) {
			throw new FormatException();
		}
		// Accepting more props than we need, for forward compatibility
		if (properties.size() < PROP_COUNT) {
			throw new FormatException();
		}
		String onion = properties.getString(PROP_KEY_ONION);
		checkLength(onion, PROP_ONION_LENGTH);
		try {
			Base32.decode(onion, true);
		} catch (IllegalArgumentException e) {
			throw new FormatException();
		}
		byte[] authToken = properties.getRaw(PROP_KEY_AUTHTOKEN);
		checkLength(authToken, UniqueId.LENGTH);
		byte[] inboxId = properties.getRaw(PROP_KEY_INBOXID);
		checkLength(inboxId, UniqueId.LENGTH);
		byte[] outboxId = properties.getRaw(PROP_KEY_OUTBOXID);
		checkLength(outboxId, UniqueId.LENGTH);
		MailboxProperties props = new MailboxProperties(onion,
				new MailboxAuthToken(authToken), serverSupportsList,
				new MailboxFolderId(inboxId), new MailboxFolderId(outboxId));
		return new MailboxUpdateWithMailbox(clientSupportsList, props);
	}

	@Override
	public List<MailboxVersion> parseMailboxVersionList(BdfList bdfList)
			throws FormatException {
		List<MailboxVersion> list = new ArrayList<>();
		for (int i = 0; i < bdfList.size(); i++) {
			BdfList element = bdfList.getList(i);
			if (element.size() != 2) {
				throw new FormatException();
			}
			list.add(new MailboxVersion(element.getInt(0), element.getInt(1)));
		}
		// Sort the list of versions for easier comparison
		sort(list);
		return list;
	}

	@Override
	public ContactId getContactId(Transaction txn, GroupId contactGroupId)
			throws DbException {
		try {
			BdfDictionary meta =
					getGroupMetadataAsDictionary(txn, contactGroupId);
			return new ContactId(meta.getInt(GROUP_KEY_CONTACT_ID));
		} catch (FormatException e) {
			throw new DbException(e); // Invalid group metadata
		}
	}

	@Override
	public void setContactId(Transaction txn, GroupId contactGroupId,
			ContactId c) throws DbException {
		BdfDictionary meta = BdfDictionary.of(
				new BdfEntry(GROUP_KEY_CONTACT_ID, c.getInt()));
		try {
			mergeGroupMetadata(txn, contactGroupId, meta);
		} catch (FormatException e) {
			throw new AssertionError(e);
		}
	}
}
