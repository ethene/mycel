package com.quantumresearch.mycel.spore.api.transport;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

@NotNullByDefault
public interface StreamWriterFactory {
	/**
	 * Creates a {@link StreamWriter} for writing to a transport stream.
	 */
	StreamWriter createStreamWriter(OutputStream out, StreamContext ctx);

	/**
	 * Creates a {@link StreamWriter} for writing to a contact exchange stream.
	 */
	StreamWriter createContactExchangeStreamWriter(OutputStream out,
			SecretKey headerKey);

	/**
	 * Creates a {@link StreamWriter} for writing to a log stream.
	 */
	StreamWriter createLogStreamWriter(OutputStream out, SecretKey headerKey);
}
