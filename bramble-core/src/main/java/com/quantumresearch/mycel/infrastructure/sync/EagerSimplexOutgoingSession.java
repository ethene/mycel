package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.DbException;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import javax.annotation.concurrent.ThreadSafe;

import static java.util.logging.Level.INFO;
import static java.util.logging.Logger.getLogger;

/**
 * A {@link SimplexOutgoingSession} that sends messages eagerly, ie
 * regardless of whether they're due for retransmission.
 */
@ThreadSafe
@NotNullByDefault
class EagerSimplexOutgoingSession extends SimplexOutgoingSession {

	private static final Logger LOG =
			getLogger(EagerSimplexOutgoingSession.class.getName());

	EagerSimplexOutgoingSession(DatabaseComponent db,
			EventBus eventBus,
			ContactId contactId,
			TransportId transportId,
			long maxLatency,
			StreamWriter streamWriter,
			SyncRecordWriter recordWriter) {
		super(db, eventBus, contactId, transportId, maxLatency, streamWriter,
				recordWriter);
	}

	@Override
	void sendMessages() throws DbException, IOException {
		for (MessageId m : loadUnackedMessageIdsToSend()) {
			if (isInterrupted()) break;
			Message message = db.transactionWithNullableResult(false, txn ->
					db.getMessageToSend(txn, contactId, m, maxLatency, true));
			if (message == null) continue; // No longer shared
			recordWriter.writeMessage(message);
			LOG.info("Sent message");
		}
	}

	private Collection<MessageId> loadUnackedMessageIdsToSend()
			throws DbException {
		Collection<MessageId> ids = db.transactionWithResult(true, txn ->
				db.getUnackedMessagesToSend(txn, contactId));
		if (LOG.isLoggable(INFO)) {
			LOG.info(ids.size() + " unacked messages to send");
		}
		return ids;
	}
}
