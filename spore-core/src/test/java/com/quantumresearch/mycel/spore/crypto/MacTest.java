package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import com.quantumresearch.mycel.spore.test.TestSecureRandomProvider;
import org.junit.Test;

import java.util.Arrays;

import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.spore.test.TestUtils.getSecretKey;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MacTest extends BrambleTestCase {

	private final CryptoComponent crypto =
			new CryptoComponentImpl(new TestSecureRandomProvider(), null);

	private final SecretKey key1 = getSecretKey(), key2 = getSecretKey();
	private final String label1 = getRandomString(123);
	private final String label2 = getRandomString(123);
	private final byte[] input1 = getRandomBytes(123);
	private final byte[] input2 = getRandomBytes(234);
	private final byte[] input3 = new byte[0];

	@Test
	public void testIdenticalKeysAndInputsProduceIdenticalMacs() {
		// Calculate the MAC twice - the results should be identical
		byte[] mac = crypto.mac(label1, key1, input1, input2, input3);
		byte[] mac1 = crypto.mac(label1, key1, input1, input2, input3);
		assertArrayEquals(mac, mac1);
		assertTrue(crypto.verifyMac(mac, label1, key1, input1, input2, input3));
	}

	@Test
	public void testDifferentLabelsProduceDifferentMacs() {
		// Calculate the MAC with each label - the results should be different
		byte[] mac = crypto.mac(label1, key1, input1, input2, input3);
		byte[] mac1 = crypto.mac(label2, key1, input1, input2, input3);
		assertFalse(Arrays.equals(mac, mac1));
		// Each MAC should fail to verify with the other MAC's label
		assertFalse(crypto.verifyMac(mac, label2, key1, input1, input2,
				input3));
		assertFalse(crypto.verifyMac(mac1, label1, key2, input1, input2,
				input3));
	}

	@Test
	public void testDifferentKeysProduceDifferentMacs() {
		// Calculate the MAC with each key - the results should be different
		byte[] mac = crypto.mac(label1, key1, input1, input2, input3);
		byte[] mac1 = crypto.mac(label1, key2, input1, input2, input3);
		assertFalse(Arrays.equals(mac, mac1));
		// Each MAC should fail to verify with the other MAC's key
		assertFalse(crypto.verifyMac(mac, label1, key2, input1, input2,
				input3));
		assertFalse(crypto.verifyMac(mac1, label2, key1, input1, input2,
				input3));
	}

	@Test
	public void testDifferentInputsProduceDifferentMacs() {
		// Calculate the MAC with the inputs in different orders - the results
		// should be different
		byte[] mac = crypto.mac(label1, key1, input1, input2, input3);
		byte[] mac1 = crypto.mac(label1, key1, input3, input2, input1);
		assertFalse(Arrays.equals(mac, mac1));
		// Each MAC should fail to verify with the other MAC's inputs
		assertFalse(crypto.verifyMac(mac, label1, key2, input3, input2,
				input1));
		assertFalse(crypto.verifyMac(mac1, label1, key1, input1, input2,
				input3));
	}

}
