package com.quantumresearch.mycel.spore.crypto;

import net.i2p.crypto.eddsa.EdDSAPrivateKey;
import net.i2p.crypto.eddsa.EdDSAPublicKey;
import net.i2p.crypto.eddsa.EdDSASecurityProvider;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveSpec;
import net.i2p.crypto.eddsa.spec.EdDSANamedCurveTable;
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec;
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec;

import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import static net.i2p.crypto.eddsa.EdDSAEngine.SIGNATURE_ALGORITHM;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_SIGNATURE;

@NotNullByDefault
class EdSignature implements Signature {

	private static final Provider PROVIDER = new EdDSASecurityProvider();

	private static final EdDSANamedCurveSpec CURVE_SPEC =
			EdDSANamedCurveTable.getByName("Ed25519");

	private final java.security.Signature signature;

	EdSignature() {
		try {
			signature = java.security.Signature
					.getInstance(SIGNATURE_ALGORITHM, PROVIDER);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public void initSign(PrivateKey k) throws GeneralSecurityException {
		if (!k.getKeyType().equals(KEY_TYPE_SIGNATURE))
			throw new IllegalArgumentException();
		EdDSAPrivateKey privateKey = new EdDSAPrivateKey(
				new EdDSAPrivateKeySpec(k.getEncoded(), CURVE_SPEC));
		signature.initSign(privateKey);
	}

	@Override
	public void initVerify(PublicKey k) throws GeneralSecurityException {
		if (!k.getKeyType().equals(KEY_TYPE_SIGNATURE))
			throw new IllegalArgumentException();
		EdDSAPublicKey publicKey = new EdDSAPublicKey(
				new EdDSAPublicKeySpec(k.getEncoded(), CURVE_SPEC));
		signature.initVerify(publicKey);
	}

	@Override
	public void update(byte b) throws GeneralSecurityException {
		signature.update(b);
	}

	@Override
	public void update(byte[] b) throws GeneralSecurityException {
		signature.update(b);
	}

	@Override
	public void update(byte[] b, int off, int len)
			throws GeneralSecurityException {
		signature.update(b, off, len);
	}

	@Override
	public byte[] sign() throws GeneralSecurityException {
		return signature.sign();
	}

	@Override
	public boolean verify(byte[] sig) throws GeneralSecurityException {
		return signature.verify(sig);
	}
}
