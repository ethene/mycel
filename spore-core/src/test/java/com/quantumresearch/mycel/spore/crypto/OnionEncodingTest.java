package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import com.quantumresearch.mycel.spore.test.TestSecureRandomProvider;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class OnionEncodingTest extends BrambleTestCase {

	private static final Pattern ONION_V3 = Pattern.compile("[a-z2-7]{56}");

	private final CryptoComponent crypto =
			new CryptoComponentImpl(new TestSecureRandomProvider(), null);
	private final SecureRandom secureRandom = new SecureRandom();

	@Test
	public void testHostnameIsValid() {
		byte[] publicKey = new byte[32];
		for (int i = 0; i < 100; i++) {
			secureRandom.nextBytes(publicKey);
			String onion = crypto.encodeOnion(publicKey);
			assertTrue(onion, ONION_V3.matcher(onion).matches());
		}
	}
}
