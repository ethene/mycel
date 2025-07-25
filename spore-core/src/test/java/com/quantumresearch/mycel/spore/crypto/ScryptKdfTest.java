package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.system.SystemClock;
import com.quantumresearch.mycel.spore.test.ArrayClock;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertTrue;
import static com.quantumresearch.mycel.spore.test.TestUtils.getRandomBytes;
import static com.quantumresearch.mycel.spore.util.StringUtils.getRandomString;
import static org.junit.Assert.assertEquals;

public class ScryptKdfTest extends BrambleTestCase {

	@Test
	public void testPasswordAffectsKey() throws Exception {
		PasswordBasedKdf kdf = new ScryptKdf(new SystemClock());
		byte[] salt = getRandomBytes(32);
		Set<Bytes> keys = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			String password = getRandomString(16);
			SecretKey key = kdf.deriveKey(password, salt, 256);
			assertTrue(keys.add(new Bytes(key.getBytes())));
		}
	}

	@Test
	public void testSaltAffectsKey() throws Exception {
		PasswordBasedKdf kdf = new ScryptKdf(new SystemClock());
		String password = getRandomString(16);
		Set<Bytes> keys = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			byte[] salt = getRandomBytes(32);
			SecretKey key = kdf.deriveKey(password, salt, 256);
			assertTrue(keys.add(new Bytes(key.getBytes())));
		}
	}

	@Test
	public void testCostParameterAffectsKey() throws Exception {
		PasswordBasedKdf kdf = new ScryptKdf(new SystemClock());
		String password = getRandomString(16);
		byte[] salt = getRandomBytes(32);
		Set<Bytes> keys = new HashSet<>();
		for (int cost = 2; cost <= 256; cost *= 2) {
			SecretKey key = kdf.deriveKey(password, salt, cost);
			assertTrue(keys.add(new Bytes(key.getBytes())));
		}
	}

	@Test
	public void testCalibration() throws Exception {
		Clock clock = new ArrayClock(
				0, 50, // Duration for cost 256
				0, 100, // Duration for cost 512
				0, 200, // Duration for cost 1024
				0, 400, // Duration for cost 2048
				0, 800 // Duration for cost 4096
		);
		PasswordBasedKdf kdf = new ScryptKdf(clock);
		assertEquals(4096, kdf.chooseCostParameter());
	}

	@Test
	public void testCalibrationChoosesMinCost() throws Exception {
		Clock clock = new ArrayClock(
				0, 2000 // Duration for cost 256 is already too high
		);
		PasswordBasedKdf kdf = new ScryptKdf(clock);
		assertEquals(256, kdf.chooseCostParameter());
	}
}
