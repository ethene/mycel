package com.quantumresearch.mycel.spore.connection;

import com.quantumresearch.mycel.spore.api.connection.ConnectionRegistry;
import com.quantumresearch.mycel.spore.api.db.DbException;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.spore.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.transport.KeyManager;
import com.quantumresearch.mycel.spore.api.transport.StreamContext;
import com.quantumresearch.mycel.spore.api.transport.StreamReaderFactory;
import com.quantumresearch.mycel.spore.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.api.transport.TransportConstants.TAG_LENGTH;
import static com.quantumresearch.mycel.spore.util.IoUtils.read;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;

@NotNullByDefault
abstract class Connection {

	protected static final Logger LOG = getLogger(Connection.class.getName());

	final KeyManager keyManager;
	final ConnectionRegistry connectionRegistry;
	final StreamReaderFactory streamReaderFactory;
	final StreamWriterFactory streamWriterFactory;

	Connection(KeyManager keyManager, ConnectionRegistry connectionRegistry,
			StreamReaderFactory streamReaderFactory,
			StreamWriterFactory streamWriterFactory) {
		this.keyManager = keyManager;
		this.connectionRegistry = connectionRegistry;
		this.streamReaderFactory = streamReaderFactory;
		this.streamWriterFactory = streamWriterFactory;
	}

	@Nullable
	StreamContext recogniseTag(TransportConnectionReader reader,
			TransportId transportId) {
		try {
			byte[] tag = readTag(reader.getInputStream());
			return keyManager.getStreamContext(transportId, tag);
		} catch (IOException | DbException e) {
			logException(LOG, WARNING, e);
			return null;
		}
	}

	byte[] readTag(InputStream in) throws IOException {
		byte[] tag = new byte[TAG_LENGTH];
		read(in, tag);
		return tag;
	}

	void disposeOnError(TransportConnectionReader reader, boolean recognised) {
		try {
			reader.dispose(true, recognised);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}

	void disposeOnError(TransportConnectionWriter writer) {
		try {
			writer.dispose(true);
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}
}
