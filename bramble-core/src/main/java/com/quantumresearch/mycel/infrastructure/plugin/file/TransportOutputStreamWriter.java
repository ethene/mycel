package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.simplex.SimplexPlugin;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.infrastructure.util.IoUtils.tryToClose;

@NotNullByDefault
class TransportOutputStreamWriter implements TransportConnectionWriter {

	private static final Logger LOG =
			getLogger(TransportOutputStreamWriter.class.getName());

	private final SimplexPlugin plugin;
	private final OutputStream out;

	TransportOutputStreamWriter(SimplexPlugin plugin, OutputStream out) {
		this.plugin = plugin;
		this.out = out;
	}

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
		return plugin.isLossyAndCheap();
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public void dispose(boolean exception) {
		tryToClose(out, LOG, WARNING);
	}
}
