package com.quantumresearch.mycel.spore.sync;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.contact.event.ContactRemovedEvent;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.db.DatabaseExecutor;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.event.EventListener;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.event.LifecycleEvent;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.event.TransportInactiveEvent;
import com.quantumresearch.mycel.spore.api.record.Record;
import com.quantumresearch.mycel.spore.api.sync.Ack;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.Offer;
import com.quantumresearch.mycel.spore.api.sync.Priority;
import com.quantumresearch.mycel.spore.api.sync.Request;
import com.quantumresearch.mycel.spore.api.sync.SyncConstants;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.spore.api.sync.SyncSession;
import com.quantumresearch.mycel.spore.api.sync.Versions;
import com.quantumresearch.mycel.spore.api.sync.event.CloseSyncConnectionsEvent;
import com.quantumresearch.mycel.spore.api.sync.event.GroupVisibilityUpdatedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageRequestedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageSharedEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageToAckEvent;
import com.quantumresearch.mycel.spore.api.sync.event.MessageToRequestEvent;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.transport.StreamWriter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

import static java.lang.Boolean.TRUE;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager.LifecycleState.STOPPING;
import static com.quantumresearch.mycel.spore.api.record.Record.RECORD_HEADER_BYTES;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_IDS;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.SUPPORTED_VERSIONS;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;

/**
 * An outgoing {@link SyncSession} suitable for duplex transports. The session
 * offers messages before sending them, keeps its output stream open when there
 * are no records to send, and reacts to events that make records available to
 * send.
 */
@ThreadSafe
@NotNullByDefault
class DuplexOutgoingSession implements SyncSession, EventListener {

	private static final Logger LOG =
			getLogger(DuplexOutgoingSession.class.getName());

	private static final ThrowingRunnable<IOException> CLOSE = () -> {
	};
	private static final ThrowingRunnable<IOException>
			NEXT_SEND_TIME_DECREASED = () -> {
	};

	/**
	 * The batch capacity must be at least {@link Record#RECORD_HEADER_BYTES}
	 * + {@link SyncConstants#MAX_MESSAGE_LENGTH} to ensure that maximum-size
	 * messages can be selected for transmission. Larger batches will mean
	 * fewer round-trips between the DB and the output stream, but each
	 * round-trip will block the DB for longer.
	 */
	private static final int BATCH_CAPACITY =
			(RECORD_HEADER_BYTES + MAX_MESSAGE_LENGTH) * 2;

	private final DatabaseComponent db;
	private final Executor dbExecutor;
	private final EventBus eventBus;
	private final Clock clock;
	private final ContactId contactId;
	private final TransportId transportId;
	private final long maxLatency, maxIdleTime;
	private final StreamWriter streamWriter;
	private final SyncRecordWriter recordWriter;
	@Nullable
	private final Priority priority;
	private final BlockingQueue<ThrowingRunnable<IOException>> writerTasks;

	private final AtomicBoolean generateAckQueued = new AtomicBoolean(false);
	private final AtomicBoolean generateBatchQueued = new AtomicBoolean(false);
	private final AtomicBoolean generateOfferQueued = new AtomicBoolean(false);
	private final AtomicBoolean generateRequestQueued =
			new AtomicBoolean(false);
	private final AtomicLong nextSendTime = new AtomicLong(Long.MAX_VALUE);

	private volatile boolean interrupted = false;

	DuplexOutgoingSession(DatabaseComponent db, Executor dbExecutor,
			EventBus eventBus, Clock clock, ContactId contactId,
			TransportId transportId, long maxLatency, int maxIdleTime,
			StreamWriter streamWriter, SyncRecordWriter recordWriter,
			@Nullable Priority priority) {
		this.db = db;
		this.dbExecutor = dbExecutor;
		this.eventBus = eventBus;
		this.clock = clock;
		this.contactId = contactId;
		this.transportId = transportId;
		this.maxLatency = maxLatency;
		this.maxIdleTime = maxIdleTime;
		this.streamWriter = streamWriter;
		this.recordWriter = recordWriter;
		this.priority = priority;
		writerTasks = new LinkedBlockingQueue<>();
	}

	@IoExecutor
	@Override
	public void run() throws IOException {
		eventBus.addListener(this);
		try {
			// Send our supported protocol versions
			recordWriter.writeVersions(new Versions(SUPPORTED_VERSIONS));
			// Send our connection priority, if this is an outgoing connection
			if (priority != null) recordWriter.writePriority(priority);
			// Start a query for each type of record
			generateAck();
			generateBatch();
			generateOffer();
			generateRequest();
			long now = clock.currentTimeMillis();
			long nextKeepalive = now + maxIdleTime;
			boolean dataToFlush = true;
			// Write records until interrupted
			try {
				while (!interrupted) {
					// Work out how long we should wait for a record
					now = clock.currentTimeMillis();
					long keepaliveWait = Math.max(0, nextKeepalive - now);
					long sendWait = Math.max(0, nextSendTime.get() - now);
					long wait = Math.min(keepaliveWait, sendWait);
					// Flush any unflushed data if we're going to wait
					if (wait > 0 && dataToFlush && writerTasks.isEmpty()) {
						recordWriter.flush();
						dataToFlush = false;
						nextKeepalive = now + maxIdleTime;
					}
					// Wait for a record
					ThrowingRunnable<IOException> task = writerTasks.poll(wait,
							MILLISECONDS);
					if (task == null) {
						now = clock.currentTimeMillis();
						if (now >= nextSendTime.get()) {
							// Check for retransmittable messages
							LOG.info("Checking for retransmittable messages");
							setNextSendTime(Long.MAX_VALUE);
							generateBatch();
							generateOffer();
						}
						if (now >= nextKeepalive) {
							// Flush the stream to keep it alive
							LOG.info("Sending keepalive");
							recordWriter.flush();
							dataToFlush = false;
							nextKeepalive = now + maxIdleTime;
						}
					} else if (task == CLOSE) {
						LOG.info("Closed");
						break;
					} else if (task == NEXT_SEND_TIME_DECREASED) {
						LOG.info("Next send time decreased");
					} else {
						task.run();
						dataToFlush = true;
					}
				}
				streamWriter.sendEndOfStream();
			} catch (InterruptedException e) {
				LOG.info("Interrupted while waiting for a record to write");
				Thread.currentThread().interrupt();
			}
		} finally {
			eventBus.removeListener(this);
		}
	}

	private void generateAck() {
		if (generateAckQueued.compareAndSet(false, true))
			dbExecutor.execute(new GenerateAck());
	}

	private void generateBatch() {
		if (generateBatchQueued.compareAndSet(false, true))
			dbExecutor.execute(new GenerateBatch());
	}

	private void generateOffer() {
		if (generateOfferQueued.compareAndSet(false, true))
			dbExecutor.execute(new GenerateOffer());
	}

	private void generateRequest() {
		if (generateRequestQueued.compareAndSet(false, true))
			dbExecutor.execute(new GenerateRequest());
	}

	private void setNextSendTime(long time) {
		long old = nextSendTime.getAndSet(time);
		if (time < old) writerTasks.add(NEXT_SEND_TIME_DECREASED);
	}

	@Override
	public void interrupt() {
		interrupted = true;
		writerTasks.add(CLOSE);
	}

	@Override
	public void eventOccurred(Event e) {
		if (e instanceof ContactRemovedEvent) {
			ContactRemovedEvent c = (ContactRemovedEvent) e;
			if (c.getContactId().equals(contactId)) interrupt();
		} else if (e instanceof MessageSharedEvent) {
			MessageSharedEvent m = (MessageSharedEvent) e;
			// If the contact is present in the map (ie the value is not null)
			// and the value is true, the message's group is shared with the
			// contact and therefore the message may now be sendable
			if (m.getGroupVisibility().get(contactId) == TRUE) {
				generateOffer();
			}
		} else if (e instanceof GroupVisibilityUpdatedEvent) {
			GroupVisibilityUpdatedEvent g = (GroupVisibilityUpdatedEvent) e;
			if (g.getVisibility() == SHARED &&
					g.getAffectedContacts().contains(contactId)) {
				generateOffer();
			}
		} else if (e instanceof MessageRequestedEvent) {
			if (((MessageRequestedEvent) e).getContactId().equals(contactId))
				generateBatch();
		} else if (e instanceof MessageToAckEvent) {
			if (((MessageToAckEvent) e).getContactId().equals(contactId))
				generateAck();
		} else if (e instanceof MessageToRequestEvent) {
			if (((MessageToRequestEvent) e).getContactId().equals(contactId))
				generateRequest();
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

	private class GenerateAck implements Runnable {

		@DatabaseExecutor
		@Override
		public void run() {
			if (interrupted) return;
			if (!generateAckQueued.getAndSet(false)) throw new AssertionError();
			try {
				Ack a = db.transactionWithNullableResult(false, txn ->
						db.generateAck(txn, contactId, MAX_MESSAGE_IDS));
				if (LOG.isLoggable(INFO))
					LOG.info("Generated ack: " + (a != null));
				if (a != null) writerTasks.add(new WriteAck(a));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				interrupt();
			}
		}
	}

	private class WriteAck implements ThrowingRunnable<IOException> {

		private final Ack ack;

		private WriteAck(Ack ack) {
			this.ack = ack;
		}

		@IoExecutor
		@Override
		public void run() throws IOException {
			if (interrupted) return;
			recordWriter.writeAck(ack);
			LOG.info("Sent ack");
			generateAck();
		}
	}

	private class GenerateBatch implements Runnable {

		@DatabaseExecutor
		@Override
		public void run() {
			if (interrupted) return;
			if (!generateBatchQueued.getAndSet(false))
				throw new AssertionError();
			try {
				Collection<Message> b =
						db.transactionWithNullableResult(false, txn -> {
							Collection<Message> batch =
									db.generateRequestedBatch(txn, contactId,
											BATCH_CAPACITY, maxLatency);
							setNextSendTime(db.getNextSendTime(txn, contactId,
									maxLatency));
							return batch;
						});
				if (LOG.isLoggable(INFO))
					LOG.info("Generated batch: " + (b != null));
				if (b != null) writerTasks.add(new WriteBatch(b));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				interrupt();
			}
		}
	}

	private class WriteBatch implements ThrowingRunnable<IOException> {

		private final Collection<Message> batch;

		private WriteBatch(Collection<Message> batch) {
			this.batch = batch;
		}

		@IoExecutor
		@Override
		public void run() throws IOException {
			if (interrupted) return;
			for (Message m : batch) recordWriter.writeMessage(m);
			LOG.info("Sent batch");
			generateBatch();
		}
	}

	private class GenerateOffer implements Runnable {

		@DatabaseExecutor
		@Override
		public void run() {
			if (interrupted) return;
			if (!generateOfferQueued.getAndSet(false))
				throw new AssertionError();
			try {
				Offer o = db.transactionWithNullableResult(false, txn -> {
					Offer offer = db.generateOffer(txn, contactId,
							MAX_MESSAGE_IDS, maxLatency);
					setNextSendTime(db.getNextSendTime(txn, contactId,
							maxLatency));
					return offer;
				});
				if (LOG.isLoggable(INFO))
					LOG.info("Generated offer: " + (o != null));
				if (o != null) writerTasks.add(new WriteOffer(o));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				interrupt();
			}
		}
	}

	private class WriteOffer implements ThrowingRunnable<IOException> {

		private final Offer offer;

		private WriteOffer(Offer offer) {
			this.offer = offer;
		}

		@IoExecutor
		@Override
		public void run() throws IOException {
			if (interrupted) return;
			recordWriter.writeOffer(offer);
			LOG.info("Sent offer");
			generateOffer();
		}
	}

	private class GenerateRequest implements Runnable {

		@DatabaseExecutor
		@Override
		public void run() {
			if (interrupted) return;
			if (!generateRequestQueued.getAndSet(false))
				throw new AssertionError();
			try {
				Request r = db.transactionWithNullableResult(false, txn ->
						db.generateRequest(txn, contactId, MAX_MESSAGE_IDS));
				if (LOG.isLoggable(INFO))
					LOG.info("Generated request: " + (r != null));
				if (r != null) writerTasks.add(new WriteRequest(r));
			} catch (DbException e) {
				logException(LOG, WARNING, e);
				interrupt();
			}
		}
	}

	private class WriteRequest implements ThrowingRunnable<IOException> {

		private final Request request;

		private WriteRequest(Request request) {
			this.request = request;
		}

		@IoExecutor
		@Override
		public void run() throws IOException {
			if (interrupted) return;
			recordWriter.writeRequest(request);
			LOG.info("Sent request");
			generateRequest();
		}
	}
}
