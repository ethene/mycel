package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyAgreementCrypto;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.crypto.SecretKey;

import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementConstants.COMMIT_LENGTH;

class KeyAgreementCryptoImpl implements KeyAgreementCrypto {

	private final CryptoComponent crypto;

	@Inject
	KeyAgreementCryptoImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public byte[] deriveKeyCommitment(PublicKey publicKey) {
		byte[] hash = crypto.hash(COMMIT_LABEL, publicKey.getEncoded());
		// The output is the first COMMIT_LENGTH bytes of the hash
		byte[] commitment = new byte[COMMIT_LENGTH];
		System.arraycopy(hash, 0, commitment, 0, COMMIT_LENGTH);
		return commitment;
	}

	@Override
	public byte[] deriveConfirmationRecord(SecretKey sharedSecret,
			byte[] theirPayload, byte[] ourPayload, PublicKey theirPublicKey,
			KeyPair ourKeyPair, boolean alice, boolean aliceRecord) {
		SecretKey ck = crypto.deriveKey(CONFIRMATION_KEY_LABEL, sharedSecret);
		byte[] alicePayload, alicePub, bobPayload, bobPub;
		if (alice) {
			alicePayload = ourPayload;
			alicePub = ourKeyPair.getPublic().getEncoded();
			bobPayload = theirPayload;
			bobPub = theirPublicKey.getEncoded();
		} else {
			alicePayload = theirPayload;
			alicePub = theirPublicKey.getEncoded();
			bobPayload = ourPayload;
			bobPub = ourKeyPair.getPublic().getEncoded();
		}
		if (aliceRecord) {
			return crypto.mac(CONFIRMATION_MAC_LABEL, ck, alicePayload,
					alicePub, bobPayload, bobPub);
		} else {
			return crypto.mac(CONFIRMATION_MAC_LABEL, ck, bobPayload, bobPub,
					alicePayload, alicePub);
		}
	}
}
