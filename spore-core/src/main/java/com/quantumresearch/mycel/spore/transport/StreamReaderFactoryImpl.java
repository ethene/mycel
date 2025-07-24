package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.crypto.StreamDecrypterFactory;
import com.quantumresearch.mycel.spore.api.transport.StreamContext;
import com.quantumresearch.mycel.spore.api.transport.StreamReaderFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class StreamReaderFactoryImpl implements StreamReaderFactory {

	private final StreamDecrypterFactory streamDecrypterFactory;

	@Inject
	StreamReaderFactoryImpl(StreamDecrypterFactory streamDecrypterFactory) {
		this.streamDecrypterFactory = streamDecrypterFactory;
	}

	@Override
	public InputStream createStreamReader(InputStream in, StreamContext ctx) {
		return new StreamReaderImpl(streamDecrypterFactory
				.createStreamDecrypter(in, ctx));
	}

	@Override
	public InputStream createContactExchangeStreamReader(InputStream in,
			SecretKey headerKey) {
		return new StreamReaderImpl(streamDecrypterFactory
				.createContactExchangeStreamDecrypter(in, headerKey));
	}

	@Override
	public InputStream createLogStreamReader(InputStream in,
			SecretKey headerKey) {
		return new StreamReaderImpl(streamDecrypterFactory
				.createLogStreamDecrypter(in, headerKey));
	}
}
