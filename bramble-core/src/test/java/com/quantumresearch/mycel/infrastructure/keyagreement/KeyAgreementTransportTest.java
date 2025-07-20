package com.quantumresearch.mycel.infrastructure.keyagreement;

import com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConnection;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.record.Record;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReader;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReader.RecordPredicate;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriter;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriterFactory;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import com.quantumresearch.mycel.infrastructure.test.CaptureArgumentAction;
import com.quantumresearch.mycel.infrastructure.test.PredicateMatcher;
import org.jmock.Expectations;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;

import static com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConstants.PROTOCOL_VERSION;
import static com.quantumresearch.mycel.infrastructure.api.keyagreement.RecordTypes.ABORT;
import static com.quantumresearch.mycel.infrastructure.api.keyagreement.RecordTypes.CONFIRM;
import static com.quantumresearch.mycel.infrastructure.api.keyagreement.RecordTypes.KEY;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getTransportId;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KeyAgreementTransportTest extends BrambleMockTestCase {

	private final DuplexTransportConnection duplexTransportConnection =
			context.mock(DuplexTransportConnection.class);
	private final TransportConnectionReader transportConnectionReader =
			context.mock(TransportConnectionReader.class);
	private final TransportConnectionWriter transportConnectionWriter =
			context.mock(TransportConnectionWriter.class);
	private final RecordReaderFactory recordReaderFactory =
			context.mock(RecordReaderFactory.class);
	private final RecordWriterFactory recordWriterFactory =
			context.mock(RecordWriterFactory.class);
	private final RecordReader recordReader = context.mock(RecordReader.class);
	private final RecordWriter recordWriter = context.mock(RecordWriter.class);

	private final TransportId transportId = getTransportId();
	private final KeyAgreementConnection keyAgreementConnection =
			new KeyAgreementConnection(duplexTransportConnection, transportId);

	private final InputStream inputStream =
			new ByteArrayInputStream(new byte[0]);
	private final OutputStream outputStream = new ByteArrayOutputStream();

	private KeyAgreementTransport kat;

	@Test
	public void testSendKey() throws Exception {
		byte[] key = getRandomBytes(123);

		setup();
		AtomicReference<Record> written = expectWriteRecord();

		kat.sendKey(key);
		assertNotNull(written.get());
		assertRecordEquals(KEY, key, written.get());
	}

	@Test
	public void testSendConfirm() throws Exception {
		byte[] confirm = getRandomBytes(123);

		setup();
		AtomicReference<Record> written = expectWriteRecord();

		kat.sendConfirm(confirm);
		assertNotNull(written.get());
		assertRecordEquals(CONFIRM, confirm, written.get());
	}

	@Test
	public void testSendAbortWithException() throws Exception {
		setup();
		AtomicReference<Record> written = expectWriteRecord();
		context.checking(new Expectations() {{
			oneOf(transportConnectionReader).dispose(true, true);
			oneOf(transportConnectionWriter).dispose(true);
		}});

		kat.sendAbort(true);
		assertNotNull(written.get());
		assertRecordEquals(ABORT, new byte[0], written.get());
	}

	@Test
	public void testSendAbortWithoutException() throws Exception {
		setup();
		AtomicReference<Record> written = expectWriteRecord();
		context.checking(new Expectations() {{
			oneOf(transportConnectionReader).dispose(false, true);
			oneOf(transportConnectionWriter).dispose(false);
		}});

		kat.sendAbort(false);
		assertNotNull(written.get());
		assertRecordEquals(ABORT, new byte[0], written.get());
	}

	@Test(expected = AbortException.class)
	public void testReceiveKeyThrowsExceptionIfAtEndOfStream()
			throws Exception {
		setup();
		expectReadEof();

		kat.receiveKey();
	}

	@Test(expected = AbortException.class)
	public void testReceiveKeyThrowsExceptionIfAbortIsReceived()
			throws Exception {
		setup();
		expectReadRecord(new Record(PROTOCOL_VERSION, ABORT, new byte[0]));

		kat.receiveKey();
	}

	@Test(expected = AbortException.class)
	public void testReceiveKeyThrowsExceptionIfConfirmIsReceived()
			throws Exception {
		byte[] confirm = getRandomBytes(123);

		setup();
		expectReadRecord(new Record(PROTOCOL_VERSION, CONFIRM, confirm));

		kat.receiveKey();
	}

	@Test(expected = AbortException.class)
	public void testReceiveConfirmThrowsExceptionIfAtEndOfStream()
			throws Exception {
		setup();
		expectReadEof();

		kat.receiveConfirm();
	}

	@Test(expected = AbortException.class)
	public void testReceiveConfirmThrowsExceptionIfAbortIsReceived()
			throws Exception {
		setup();
		expectReadRecord(new Record(PROTOCOL_VERSION, ABORT, new byte[0]));

		kat.receiveConfirm();
	}

	@Test(expected = AbortException.class)
	public void testReceiveConfirmThrowsExceptionIfKeyIsReceived()
			throws Exception {
		byte[] key = getRandomBytes(123);

		setup();
		expectReadRecord(new Record(PROTOCOL_VERSION, KEY, key));

		kat.receiveConfirm();
	}

	private void setup() throws Exception {
		context.checking(new Expectations() {{
			allowing(duplexTransportConnection).getReader();
			will(returnValue(transportConnectionReader));
			allowing(transportConnectionReader).getInputStream();
			will(returnValue(inputStream));
			oneOf(recordReaderFactory).createRecordReader(inputStream);
			will(returnValue(recordReader));
			allowing(duplexTransportConnection).getWriter();
			will(returnValue(transportConnectionWriter));
			allowing(transportConnectionWriter).getOutputStream();
			will(returnValue(outputStream));
			oneOf(recordWriterFactory).createRecordWriter(outputStream);
			will(returnValue(recordWriter));
		}});
		kat = new KeyAgreementTransport(recordReaderFactory,
				recordWriterFactory, keyAgreementConnection);
	}

	private AtomicReference<Record> expectWriteRecord() throws Exception {
		AtomicReference<Record> captured = new AtomicReference<>();
		context.checking(new Expectations() {{
			oneOf(recordWriter).writeRecord(with(any(Record.class)));
			will(new CaptureArgumentAction<>(captured, Record.class, 0));
			oneOf(recordWriter).flush();
		}});
		return captured;
	}

	private void assertRecordEquals(byte expectedType,
			byte[] expectedPayload, Record actual) {
		assertEquals(PROTOCOL_VERSION, actual.getProtocolVersion());
		assertEquals(expectedType, actual.getRecordType());
		assertArrayEquals(expectedPayload, actual.getPayload());
	}

	private void expectReadRecord(Record record) throws Exception {
		context.checking(new Expectations() {{
			// Test that the `accept` predicate passed to the reader would
			// accept the expected record
			oneOf(recordReader).readRecord(with(new PredicateMatcher<>(
							RecordPredicate.class, rp -> rp.test(record))),
					with(any(RecordPredicate.class)));
			will(returnValue(record));
		}});
	}

	private void expectReadEof() throws Exception {
		context.checking(new Expectations() {{
			oneOf(recordReader).readRecord(with(any(RecordPredicate.class)),
					with(any(RecordPredicate.class)));
			will(returnValue(null));
		}});
	}
}
