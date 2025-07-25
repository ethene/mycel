package com.quantumresearch.mycel.spore.api.crypto;

import com.quantumresearch.mycel.spore.api.Bytes;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_AGREEMENT;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.MAX_AGREEMENT_PUBLIC_KEY_BYTES;

/**
 * Type-safe wrapper for a public key used for key agreement.
 */
@Immutable
@NotNullByDefault
public class AgreementPublicKey extends Bytes implements PublicKey {

	public AgreementPublicKey(byte[] encoded) {
		super(encoded);
		if (encoded.length == 0 ||
				encoded.length > MAX_AGREEMENT_PUBLIC_KEY_BYTES) {
			throw new IllegalArgumentException();
		}
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
