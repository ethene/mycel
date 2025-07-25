package com.quantumresearch.mycel.spore.api.transport;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

@NotNullByDefault
public interface StreamReaderFactory {

	/**
	 * Creates an {@link InputStream InputStream} for reading from a
	 * transport stream.
	 */
	InputStream createStreamReader(InputStream in, StreamContext ctx);

	/**
	 * Creates an {@link InputStream InputStream} for reading from a contact
	 * exchange stream.
	 */
	InputStream createContactExchangeStreamReader(InputStream in,
			SecretKey headerKey);

	/**
	 * Creates an {@link InputStream} for reading from a log stream.
	 */
	InputStream createLogStreamReader(InputStream in, SecretKey headerKey);
}
