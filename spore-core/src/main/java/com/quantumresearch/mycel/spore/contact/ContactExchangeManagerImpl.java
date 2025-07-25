package com.quantumresearch.mycel.spore.contact;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.Contact;
import com.quantumresearch.mycel.spore.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.db.Transaction;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.record.Record;
import com.quantumresearch.mycel.spore.api.record.RecordReader;
import com.quantumresearch.mycel.spore.api.record.RecordReader.RecordPredicate;
import com.quantumresearch.mycel.spore.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.spore.api.record.RecordWriter;
import com.quantumresearch.mycel.spore.api.record.RecordWriterFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.spore.api.transport.StreamWriter;
import com.quantumresearch.mycel.spore.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.identity.AuthorConstants.MAX_SIGNATURE_LENGTH;
import static com.quantumresearch.mycel.spore.api.system.Clock.MIN_REASONABLE_TIME_MS;
import static com.quantumresearch.mycel.spore.contact.ContactExchangeConstants.PROTOCOL_VERSION;
import static com.quantumresearch.mycel.spore.contact.ContactExchangeRecordTypes.CONTACT_INFO;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkLength;
import static com.quantumresearch.mycel.spore.util.ValidationUtils.checkSize;

@Immutable
@NotNullByDefault
class ContactExchangeManagerImpl implements ContactExchangeManager {

	private static final Logger LOG =
			getLogger(ContactExchangeManagerImpl.class.getName());

	// Accept records with current protocol version, known record type
	private static final RecordPredicate ACCEPT = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					isKnownRecordType(r.getRecordType());

	// Ignore records with current protocol version, unknown record type
	private static final RecordPredicate IGNORE = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					!isKnownRecordType(r.getRecordType());

	private static boolean isKnownRecordType(byte type) {
		return type == CONTACT_INFO;
	}

	private final DatabaseComponent db;
	private final ClientHelper clientHelper;
	private final RecordReaderFactory recordReaderFactory;
	private final RecordWriterFactory recordWriterFactory;
	private final Clock clock;
	private final ContactManager contactManager;
	private final IdentityManager identityManager;
	private final TransportPropertyManager transportPropertyManager;
	private final ContactExchangeCrypto contactExchangeCrypto;
	private final StreamReaderFactory streamReaderFactory;
	private final StreamWriterFactory streamWriterFactory;

	@Inject
	ContactExchangeManagerImpl(DatabaseComponent db, ClientHelper clientHelper,
			RecordReaderFactory recordReaderFactory,
			RecordWriterFactory recordWriterFactory, Clock clock,
			ContactManager contactManager, IdentityManager identityManager,
			TransportPropertyManager transportPropertyManager,
			ContactExchangeCrypto contactExchangeCrypto,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory) {
		this.db = db;
		this.clientHelper = clientHelper;
		this.recordReaderFactory = recordReaderFactory;
		this.recordWriterFactory = recordWriterFactory;
		this.clock = clock;
		this.contactManager = contactManager;
		this.identityManager = identityManager;
		this.transportPropertyManager = transportPropertyManager;
		this.contactExchangeCrypto = contactExchangeCrypto;
		this.streamReaderFactory = streamReaderFactory;
		this.streamWriterFactory = streamWriterFactory;
	}

	@Override
	public Contact exchangeContacts(DuplexTransportConnection conn,
			SecretKey masterKey, boolean alice,
			boolean verified) throws IOException, DbException {
		return exchange(null, conn, masterKey, alice, verified);
	}

	@Override
	public Contact exchangeContacts(PendingContactId p,
			DuplexTransportConnection conn, SecretKey masterKey, boolean alice,
			boolean verified) throws IOException, DbException {
		return exchange(p, conn, masterKey, alice, verified);
	}

	private Contact exchange(@Nullable PendingContactId p,
			DuplexTransportConnection conn, SecretKey masterKey, boolean alice,
			boolean verified) throws IOException, DbException {
		// Get the transport connection's input and output streams
		InputStream in = conn.getReader().getInputStream();
		OutputStream out = conn.getWriter().getOutputStream();

		// Get the local author and transport properties
		LocalAuthor localAuthor = identityManager.getLocalAuthor();
		Map<TransportId, TransportProperties> localProperties =
				transportPropertyManager.getLocalProperties();

		// Derive the header keys for the transport streams
		SecretKey localHeaderKey =
				contactExchangeCrypto.deriveHeaderKey(masterKey, alice);
		SecretKey remoteHeaderKey =
				contactExchangeCrypto.deriveHeaderKey(masterKey, !alice);

		// Create the readers
		InputStream streamReader = streamReaderFactory
				.createContactExchangeStreamReader(in, remoteHeaderKey);
		RecordReader recordReader =
				recordReaderFactory.createRecordReader(streamReader);

		// Create the writers
		StreamWriter streamWriter = streamWriterFactory
				.createContactExchangeStreamWriter(out, localHeaderKey);
		RecordWriter recordWriter = recordWriterFactory
				.createRecordWriter(streamWriter.getOutputStream());

		// Create our signature
		byte[] localSignature = contactExchangeCrypto
				.sign(localAuthor.getPrivateKey(), masterKey, alice);

		// Exchange contact info
		long localTimestamp = clock.currentTimeMillis();
		ContactInfo remoteInfo;
		if (alice) {
			sendContactInfo(recordWriter, localAuthor, localProperties,
					localSignature, localTimestamp);
			remoteInfo = receiveContactInfo(recordReader);
		} else {
			remoteInfo = receiveContactInfo(recordReader);
			sendContactInfo(recordWriter, localAuthor, localProperties,
					localSignature, localTimestamp);
		}

		// Send EOF on the outgoing stream
		streamWriter.sendEndOfStream();

		// Skip any remaining records from the incoming stream
		recordReader.readRecord(r -> false, IGNORE);

		// Verify the contact's signature
		PublicKey remotePublicKey = remoteInfo.author.getPublicKey();
		if (!contactExchangeCrypto.verify(remotePublicKey,
				masterKey, !alice, remoteInfo.signature)) {
			LOG.warning("Invalid signature");
			throw new FormatException();
		}

		// The agreed timestamp is the minimum of the peers' timestamps
		long timestamp = Math.min(localTimestamp, remoteInfo.timestamp);
		if (timestamp < MIN_REASONABLE_TIME_MS) {
			LOG.warning("Timestamp is too old");
			throw new FormatException();
		}

		// Add the contact
		Contact contact = addContact(p, remoteInfo.author, localAuthor,
				masterKey, timestamp, alice, verified, remoteInfo.properties);

		// Contact exchange succeeded
		LOG.info("Contact exchange succeeded");
		return contact;
	}

	private void sendContactInfo(RecordWriter recordWriter, Author author,
			Map<TransportId, TransportProperties> properties, byte[] signature,
			long timestamp) throws IOException {
		BdfList authorList = clientHelper.toList(author);
		BdfDictionary props = clientHelper.toDictionary(properties);
		BdfList payload = BdfList.of(authorList, props, signature, timestamp);
		recordWriter.writeRecord(new Record(PROTOCOL_VERSION, CONTACT_INFO,
				clientHelper.toByteArray(payload)));
		recordWriter.flush();
		LOG.info("Sent contact info");
	}

	private ContactInfo receiveContactInfo(RecordReader recordReader)
			throws IOException {
		Record record = recordReader.readRecord(ACCEPT, IGNORE);
		if (record == null) throw new EOFException();
		LOG.info("Received contact info");
		BdfList payload = clientHelper.toList(record.getPayload());
		checkSize(payload, 4);
		Author author = clientHelper.parseAndValidateAuthor(payload.getList(0));
		BdfDictionary props = payload.getDictionary(1);
		Map<TransportId, TransportProperties> properties =
				clientHelper.parseAndValidateTransportPropertiesMap(props);
		byte[] signature = payload.getRaw(2);
		checkLength(signature, 1, MAX_SIGNATURE_LENGTH);
		long timestamp = payload.getLong(3);
		if (timestamp < 0) throw new FormatException();
		return new ContactInfo(author, properties, signature, timestamp);
	}

	private Contact addContact(@Nullable PendingContactId pendingContactId,
			Author remoteAuthor, LocalAuthor localAuthor, SecretKey masterKey,
			long timestamp, boolean alice, boolean verified,
			Map<TransportId, TransportProperties> remoteProperties)
			throws DbException, FormatException {
		Transaction txn = db.startTransaction(false);
		try {
			ContactId contactId;
			if (pendingContactId == null) {
				contactId = contactManager.addContact(txn, remoteAuthor,
						localAuthor.getId(), masterKey, timestamp, alice,
						verified, true);
			} else {
				contactId = contactManager.addContact(txn, pendingContactId,
						remoteAuthor, localAuthor.getId(), masterKey,
						timestamp, alice, verified, true);
			}
			transportPropertyManager.addRemoteProperties(txn, contactId,
					remoteProperties);
			Contact contact = contactManager.getContact(txn, contactId);
			db.commitTransaction(txn);
			return contact;
		} catch (GeneralSecurityException e) {
			// Pending contact's public key is invalid
			throw new FormatException();
		} finally {
			db.endTransaction(txn);
		}
	}

	private static class ContactInfo {

		private final Author author;
		private final Map<TransportId, TransportProperties> properties;
		private final byte[] signature;
		private final long timestamp;

		private ContactInfo(Author author,
				Map<TransportId, TransportProperties> properties,
				byte[] signature, long timestamp) {
			this.author = author;
			this.properties = properties;
			this.signature = signature;
			this.timestamp = timestamp;
		}
	}
}
