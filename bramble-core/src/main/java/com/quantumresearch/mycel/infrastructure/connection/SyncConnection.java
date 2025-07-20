package com.quantumresearch.mycel.infrastructure.connection;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.infrastructure.api.sync.PriorityHandler;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSession;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSessionFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;
import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
class SyncConnection extends Connection {

	final SyncSessionFactory syncSessionFactory;
	final TransportPropertyManager transportPropertyManager;

	SyncConnection(KeyManager keyManager, ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory);
		this.syncSessionFactory = syncSessionFactory;
		this.transportPropertyManager = transportPropertyManager;
	}

	@Nullable
	StreamContext allocateStreamContext(ContactId contactId,
			TransportId transportId) {
		try {
			return keyManager.getStreamContext(contactId, transportId);
		} catch (DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	SyncSession createIncomingSession(StreamContext ctx,
			TransportConnectionReader r, PriorityHandler handler)
			throws IOException {
		InputStream streamReader = streamReaderFactory.createStreamReader(
				r.getInputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory
				.createIncomingSession(c, streamReader, handler);
	}
}
