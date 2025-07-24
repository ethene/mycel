package com.quantumresearch.mycel.spore.data;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Random;

import static com.quantumresearch.mycel.spore.api.data.BdfReader.DEFAULT_MAX_BUFFER_SIZE;
import static com.quantumresearch.mycel.spore.api.data.BdfReader.DEFAULT_NESTED_LIMIT;
import static com.quantumresearch.mycel.spore.test.TestUtils.isOptionalTestEnabled;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class BdfReaderImplFuzzingTest extends BrambleTestCase {

	@Before
	public void setUp() {
		assumeTrue(isOptionalTestEnabled(BdfReaderImplFuzzingTest.class));
	}

	@Test
	public void testStringFuzzing() throws Exception {
		Random random = new Random();
		byte[] buf = new byte[22];
		ByteArrayInputStream in = new ByteArrayInputStream(buf);
		for (int i = 0; i < 100_000_000; i++) {
			random.nextBytes(buf);
			buf[0] = 0x41; // String with 1-byte length
			buf[1] = 0x14; // Length 20 bytes
			in.reset();
			BdfReaderImpl r = new BdfReaderImpl(in, DEFAULT_NESTED_LIMIT,
					DEFAULT_MAX_BUFFER_SIZE, true);
			try {
				int length = r.readString().length();
				assertTrue(length <= 20);
				assertTrue(r.eof());
			} catch (FormatException e) {
				// Expected when bytes are not valid UTF-8
			}
		}
	}
}
