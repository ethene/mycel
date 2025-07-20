package com.quantumresearch.mycel.infrastructure.connection;

import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.infrastructure.api.connection.InterruptibleConnection;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportProperties;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.infrastructure.api.sync.Priority;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSession;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSessionFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import static org.briarproject.nullsafety.NullSafety.requireNonNull;

@NotNullByDefault
abstract class DuplexSyncConnection extends SyncConnection
		implements InterruptibleConnection {

	final Executor ioExecutor;
	final TransportId transportId;
	final TransportConnectionReader reader;
	final TransportConnectionWriter writer;
	final TransportProperties remote;

	private final Object interruptLock = new Object();

	@GuardedBy("interruptLock")
	@Nullable
	private SyncSession outgoingSession = null;
	@GuardedBy("interruptLock")
	private boolean interruptWaiting = false;

	@Override
	public void interruptOutgoingSession() {
		SyncSession out = null;
		synchronized (interruptLock) {
			if (outgoingSession == null) interruptWaiting = true;
			else out = outgoingSession;
		}
		if (out != null) out.interrupt();
	}

	void setOutgoingSession(SyncSession outgoingSession) {
		boolean interruptWasWaiting = false;
		synchronized (interruptLock) {
			this.outgoingSession = outgoingSession;
			if (interruptWaiting) {
				interruptWasWaiting = true;
				interruptWaiting = false;
			}
		}
		if (interruptWasWaiting) outgoingSession.interrupt();
	}

	DuplexSyncConnection(KeyManager keyManager,
			ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory,
			SyncSessionFactory syncSessionFactory,
			TransportPropertyManager transportPropertyManager,
			Executor ioExecutor, TransportId transportId,
			DuplexTransportConnection connection) {
		super(keyManager, connectionRegistry, streamReaderFactory,
				streamWriterFactory, syncSessionFactory,
				transportPropertyManager);
		this.ioExecutor = ioExecutor;
		this.transportId = transportId;
		reader = connection.getReader();
		writer = connection.getWriter();
		remote = connection.getRemoteProperties();
	}

	void onReadError(boolean recognised) {
		disposeOnError(reader, recognised);
		disposeOnError(writer);
		interruptOutgoingSession();
	}

	void onWriteError() {
		disposeOnError(reader, true);
		disposeOnError(writer);
	}

	SyncSession createDuplexOutgoingSession(StreamContext ctx,
			TransportConnectionWriter w, @Nullable Priority priority)
			throws IOException {
		StreamWriter streamWriter = streamWriterFactory.createStreamWriter(
				w.getOutputStream(), ctx);
		ContactId c = requireNonNull(ctx.getContactId());
		return syncSessionFactory.createDuplexOutgoingSession(c,
				ctx.getTransportId(), w.getMaxLatency(), w.getMaxIdleTime(),
				streamWriter, priority);
	}
}
