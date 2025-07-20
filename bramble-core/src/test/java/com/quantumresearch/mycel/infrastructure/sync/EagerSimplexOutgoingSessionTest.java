package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.db.Transaction;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.sync.Ack;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.Versions;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.infrastructure.test.DbExpectations;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.MAX_MESSAGE_BODY_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.MAX_MESSAGE_IDS;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getContactId;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getMessage;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getTransportId;

public class EagerSimplexOutgoingSessionTest extends BrambleMockTestCase {

	private static final int MAX_LATENCY = Integer.MAX_VALUE;

	private final DatabaseComponent db = context.mock(DatabaseComponent.class);
	private final EventBus eventBus = context.mock(EventBus.class);
	private final StreamWriter streamWriter = context.mock(StreamWriter.class);
	private final SyncRecordWriter recordWriter =
			context.mock(SyncRecordWriter.class);

	private final ContactId contactId = getContactId();
	private final TransportId transportId = getTransportId();
	private final Ack ack =
			new Ack(singletonList(new MessageId(getRandomId())));
	private final Message message = getMessage(new GroupId(getRandomId()),
			MAX_MESSAGE_BODY_LENGTH);
	private final Message message1 = getMessage(new GroupId(getRandomId()),
			MAX_MESSAGE_BODY_LENGTH);

	@Test
	public void testNothingToSendEagerly() throws Exception {
		EagerSimplexOutgoingSession session =
				new EagerSimplexOutgoingSession(db, eventBus, contactId,
						transportId, MAX_LATENCY, streamWriter, recordWriter);

		Transaction noAckTxn = new Transaction(null, false);
		Transaction noIdsTxn = new Transaction(null, true);

		context.checking(new DbExpectations() {{
			// Add listener
			oneOf(eventBus).addListener(session);
			// Send the protocol versions
			oneOf(recordWriter).writeVersions(with(any(Versions.class)));
			// No acks to send
			oneOf(db).transactionWithNullableResult(with(false),
					withNullableDbCallable(noAckTxn));
			oneOf(db).generateAck(noAckTxn, contactId, MAX_MESSAGE_IDS);
			will(returnValue(null));
			// No messages to send
			oneOf(db).transactionWithResult(with(true),
					withDbCallable(noIdsTxn));
			oneOf(db).getUnackedMessagesToSend(noIdsTxn, contactId);
			will(returnValue(emptyList()));
			// Send the end of stream marker
			oneOf(streamWriter).sendEndOfStream();
			// Remove listener
			oneOf(eventBus).removeListener(session);
		}});

		session.run();
	}

	@Test
	public void testSomethingToSendEagerly() throws Exception {
		EagerSimplexOutgoingSession session =
				new EagerSimplexOutgoingSession(db, eventBus, contactId,
						transportId, MAX_LATENCY, streamWriter, recordWriter);

		Transaction ackTxn = new Transaction(null, false);
		Transaction noAckTxn = new Transaction(null, false);
		Transaction idsTxn = new Transaction(null, true);
		Transaction msgTxn = new Transaction(null, false);
		Transaction msgTxn1 = new Transaction(null, false);

		context.checking(new DbExpectations() {{
			// Add listener
			oneOf(eventBus).addListener(session);
			// Send the protocol versions
			oneOf(recordWriter).writeVersions(with(any(Versions.class)));
			// One ack to send
			oneOf(db).transactionWithNullableResult(with(false),
					withNullableDbCallable(ackTxn));
			oneOf(db).generateAck(ackTxn, contactId, MAX_MESSAGE_IDS);
			will(returnValue(ack));
			oneOf(recordWriter).writeAck(ack);
			// No more acks
			oneOf(db).transactionWithNullableResult(with(false),
					withNullableDbCallable(noAckTxn));
			oneOf(db).generateAck(noAckTxn, contactId, MAX_MESSAGE_IDS);
			will(returnValue(null));
			// Two messages to send
			oneOf(db).transactionWithResult(with(true), withDbCallable(idsTxn));
			oneOf(db).getUnackedMessagesToSend(idsTxn, contactId);
			will(returnValue(asList(message.getId(), message1.getId())));
			// Try to send the first message - it's no longer shared
			oneOf(db).transactionWithNullableResult(with(false),
					withNullableDbCallable(msgTxn));
			oneOf(db).getMessageToSend(msgTxn, contactId, message.getId(),
					MAX_LATENCY, true);
			will(returnValue(null));
			// Send the second message
			oneOf(db).transactionWithNullableResult(with(false),
					withNullableDbCallable(msgTxn1));
			oneOf(db).getMessageToSend(msgTxn1, contactId, message1.getId(),
					MAX_LATENCY, true);
			will(returnValue(message1));
			oneOf(recordWriter).writeMessage(message1);
			// Send the end of stream marker
			oneOf(streamWriter).sendEndOfStream();
			// Remove listener
			oneOf(eventBus).removeListener(session);
		}});

		session.run();
	}
}
