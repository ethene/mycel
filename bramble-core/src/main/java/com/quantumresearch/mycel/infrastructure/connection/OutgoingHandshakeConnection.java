package com.quantumresearch.mycel.infrastructure.connection;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.infrastructure.api.contact.Contact;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.HandshakeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.HandshakeManager.HandshakeResult;
import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import static java.util.logging.Level.WARNING;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;

@NotNullByDefault
class OutgoingHandshakeConnection extends HandshakeConnection
		implements Runnable {

	OutgoingHandshakeConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			HandshakeManager handshakeManager,
			ContactExchangeManager contactExchangeManager,
			ConnectionManager connectionManager,
			PendingContactId pendingContactId,
			TransportId transportId, DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, handshakeManager, contactExchangeManager,
				connectionManager, pendingContactId, transportId, connection);
	}

	@Override
	public void run() {
		// Allocate the outgoing stream context
		StreamContext ctxOut =
				allocateStreamContext(pendingContactId, transportId);
		if (ctxOut == null) {
			LOG.warning("Could not allocate stream context");
			onError();
			return;
		}
		// Flush the output stream to send the outgoing stream header
		StreamWriter out;
		try {
			out = streamWriterFactory.createStreamWriter(
					writer.getOutputStream(), ctxOut);
			out.getOutputStream().flush();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
			onError();
			return;
		}
		// Read and recognise the tag
		StreamContext ctxIn = recogniseTag(reader, transportId);
		// Unrecognised tags are suspicious in this case
		if (ctxIn == null) {
			LOG.warning("Unrecognised tag for returning stream");
			onError();
			return;
		}
		// Check that the stream comes from the expected pending contact
		PendingContactId inPendingContactId = ctxIn.getPendingContactId();
		if (inPendingContactId == null) {
			LOG.warning("Expected rendezvous tag, got contact tag");
			onError();
			return;
		}
		if (!inPendingContactId.equals(pendingContactId)) {
			LOG.warning("Wrong pending contact ID for returning stream");
			onError();
			return;
		}
		// Close the connection if it's redundant
		if (!connectionRegistry.registerConnection(pendingContactId)) {
			LOG.info("Redundant rendezvous connection");
			onError();
			return;
		}
		// Handshake and exchange contacts
		try {
			InputStream in = streamReaderFactory.createStreamReader(
					reader.getInputStream(), ctxIn);
			HandshakeResult result =
					handshakeManager.handshake(pendingContactId, in, out);
			Contact contact = contactExchangeManager.exchangeContacts(
					pendingContactId, connection, result.getMasterKey(),
					result.isAlice(), false);
			connectionRegistry.unregisterConnection(pendingContactId, true);
			// Reuse the connection as a transport connection
			connectionManager.manageOutgoingConnection(contact.getId(),
					transportId, connection);
		} catch (IOException | DbException e) {
			logException(LOG, WARNING, e);
			onError();
			connectionRegistry.unregisterConnection(pendingContactId, false);
		}
	}

	private void onError() {
		// 'Recognised' is always true for outgoing connections
		onError(true);
	}
}
