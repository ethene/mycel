package com.quantumresearch.mycel.spore.record;

import com.quantumresearch.mycel.spore.api.record.Record;
import com.quantumresearch.mycel.spore.api.record.RecordWriter;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import com.quantumresearch.mycel.spore.util.ByteUtils;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static com.quantumresearch.mycel.spore.api.record.Record.MAX_RECORD_PAYLOAD_BYTES;
import static com.quantumresearch.mycel.spore.api.record.Record.RECORD_HEADER_BYTES;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomBytes;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class RecordWriterImplTest extends BrambleTestCase {

	@Test
	public void testWritesEmptyRecord() throws Exception {
		testWritesRecord(0);
	}

	@Test
	public void testWritesMaxLengthRecord() throws Exception {
		testWritesRecord(MAX_RECORD_PAYLOAD_BYTES);
	}

	private void testWritesRecord(int payloadLength) throws Exception {
		byte protocolVersion = 123;
		byte recordType = 45;
		byte[] payload = getRandomBytes(payloadLength);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		RecordWriter writer = new RecordWriterImpl(out);
		writer.writeRecord(new Record(protocolVersion, recordType, payload));
		writer.flush();
		byte[] written = out.toByteArray();

		assertEquals(RECORD_HEADER_BYTES + payloadLength, written.length);
		assertEquals(protocolVersion, written[0]);
		assertEquals(recordType, written[1]);
		assertEquals(payloadLength, ByteUtils.readUint16(written, 2));
		byte[] writtenPayload = new byte[payloadLength];
		System.arraycopy(written, RECORD_HEADER_BYTES, writtenPayload, 0,
				payloadLength);
		assertArrayEquals(payload, writtenPayload);
	}
}
