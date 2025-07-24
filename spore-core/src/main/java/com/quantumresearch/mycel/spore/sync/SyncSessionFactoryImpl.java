package com.quantumresearch.mycel.spore.sync;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DatabaseExecutor;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.sync.OutgoingSessionRecord;
import com.quantumresearch.mycel.spore.api.sync.Priority;
import com.quantumresearch.mycel.spore.api.sync.PriorityHandler;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordReader;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordReaderFactory;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordWriterFactory;
import com.quantumresearch.mycel.spore.api.sync.SyncSession;
import com.quantumresearch.mycel.spore.api.sync.SyncSessionFactory;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.transport.StreamWriter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.MAX_FILE_PAYLOAD_BYTES;

@Immutable
@NotNullByDefault
class SyncSessionFactoryImpl implements SyncSessionFactory {

	private final DatabaseComponent db;
	private final Executor dbExecutor;
	private final EventBus eventBus;
	private final Clock clock;
	private final SyncRecordReaderFactory recordReaderFactory;
	private final SyncRecordWriterFactory recordWriterFactory;

	@Inject
	SyncSessionFactoryImpl(DatabaseComponent db,
			@DatabaseExecutor Executor dbExecutor, EventBus eventBus,
			Clock clock, SyncRecordReaderFactory recordReaderFactory,
			SyncRecordWriterFactory recordWriterFactory) {
		this.db = db;
		this.dbExecutor = dbExecutor;
		this.eventBus = eventBus;
		this.clock = clock;
		this.recordReaderFactory = recordReaderFactory;
		this.recordWriterFactory = recordWriterFactory;
	}

	@Override
	public SyncSession createIncomingSession(ContactId c, InputStream in,
			PriorityHandler handler) {
		SyncRecordReader recordReader =
				recordReaderFactory.createRecordReader(in);
		return new IncomingSession(db, dbExecutor, eventBus, c, recordReader,
				handler);
	}

	@Override
	public SyncSession createSimplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, boolean eager, StreamWriter streamWriter) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		if (eager) {
			return new EagerSimplexOutgoingSession(db, eventBus, c, t,
					maxLatency, streamWriter, recordWriter);
		} else {
			return new SimplexOutgoingSession(db, eventBus, c, t,
					maxLatency, streamWriter, recordWriter);
		}
	}

	@Override
	public SyncSession createSimplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, StreamWriter streamWriter,
			OutgoingSessionRecord sessionRecord) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		return new MailboxOutgoingSession(db, eventBus, c, t, maxLatency,
				streamWriter, recordWriter, sessionRecord,
				MAX_FILE_PAYLOAD_BYTES);
	}

	@Override
	public SyncSession createDuplexOutgoingSession(ContactId c, TransportId t,
			long maxLatency, int maxIdleTime, StreamWriter streamWriter,
			@Nullable Priority priority) {
		OutputStream out = streamWriter.getOutputStream();
		SyncRecordWriter recordWriter =
				recordWriterFactory.createRecordWriter(out);
		return new DuplexOutgoingSession(db, dbExecutor, eventBus, clock, c, t,
				maxLatency, maxIdleTime, streamWriter, recordWriter, priority);
	}
}
