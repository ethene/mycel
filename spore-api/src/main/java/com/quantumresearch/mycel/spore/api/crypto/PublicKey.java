package com.quantumresearch.mycel.spore.api.crypto;

import org.briarproject.nullsafety.NotNullByDefault;

/**
 * The public half of a public/private {@link KeyPair}.
 */
@NotNullByDefault
public interface PublicKey {

	/**
	 * Returns the type of this key pair.
	 */
	String getKeyType();

	/**
	 * Returns the encoded representation of this key.
	 */
	byte[] getEncoded();
}
