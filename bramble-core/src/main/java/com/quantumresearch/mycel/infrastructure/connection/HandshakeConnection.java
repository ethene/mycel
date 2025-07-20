package com.quantumresearch.mycel.infrastructure.connection;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.HandshakeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;

@NotNullByDefault
abstract class HandshakeConnection extends Connection {

	final HandshakeManager handshakeManager;
	final ContactExchangeManager contactExchangeManager;
	final ConnectionManager connectionManager;
	final PendingContactId pendingContactId;
	final TransportId transportId;
	final DuplexTransportConnection connection;
	final TransportConnectionReader reader;
	final TransportConnectionWriter writer;

	HandshakeConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			HandshakeManager handshakeManager,
			ContactExchangeManager contactExchangeManager,
			ConnectionManager connectionManager,
			PendingContactId pendingContactId,
			TransportId transportId, DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory);
		this.handshakeManager = handshakeManager;
		this.contactExchangeManager = contactExchangeManager;
		this.connectionManager = connectionManager;
		this.pendingContactId = pendingContactId;
		this.transportId = transportId;
		this.connection = connection;
		reader = connection.getReader();
		writer = connection.getWriter();
	}

	@Nullable
	StreamContext allocateStreamContext(PendingContactId pendingContactId,
			TransportId transportId) {
		try {
			return keyManager.getStreamContext(pendingContactId, transportId);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	void onError(boolean recognised) {
		disposeOnError(reader, recognised);
		disposeOnError(writer);
	}
}
