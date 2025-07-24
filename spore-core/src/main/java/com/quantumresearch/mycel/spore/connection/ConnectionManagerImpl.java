package com.quantumresearch.mycel.spore.connection;

import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.spore.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.HandshakeManager;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.sync.OutgoingSessionRecord;
import com.quantumresearch.mycel.spore.api.sync.SyncSessionFactory;
import com.quantumresearch.mycel.spore.api.transport.KeyManager;
import com.quantumresearch.mycel.spore.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.spore.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.SecureRandom;
import java.util.concurrent.Executor;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class ConnectionManagerImpl implements ConnectionManager {

	private final Executor ioExecutor;
	private final KeyManager keyManager;
	private final StreamReaderFactory streamReaderFactory;
	private final StreamWriterFactory streamWriterFactory;
	private final SyncSessionFactory syncSessionFactory;
	private final HandshakeManager handshakeManager;
	private final ContactExchangeManager contactExchangeManager;
	private final ConnectionRegistry connectionRegistry;
	private final TransportPropertyManager transportPropertyManager;
	private final SecureRandom secureRandom;

	@Inject
	ConnectionManagerImpl(@IoExecutor Executor ioExecutor,
			KeyManager keyManager, StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			HandshakeManager handshakeManager,
			ContactExchangeManager contactExchangeManager,
			ConnectionRegistry connectionRegistry,
			TransportPropertyManager transportPropertyManager,
			SecureRandom secureRandom) {
		this.ioExecutor = ioExecutor;
		this.keyManager = keyManager;
		this.streamReaderFactory = streamReaderFactory;
		this.streamWriterFactory = streamWriterFactory;
		this.syncSessionFactory = syncSessionFactory;
		this.handshakeManager = handshakeManager;
		this.contactExchangeManager = contactExchangeManager;
		this.connectionRegistry = connectionRegistry;
		this.transportPropertyManager = transportPropertyManager;
		this.secureRandom = secureRandom;
	}


	@Override
	public void manageIncomingConnection(TransportId t,
			TransportConnectionReader r) {
		ioExecutor.execute(new IncomingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, t, r, null));
	}

	@Override
	public void manageIncomingConnection(TransportId t,
			TransportConnectionReader r, TagController c) {
		ioExecutor.execute(new IncomingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, t, r, c));
	}

	@Override
	public void manageIncomingConnection(TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new IncomingDuplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, ioExecutor,
				t, d));
	}

	@Override
	public void manageIncomingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new IncomingHandshakeConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				handshakeManager, contactExchangeManager, this, p, t, d));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w) {
		ioExecutor.execute(new OutgoingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, c, t, w, null));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w, OutgoingSessionRecord sessionRecord) {
		ioExecutor.execute(new OutgoingSimplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, c, t, w,
				sessionRecord));
	}

	@Override
	public void manageOutgoingConnection(ContactId c, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new OutgoingDuplexSyncConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				syncSessionFactory, transportPropertyManager, ioExecutor,
				secureRandom, c, t, d));
	}

	@Override
	public void manageOutgoingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d) {
		ioExecutor.execute(new OutgoingHandshakeConnection(keyManager,
				connectionRegistry, streamReaderFactory, streamWriterFactory,
				handshakeManager, contactExchangeManager, this, p, t, d));
	}
}
