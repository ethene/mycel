package com.quantumresearch.mycel.infrastructure.api.crypto;

import org.briarproject.nullsafety.NotNullByDefault;

/**
 * The private half of a public/private {@link KeyPair}.
 */
@NotNullByDefault
public interface PrivateKey {

	/**
	 * Returns the type of this key pair.
	 */
	String getKeyType();

	/**
	 * Returns the encoded representation of this key.
	 */
	byte[] getEncoded();
}
