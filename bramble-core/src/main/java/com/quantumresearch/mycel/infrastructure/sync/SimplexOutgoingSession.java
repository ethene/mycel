package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.contact.event.ContactRemovedEvent;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.event.EventListener;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.event.LifecycleEvent;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.infrastructure.api.record.Record;
import com.quantumresearch.mycel.infrastructure.api.sync.Ack;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSession;
import com.quantumresearch.mycel.infrastructure.api.sync.Versions;
import com.quantumresearch.mycel.infrastructure.api.sync.event.CloseSyncConnectionsEvent;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager.LifecycleState.STOPPING;
import static com.quantumresearch.mycel.infrastructure.api.record.Record.RECORD_HEADER_BYTES;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.MAX_MESSAGE_IDS;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.MAX_MESSAGE_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.SUPPORTED_VERSIONS;
import static com.quantumresearch.mycel.infrastructure.util.LogUtils.logException;

/**
 * An outgoing {@link SyncSession} suitable for simplex transports. The session
 * sends messages without offering them first, and closes its output stream
 * when there are no more records to send.
 */
@ThreadSafe
@NotNullByDefault
class SimplexOutgoingSession implements SyncSession, EventListener {

	private static final Logger LOG =
			getLogger(SimplexOutgoingSession.class.getName());

	/**
	 * The batch capacity must be at least {@link Record#RECORD_HEADER_BYTES}
	 * + {@link SyncConstants#MAX_MESSAGE_LENGTH} to ensure that maximum-size
	 * messages can be selected for transmission. Larger batches will mean
	 * fewer round-trips between the DB and the output stream, but each
	 * round-trip will block the DB for longer.
	 */
	static final int BATCH_CAPACITY =
			(RECORD_HEADER_BYTES + MAX_MESSAGE_LENGTH) * 2;

	protected final DatabaseComponent db;
	protected final EventBus eventBus;
	protected final ContactId contactId;
	protected final TransportId transportId;
	protected final long maxLatency;
	protected final StreamWriter streamWriter;
	protected final SyncRecordWriter recordWriter;

	private volatile boolean interrupted = false;

	SimplexOutgoingSession(DatabaseComponent db,
			EventBus eventBus,
			ContactId contactId,
			TransportId transportId,
			long maxLatency,
			StreamWriter streamWriter,
			SyncRecordWriter recordWriter) {
		this.db = db;
		this.eventBus = eventBus;
		this.contactId = contactId;
		this.transportId = transportId;
		this.maxLatency = maxLatency;
		this.streamWriter = streamWriter;
		this.recordWriter = recordWriter;
	}

	@IoExecutor
	@Override
	public void run() throws IOException {
		eventBus.addListener(this);
		try {
			// Send our supported protocol versions
			recordWriter.writeVersions(new Versions(SUPPORTED_VERSIONS));
			try {
				sendAcks();
				sendMessages();
			} catch (DbException e) {
				logException(LOG, WARNING, e);
			}
			streamWriter.sendEndOfStream();
		} finally {
			eventBus.removeListener(this);
		}
	}

	@Override
	public void interrupt() {
		interrupted = true;
	}

	boolean isInterrupted() {
		return interrupted;
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			ContactRemovedEvent c = (ContactRemovedEvent) e;
			if (c.getContactId().equals(contactId)) interrupt();
		} else if (e instanceof LifecycleEvent) {
			LifecycleEvent l = (LifecycleEvent) e;
			if (l.getLifecycleState() == STOPPING) interrupt();
		} else if (e instanceof CloseSyncConnectionsEvent) {
			CloseSyncConnectionsEvent c = (CloseSyncConnectionsEvent) e;
			if (c.getTransportId().equals(transportId)) interrupt();
		} else if (e instanceof TransportInactiveEvent) {
			TransportInactiveEvent t = (TransportInactiveEvent) e;
			if (t.getTransportId().equals(transportId)) interrupt();
		}
	}

	void sendAcks() throws DbException, IOException {
		while (!isInterrupted()) if (!generateAndSendAck()) break;
	}

	private boolean generateAndSendAck() throws DbException, IOException {
		Ack a = db.transactionWithNullableResult(false, txn ->
				db.generateAck(txn, contactId, MAX_MESSAGE_IDS));
		if (LOG.isLoggable(INFO))
			LOG.info("Generated ack: " + (a != null));
		if (a == null) return false; // No more acks to send
		recordWriter.writeAck(a);
		LOG.info("Sent ack");
		return true;
	}

	void sendMessages() throws DbException, IOException {
		while (!isInterrupted()) if (!generateAndSendBatch()) break;
	}

	private boolean generateAndSendBatch() throws DbException, IOException {
		Collection<Message> b = db.transactionWithNullableResult(false, txn ->
				db.generateBatch(txn, contactId, BATCH_CAPACITY, maxLatency));
		if (LOG.isLoggable(INFO))
			LOG.info("Generated batch: " + (b != null));
		if (b == null) return false; // No more messages to send
		for (Message m : b) recordWriter.writeMessage(m);
		LOG.info("Sent batch");
		return true;
	}
}
