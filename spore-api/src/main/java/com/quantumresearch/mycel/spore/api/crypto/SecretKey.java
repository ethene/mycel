package com.quantumresearch.mycel.spore.api.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;

/**
 * A secret key used for encryption and/or authentication.
 */
public class SecretKey extends Bytes {

	/**
	 * The length of a secret key in bytes.
	 */
	public static final int LENGTH = 32;

	public SecretKey(byte[] key) {
		super(key);
		if (key.length != LENGTH) throw new IllegalArgumentException();
	}
}
