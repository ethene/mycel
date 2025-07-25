package com.quantumresearch.mycel.spore.crypto;

import org.bouncycastle.crypto.CryptoException;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.test.BrambleTestCase;
import org.junit.Test;

import java.security.SecureRandom;

import static org.junit.Assert.assertArrayEquals;

public class MessageEncrypterTest extends BrambleTestCase {

	private final SecureRandom random = new SecureRandom();

	@Test
	public void testEncryptionAndDecryption() throws Exception {
		MessageEncrypter m = new MessageEncrypter(random);
		KeyPair kp = m.generateKeyPair();
		PublicKey pub = kp.getPublic();
		PrivateKey priv = kp.getPrivate();
		byte[] plaintext = new byte[123];
		random.nextBytes(plaintext);
		byte[] ciphertext = m.encrypt(pub, plaintext);
		byte[] decrypted = m.decrypt(priv, ciphertext);
		assertArrayEquals(plaintext, decrypted);
	}

	@Test(expected = CryptoException.class)
	public void testDecryptionFailsWithAlteredCiphertext() throws Exception {
		MessageEncrypter m = new MessageEncrypter(random);
		KeyPair kp = m.generateKeyPair();
		PublicKey pub = kp.getPublic();
		PrivateKey priv = kp.getPrivate();
		byte[] ciphertext = m.encrypt(pub, new byte[123]);
		ciphertext[random.nextInt(ciphertext.length)] ^= 0xFF;
		m.decrypt(priv, ciphertext);
	}
}
