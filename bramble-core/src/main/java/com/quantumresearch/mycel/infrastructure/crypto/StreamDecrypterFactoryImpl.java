package com.quantumresearch.mycel.infrastructure.crypto;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.StreamDecrypter;
import com.quantumresearch.mycel.infrastructure.api.crypto.StreamDecrypterFactory;
import com.quantumresearch.mycel.infrastructure.api.transport.StreamContext;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import javax.inject.Provider;

@Immutable
@NotNullByDefault
class StreamDecrypterFactoryImpl implements StreamDecrypterFactory {

	private final Provider<AuthenticatedCipher> cipherProvider;

	@Inject
	StreamDecrypterFactoryImpl(Provider<AuthenticatedCipher> cipherProvider) {
		this.cipherProvider = cipherProvider;
	}

	@Override
	public StreamDecrypter createStreamDecrypter(InputStream in,
			StreamContext ctx) {
		AuthenticatedCipher cipher = cipherProvider.get();
		return new StreamDecrypterImpl(in, cipher, ctx.getStreamNumber(),
				ctx.getHeaderKey());
	}

	@Override
	public StreamDecrypter createContactExchangeStreamDecrypter(InputStream in,
			SecretKey headerKey) {
		return new StreamDecrypterImpl(in, cipherProvider.get(), 0, headerKey);
	}

	@Override
	public StreamDecrypter createLogStreamDecrypter(InputStream in,
			SecretKey headerKey) {
		return createContactExchangeStreamDecrypter(in, headerKey);
	}
}
