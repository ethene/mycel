package com.quantumresearch.mycel.spore.api.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_SIGNATURE;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.MAX_SIGNATURE_PUBLIC_KEY_BYTES;

/**
 * Type-safe wrapper for a public key used for verifying signatures.
 */
@Immutable
@NotNullByDefault
public class SignaturePublicKey extends Bytes implements PublicKey {

	public SignaturePublicKey(byte[] encoded) {
		super(encoded);
		if (encoded.length == 0 ||
				encoded.length > MAX_SIGNATURE_PUBLIC_KEY_BYTES) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getKeyType() {
		return KEY_TYPE_SIGNATURE;
	}

	@Override
	public byte[] getEncoded() {
		return getBytes();
	}
}
