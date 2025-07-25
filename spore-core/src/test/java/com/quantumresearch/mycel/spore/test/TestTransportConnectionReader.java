package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@NotNullByDefault
public class TestTransportConnectionReader
		implements TransportConnectionReader {

	private final InputStream in;

	public TestTransportConnectionReader(InputStream in) {
		this.in = in;
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public void dispose(boolean exception, boolean recognised)
			throws IOException {
		in.close();
	}
}
