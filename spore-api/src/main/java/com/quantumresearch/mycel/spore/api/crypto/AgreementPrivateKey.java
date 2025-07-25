package com.quantumresearch.mycel.spore.api.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_AGREEMENT;

/**
 * Type-safe wrapper for a private key used for key agreement.
 */
@Immutable
@NotNullByDefault
public class AgreementPrivateKey extends Bytes implements PrivateKey {

	public AgreementPrivateKey(byte[] encoded) {
		super(encoded);
	}

	@Override
	public String getKeyType() {
		return KEY_TYPE_AGREEMENT;
	}

	@Override
	public byte[] getEncoded() {
		return getBytes();
	}
}
