package com.quantumresearch.mycel.spore.api.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_SIGNATURE;

/**
 * Type-safe wrapper for a public key used for signing.
 */
@Immutable
@NotNullByDefault
public class SignaturePrivateKey extends Bytes implements PrivateKey {

	public SignaturePrivateKey(byte[] bytes) {
		super(bytes);
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
