package com.quantumresearch.mycel.infrastructure.transport.agreement;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.api.crypto.KeyPair;
import com.quantumresearch.mycel.infrastructure.api.crypto.PrivateKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;

@NotNullByDefault
interface TransportKeyAgreementCrypto {

	KeyPair generateKeyPair();

	SecretKey deriveRootKey(KeyPair localKeyPair, PublicKey remotePublicKey)
			throws GeneralSecurityException;

	PublicKey parsePublicKey(byte[] encoded) throws FormatException;

	PrivateKey parsePrivateKey(byte[] encoded) throws FormatException;
}
