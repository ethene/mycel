package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.StreamEncrypter;
import com.quantumresearch.mycel.spore.test.BrambleMockTestCase;
import org.jmock.Expectations;
import org.junit.Test;

import static com.quantumresearch.mycel.spore.api.transport.TransportConstants.MAX_PAYLOAD_LENGTH;
import static org.junit.Assert.assertEquals;

public class StreamWriterImplTest extends BrambleMockTestCase {

	@Test
	public void testCloseWithoutWritingWritesFinalFrame() throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		context.checking(new Expectations() {{
			// Write an empty final frame
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(true));
			// Flush the stream
			oneOf(encrypter).flush();
		}});
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		w.close();
	}

	@Test
	public void testFlushWithoutBufferedDataWritesFrameAndFlushes()
			throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		context.checking(new Expectations() {{
			// Write a non-final frame with an empty payload
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(false));
			// Flush the stream
			oneOf(encrypter).flush();
		}});
		w.flush();
		context.assertIsSatisfied();

		// Clean up
		context.checking(new Expectations() {{
			// Closing the writer writes a final frame and flushes again
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(true));
			oneOf(encrypter).flush();
		}});
		w.close();
	}

	@Test
	public void testFlushWithBufferedDataWritesFrameAndFlushes()
			throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		context.checking(new Expectations() {{
			// Write a non-final frame with one payload byte
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(1),
					with(0), with(false));
			// Flush the stream
			oneOf(encrypter).flush();
		}});
		w.write(0);
		w.flush();
		context.assertIsSatisfied();

		// Clean up
		context.checking(new Expectations() {{
			// Closing the writer writes a final frame and flushes again
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(true));
			oneOf(encrypter).flush();
		}});
		w.close();
	}

	@Test
	public void testSingleByteWritesWriteFullFrame() throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		context.checking(new Expectations() {{
			// Write a full non-final frame
			oneOf(encrypter).writeFrame(with(any(byte[].class)),
					with(MAX_PAYLOAD_LENGTH), with(0), with(false));
		}});
		for (int i = 0; i < MAX_PAYLOAD_LENGTH; i++) w.write(0);
		context.assertIsSatisfied();

		// Clean up
		context.checking(new Expectations() {{
			// Closing the writer writes a final frame and flushes again
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(true));
			oneOf(encrypter).flush();
		}});
		w.close();
	}

	@Test
	public void testMultiByteWritesWriteFullFrames() throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		context.checking(new Expectations() {{
			// Write two full non-final frames
			exactly(2).of(encrypter).writeFrame(with(any(byte[].class)),
					with(MAX_PAYLOAD_LENGTH), with(0), with(false));
		}});
		// Sanity check
		assertEquals(0, MAX_PAYLOAD_LENGTH % 2);
		// Write two full payloads using four multi-byte writes
		byte[] b = new byte[MAX_PAYLOAD_LENGTH / 2];
		w.write(b);
		w.write(b);
		w.write(b);
		w.write(b);
		context.assertIsSatisfied();

		// Clean up
		context.checking(new Expectations() {{
			// Closing the writer writes a final frame and flushes again
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(0),
					with(0), with(true));
			oneOf(encrypter).flush();
		}});
		w.close();
	}

	@Test
	public void testLargeMultiByteWriteWritesFullFrames() throws Exception {
		StreamEncrypter encrypter = context.mock(StreamEncrypter.class);
		StreamWriterImpl w = new StreamWriterImpl(encrypter);
		context.checking(new Expectations() {{
			// Write two full non-final frames
			exactly(2).of(encrypter).writeFrame(with(any(byte[].class)),
					with(MAX_PAYLOAD_LENGTH), with(0), with(false));
			// Write a final frame with a one-byte payload
			oneOf(encrypter).writeFrame(with(any(byte[].class)), with(1),
					with(0), with(true));
			// Flush the stream
			oneOf(encrypter).flush();
		}});
		// Write two full payloads using one large multi-byte write
		byte[] b = new byte[MAX_PAYLOAD_LENGTH * 2 + 1];
		w.write(b);
		// There should be one byte left in the buffer
		w.close();
	}
}
