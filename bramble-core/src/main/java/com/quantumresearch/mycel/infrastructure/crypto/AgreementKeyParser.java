package com.quantumresearch.mycel.infrastructure.crypto;

import com.quantumresearch.mycel.infrastructure.api.crypto.AgreementPrivateKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.AgreementPublicKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyParser;
import com.quantumresearch.mycel.infrastructure.api.crypto.PrivateKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class AgreementKeyParser implements KeyParser {

	@Override
	public PublicKey parsePublicKey(byte[] encodedKey)
			throws GeneralSecurityException {
		if (encodedKey.length != 32) throw new GeneralSecurityException();
		return new AgreementPublicKey(encodedKey);
	}

	@Override
	public PrivateKey parsePrivateKey(byte[] encodedKey)
			throws GeneralSecurityException {
		if (encodedKey.length != 32) throw new GeneralSecurityException();
		return new AgreementPrivateKey(clamp(encodedKey));
	}

	static byte[] clamp(byte[] b) {
		byte[] clamped = new byte[32];
		System.arraycopy(b, 0, clamped, 0, 32);
		clamped[0] &= 248;
		clamped[31] &= 127;
		clamped[31] |= 64;
		return clamped;
	}
}
