package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.StreamDecrypter;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import static com.quantumresearch.mycel.spore.api.transport.TransportConstants.MAX_PAYLOAD_LENGTH;
import static org.junit.Assert.assertEquals;

public class StreamReaderImplTest extends BrambleMockTestCase {

	@Test
	public void testEmptyFramesAreSkipped() throws Exception {
		StreamDecrypter decrypter = context.mock(StreamDecrypter.class);
		context.checking(new Expectations() {{
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(0)); // Empty frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(2)); // Non-empty frame with two payload bytes
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(0)); // Empty frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(-1)); // No more frames
		}});
		StreamReaderImpl r = new StreamReaderImpl(decrypter);
		assertEquals(0, r.read()); // Skip the first empty frame, read a byte
		assertEquals(0, r.read()); // Read another byte
		assertEquals(-1, r.read()); // Skip the second empty frame, reach EOF
		assertEquals(-1, r.read()); // Still at EOF
		r.close();
	}

	@Test
	public void testEmptyFramesAreSkippedWithBuffer() throws Exception {
		StreamDecrypter decrypter = context.mock(StreamDecrypter.class);
		context.checking(new Expectations() {{
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(0)); // Empty frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(2)); // Non-empty frame with two payload bytes
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(0)); // Empty frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(-1)); // No more frames
		}});
		StreamReaderImpl r = new StreamReaderImpl(decrypter);
		byte[] buf = new byte[MAX_PAYLOAD_LENGTH];
		// Skip the first empty frame, read the two payload bytes
		assertEquals(2, r.read(buf));
		// Skip the second empty frame, reach EOF
		assertEquals(-1, r.read(buf));
		// Still at EOF
		assertEquals(-1, r.read(buf));
		r.close();
	}

	@Test
	public void testMultipleReadsPerFrame() throws Exception {
		StreamDecrypter decrypter = context.mock(StreamDecrypter.class);
		context.checking(new Expectations() {{
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(MAX_PAYLOAD_LENGTH)); // Nice long frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(-1)); // No more frames
		}});
		StreamReaderImpl r = new StreamReaderImpl(decrypter);
		byte[] buf = new byte[MAX_PAYLOAD_LENGTH / 2];
		// Read the first half of the payload
		assertEquals(MAX_PAYLOAD_LENGTH / 2, r.read(buf));
		// Read the second half of the payload
		assertEquals(MAX_PAYLOAD_LENGTH / 2, r.read(buf));
		// Reach EOF
		assertEquals(-1, r.read(buf, 0, buf.length));
		r.close();
	}

	@Test
	public void testMultipleReadsPerFrameWithOffsets() throws Exception {
		StreamDecrypter decrypter = context.mock(StreamDecrypter.class);
		context.checking(new Expectations() {{
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(MAX_PAYLOAD_LENGTH)); // Nice long frame
			oneOf(decrypter).readFrame(with(any(byte[].class)));
			will(returnValue(-1)); // No more frames
		}});
		StreamReaderImpl r = new StreamReaderImpl(decrypter);
		byte[] buf = new byte[MAX_PAYLOAD_LENGTH];
		// Read the first half of the payload
		assertEquals(MAX_PAYLOAD_LENGTH / 2, r.read(buf, MAX_PAYLOAD_LENGTH / 2,
				MAX_PAYLOAD_LENGTH / 2));
		// Read the second half of the payload
		assertEquals(MAX_PAYLOAD_LENGTH / 2, r.read(buf, 123,
				MAX_PAYLOAD_LENGTH / 2));
		// Reach EOF
		assertEquals(-1, r.read(buf, 0, buf.length));
		r.close();
	}
}
