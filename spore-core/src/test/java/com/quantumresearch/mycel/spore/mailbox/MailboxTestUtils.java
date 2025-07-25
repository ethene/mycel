package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.api.WeakSingletonProvider;

import java.nio.ByteBuffer;

import javax.annotation.Nonnull;
import javax.net.SocketFactory;

import okhttp3.OkHttpClient;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.QR_FORMAT_ID;
import static com.quantumresearch.mycel.spore.api.mailbox.MailboxConstants.QR_FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomId;
import static com.quantumresearch.mycel.spore.util.StringUtils.ISO_8859_1;

class MailboxTestUtils {

	static String getQrCodePayload(byte[] onionBytes, byte[] setupToken) {
		int formatIdAndVersion = (QR_FORMAT_ID << 5) | QR_FORMAT_VERSION;
		byte[] payloadBytes = ByteBuffer.allocate(65)
				.put((byte) formatIdAndVersion) // 1
				.put(onionBytes) // 32
				.put(setupToken) // 32
				.array();
		return new String(payloadBytes, ISO_8859_1);
	}

	// Used by mailbox integration tests
	static String getQrCodePayload(byte[] setupToken) {
		return getQrCodePayload(getRandomId(), setupToken);
	}

	static WeakSingletonProvider<OkHttpClient> createHttpClientProvider() {
		return new WeakSingletonProvider<OkHttpClient>() {
			@Override
			@Nonnull
			public OkHttpClient createInstance() {
				return new OkHttpClient.Builder()
						.socketFactory(SocketFactory.getDefault())
						.connectTimeout(60_000, MILLISECONDS)
						.build();
			}
		};
	}
}
