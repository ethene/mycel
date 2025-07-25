package com.quantumresearch.mycel.spore.contact;

import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface ContactExchangeCrypto {

	/**
	 * Derives the header key for a contact exchange stream from the master key.
	 *
	 * @param alice Whether the header key is for the stream sent by Alice
	 */
	SecretKey deriveHeaderKey(SecretKey masterKey, boolean alice);

	/**
	 * Creates and returns a signature that proves ownership of a pseudonym.
	 *
	 * @param privateKey The pseudonym's signature private key
	 * @param alice Whether the pseudonym belongs to Alice
	 */
	byte[] sign(PrivateKey privateKey, SecretKey masterKey, boolean alice);

	/**
	 * Verifies a signature that proves ownership of a pseudonym.
	 *
	 * @param publicKey The pseudonym's signature public key
	 * @param alice Whether the pseudonym belongs to Alice
	 * @return True if the signature is valid
	 */
	boolean verify(PublicKey publicKey, SecretKey masterKey, boolean alice,
			byte[] signature);
}
