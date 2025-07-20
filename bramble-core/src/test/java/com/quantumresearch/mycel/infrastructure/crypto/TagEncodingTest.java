package com.quantumresearch.mycel.infrastructure.crypto;

import com.quantumresearch.mycel.infrastructure.api.Bytes;
import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.infrastructure.test.BrambleMockTestCase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.PROTOCOL_VERSION;
import static com.quantumresearch.mycel.infrastructure.api.transport.TransportConstants.TAG_LENGTH;
import static com.quantumresearch.mycel.infrastructure.test.TestUtils.getSecretKey;

public class TagEncodingTest extends BrambleMockTestCase {

	private final CryptoComponent crypto = context.mock(CryptoComponent.class);

	private final TransportCrypto transportCrypto =
			new TransportCryptoImpl(crypto);
	private final SecretKey tagKey = getSecretKey();
	private final long streamNumber = 1234567890;

	@Test
	public void testKeyAffectsTag() throws Exception {
		Set<Bytes> set = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			byte[] tag = new byte[TAG_LENGTH];
			SecretKey tagKey = getSecretKey();
			transportCrypto.encodeTag(tag, tagKey, PROTOCOL_VERSION,
					streamNumber);
			assertTrue(set.add(new Bytes(tag)));
		}
	}

	@Test
	public void testProtocolVersionAffectsTag() throws Exception {
		Set<Bytes> set = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			byte[] tag = new byte[TAG_LENGTH];
			transportCrypto.encodeTag(tag, tagKey, PROTOCOL_VERSION + i,
					streamNumber);
			assertTrue(set.add(new Bytes(tag)));
		}
	}

	@Test
	public void testStreamNumberAffectsTag() throws Exception {
		Set<Bytes> set = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			byte[] tag = new byte[TAG_LENGTH];
			transportCrypto.encodeTag(tag, tagKey, PROTOCOL_VERSION,
					streamNumber + i);
			assertTrue(set.add(new Bytes(tag)));
		}
	}
}
