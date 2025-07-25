package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.crypto.StreamEncrypterFactory;
import com.quantumresearch.mycel.spore.api.transport.StreamContext;
import com.quantumresearch.mycel.spore.api.transport.StreamWriter;
import com.quantumresearch.mycel.spore.api.transport.StreamWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class StreamWriterFactoryImpl implements StreamWriterFactory {

	private final StreamEncrypterFactory streamEncrypterFactory;

	@Inject
	StreamWriterFactoryImpl(StreamEncrypterFactory streamEncrypterFactory) {
		this.streamEncrypterFactory = streamEncrypterFactory;
	}

	@Override
	public StreamWriter createStreamWriter(OutputStream out,
			StreamContext ctx) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createStreamEncrypter(out, ctx));
	}

	@Override
	public StreamWriter createContactExchangeStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createContactExchangeStreamEncrypter(out, headerKey));
	}

	@Override
	public StreamWriter createLogStreamWriter(OutputStream out,
			SecretKey headerKey) {
		return new StreamWriterImpl(streamEncrypterFactory
				.createLogStreamEncrypter(out, headerKey));
	}
}
