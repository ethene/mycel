package com.quantumresearch.mycel.infrastructure.plugin.tor;

import com.quantumresearch.mycel.infrastructure.api.plugin.Plugin;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.AbstractDuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.util.IoUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@NotNullByDefault
class TorTransportConnection extends AbstractDuplexTransportConnection {

	private final Socket socket;

	TorTransportConnection(Plugin plugin, Socket socket) {
		super(plugin);
		this.socket = socket;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return IoUtils.getInputStream(socket);
	}

	@Override
	protected OutputStream getOutputStream() throws IOException {
		return IoUtils.getOutputStream(socket);
	}

	@Override
	protected void closeConnection(boolean exception) throws IOException {
		socket.close();
	}
}
