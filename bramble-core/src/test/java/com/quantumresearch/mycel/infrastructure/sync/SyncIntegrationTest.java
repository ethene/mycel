package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.sync.Ack;
import com.quantumresearch.mycel.infrastructure.api.sync.ClientId;
import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.sync.Offer;
import com.quantumresearch.mycel.infrastructure.api.sync.Request;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordReader;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriterFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriter;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamWriterFactory;
import com.quantumresearch.mycel.infrastructure.test.BrambleTestCase;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.MAX_GROUP_DESCRIPTOR_LENGTH;
import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.PROTOCOL_VERSION;
import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.TAG_LENGTH;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getClientId;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getContactId;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getSecretKey;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getTransportId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SyncIntegrationTest extends BrambleTestCase {

	@Inject
	GroupFactory groupFactory;
	@Inject
	MessageFactory messageFactory;
	@Inject
	StreamReaderFactory streamReaderFactory;
	@Inject
	StreamWriterFactory streamWriterFactory;
	@Inject
	SyncRecordReaderFactory recordReaderFactory;
	@Inject
	SyncRecordWriterFactory recordWriterFactory;
	@Inject
	TransportCrypto transportCrypto;

	private final ContactId contactId;
	private final TransportId transportId;
	private final SecretKey tagKey, headerKey;
	private final long streamNumber;
	private final Message message, message1;
	private final Collection<MessageId> messageIds;

	public SyncIntegrationTest() throws Exception {

		SyncIntegrationTestComponent component =
				DaggerSyncIntegrationTestComponent.builder().build();
		BrambleCoreIntegrationTestEagerSingletons.Helper
				.injectEagerSingletons(component);
		component.inject(this);

		contactId = getContactId();
		transportId = getTransportId();
		// Create the transport keys
		tagKey = getSecretKey();
		headerKey = getSecretKey();
		streamNumber = 123;
		// Create a group
		ClientId clientId = getClientId();
		int majorVersion = 1234567890;
		byte[] descriptor = new byte[MAX_GROUP_DESCRIPTOR_LENGTH];
		Group group = groupFactory.createGroup(clientId, majorVersion,
				descriptor);
		// Add two messages to the group
		long timestamp = System.currentTimeMillis();
		byte[] body = "Hello world".getBytes("UTF-8");
		message = messageFactory.createMessage(group.getId(), timestamp, body);
		message1 = messageFactory.createMessage(group.getId(), timestamp, body);
		messageIds = Arrays.asList(message.getId(), message1.getId());
	}

	@Test
	public void testWriteAndRead() throws Exception {
		read(write());
	}

	private byte[] write() throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamContext ctx = new StreamContext(contactId, null, transportId,
				tagKey, headerKey, streamNumber, false);
		StreamWriter streamWriter = streamWriterFactory.createStreamWriter(out,
				ctx);
		SyncRecordWriter recordWriter = recordWriterFactory.createRecordWriter(
				streamWriter.getOutputStream());

		recordWriter.writeAck(new Ack(messageIds));
		recordWriter.writeMessage(message);
		recordWriter.writeMessage(message1);
		recordWriter.writeOffer(new Offer(messageIds));
		recordWriter.writeRequest(new Request(messageIds));

		streamWriter.sendEndOfStream();
		return out.toByteArray();
	}

	private void read(byte[] connectionData) throws Exception {
		// Calculate the expected tag
		byte[] expectedTag = new byte[TAG_LENGTH];
		transportCrypto.encodeTag(expectedTag, tagKey, PROTOCOL_VERSION,
				streamNumber);

		// Read the tag
		InputStream in = new ByteArrayInputStream(connectionData);
		byte[] tag = new byte[TAG_LENGTH];
		assertEquals(TAG_LENGTH, in.read(tag, 0, TAG_LENGTH));
		assertArrayEquals(expectedTag, tag);

		// Create the readers
		StreamContext ctx = new StreamContext(contactId, null, transportId,
				tagKey, headerKey, streamNumber, false);
		InputStream streamReader = streamReaderFactory.createStreamReader(in,
				ctx);
		SyncRecordReader recordReader = recordReaderFactory.createRecordReader(
				streamReader);

		// Read the ack
		assertTrue(recordReader.hasAck());
		Ack a = recordReader.readAck();
		assertEquals(messageIds, a.getMessageIds());

		// Read the messages
		assertTrue(recordReader.hasMessage());
		Message m = recordReader.readMessage();
		checkMessageEquality(message, m);
		assertTrue(recordReader.hasMessage());
		m = recordReader.readMessage();
		checkMessageEquality(message1, m);
		assertFalse(recordReader.hasMessage());

		// Read the offer
		assertTrue(recordReader.hasOffer());
		Offer o = recordReader.readOffer();
		assertEquals(messageIds, o.getMessageIds());

		// Read the request
		assertTrue(recordReader.hasRequest());
		Request req = recordReader.readRequest();
		assertEquals(messageIds, req.getMessageIds());

		in.close();
	}

	private void checkMessageEquality(Message m1, Message m2) {
		assertArrayEquals(m1.getId().getBytes(), m2.getId().getBytes());
		assertArrayEquals(m1.getGroupId().getBytes(),
				m2.getGroupId().getBytes());
		assertEquals(m1.getTimestamp(), m2.getTimestamp());
		assertEquals(m1.getRawLength(), m2.getRawLength());
		assertArrayEquals(m1.getBody(), m2.getBody());
	}
}
