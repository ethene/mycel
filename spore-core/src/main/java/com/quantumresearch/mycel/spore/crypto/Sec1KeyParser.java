package com.quantumresearch.mycel.spore.crypto;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import com.quantumresearch.mycel.spore.api.crypto.KeyParser;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.util.LogUtils.logDuration;
import static com.quantumresearch.mycel.spore.util.LogUtils.now;

/**
 * A key parser that uses the encoding defined in "SEC 1: Elliptic Curve
 * Cryptography", section 2.3 (Certicom Corporation, May 2009). Point
 * compression is not used.
 */
@Immutable
@NotNullByDefault
class Sec1KeyParser implements KeyParser {

	private static final Logger LOG =
			Logger.getLogger(Sec1KeyParser.class.getName());

	private final String keyType;
	private final ECDomainParameters params;
	private final BigInteger modulus;
	private final int keyBits, bytesPerInt, publicKeyBytes, privateKeyBytes;

	Sec1KeyParser(String keyType, ECDomainParameters params, int keyBits) {
		this.keyType = keyType;
		this.params = params;
		this.keyBits = keyBits;
		modulus = ((ECCurve.Fp) params.getCurve()).getQ();
		bytesPerInt = (keyBits + 7) / 8;
		publicKeyBytes = 1 + 2 * bytesPerInt;
		privateKeyBytes = bytesPerInt;
	}

	@Override
	public PublicKey parsePublicKey(byte[] encodedKey)
			throws GeneralSecurityException {
		// The validation procedure comes from SEC 1, section 3.2.2.1. Note
		// that SEC 1 parameter names are used below, not RFC 5639 names
		long start = now();
		if (encodedKey.length != publicKeyBytes)
			throw new GeneralSecurityException();
		// The first byte must be 0x04
		if (encodedKey[0] != 4) throw new GeneralSecurityException();
		// The x co-ordinate must be >= 0 and < p
		byte[] xBytes = new byte[bytesPerInt];
		System.arraycopy(encodedKey, 1, xBytes, 0, bytesPerInt);
		BigInteger x = new BigInteger(1, xBytes); // Positive signum
		if (x.compareTo(modulus) >= 0) throw new GeneralSecurityException();
		// The y co-ordinate must be >= 0 and < p
		byte[] yBytes = new byte[bytesPerInt];
		System.arraycopy(encodedKey, 1 + bytesPerInt, yBytes, 0, bytesPerInt);
		BigInteger y = new BigInteger(1, yBytes); // Positive signum
		if (y.compareTo(modulus) >= 0) throw new GeneralSecurityException();
		// Verify that y^2 == x^3 + ax + b (mod p)
		ECCurve curve = params.getCurve();
		BigInteger a = curve.getA().toBigInteger();
		BigInteger b = curve.getB().toBigInteger();
		BigInteger lhs = y.multiply(y).mod(modulus);
		BigInteger rhs = x.multiply(x).add(a).multiply(x).add(b).mod(modulus);
		if (!lhs.equals(rhs)) throw new GeneralSecurityException();
		// We know the point (x, y) is on the curve, so we can create the point
		ECPoint pub = curve.createPoint(x, y).normalize();
		// Verify that the point (x, y) is not the point at infinity
		if (pub.isInfinity()) throw new GeneralSecurityException();
		// Verify that the point (x, y) times n is the point at infinity
		if (!pub.multiply(params.getN()).isInfinity())
			throw new GeneralSecurityException();
		// Construct a public key from the point (x, y) and the params
		ECPublicKeyParameters k = new ECPublicKeyParameters(pub, params);
		PublicKey p = new Sec1PublicKey(keyType, k);
		logDuration(LOG, "Parsing public key", start);
		return p;
	}

	@Override
	public PrivateKey parsePrivateKey(byte[] encodedKey)
			throws GeneralSecurityException {
		long start = now();
		if (encodedKey.length != privateKeyBytes)
			throw new GeneralSecurityException();
		BigInteger d = new BigInteger(1, encodedKey); // Positive signum
		// Verify that the private value is < n
		if (d.compareTo(params.getN()) >= 0)
			throw new GeneralSecurityException();
		// Construct a private key from the private value and the params
		ECPrivateKeyParameters k = new ECPrivateKeyParameters(d, params);
		PrivateKey p = new Sec1PrivateKey(keyType, k, keyBits);
		logDuration(LOG, "Parsing private key", start);
		return p;
	}
}
