package com.quantumresearch.mycel.spore.api.plugin.duplex;

import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

@NotNullByDefault
public abstract class AbstractDuplexTransportConnection
		implements DuplexTransportConnection {

	protected final TransportProperties remote = new TransportProperties();

	private final Plugin plugin;
	private final Reader reader;
	private final Writer writer;
	private final AtomicBoolean halfClosed, closed;

	protected AbstractDuplexTransportConnection(Plugin plugin) {
		this.plugin = plugin;
		reader = new Reader();
		writer = new Writer();
		halfClosed = new AtomicBoolean(false);
		closed = new AtomicBoolean(false);
	}

	protected abstract InputStream getInputStream() throws IOException;

	protected abstract OutputStream getOutputStream() throws IOException;

	protected abstract void closeConnection(boolean exception)
			throws IOException;

	@Override
	public TransportConnectionReader getReader() {
		return reader;
	}

	@Override
	public TransportConnectionWriter getWriter() {
		return writer;
	}

	@Override
	public TransportProperties getRemoteProperties() {
		return remote;
	}

	private class Reader implements TransportConnectionReader {

		@Override
		public InputStream getInputStream() throws IOException {
			return AbstractDuplexTransportConnection.this.getInputStream();
		}

		@Override
		public void dispose(boolean exception, boolean recognised)
				throws IOException {
			if (halfClosed.getAndSet(true) || exception || !recognised)
				if (!closed.getAndSet(true)) closeConnection(exception);
		}
	}

	private class Writer implements TransportConnectionWriter {

		@Override
		public long getMaxLatency() {
			return plugin.getMaxLatency();
		}

		@Override
		public int getMaxIdleTime() {
			return plugin.getMaxIdleTime();
		}

		@Override
		public boolean isLossyAndCheap() {
			return false;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return AbstractDuplexTransportConnection.this.getOutputStream();
		}

		@Override
		public void dispose(boolean exception) throws IOException {
			if (halfClosed.getAndSet(true) || exception)
				if (!closed.getAndSet(true)) closeConnection(exception);
		}
	}
}
