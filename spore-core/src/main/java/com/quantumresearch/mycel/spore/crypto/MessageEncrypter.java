package com.quantumresearch.mycel.spore.crypto;

import org.bouncycastle.asn1.teletrust.TeleTrusTNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.BasicAgreement;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DerivationFunction;
import org.bouncycastle.crypto.KeyEncoder;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.agreement.ECDHCBasicAgreement;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.engines.IESEngine;
import org.bouncycastle.crypto.generators.ECKeyPairGenerator;
import org.bouncycastle.crypto.generators.EphemeralKeyPairGenerator;
import org.bouncycastle.crypto.generators.KDF2BytesGenerator;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECKeyGenerationParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.IESWithCipherParameters;
import org.bouncycastle.crypto.parsers.ECIESPublicKeyParser;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.KeyParser;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.util.StringUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Scanner;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.util.StringUtils.UTF_8;

@Immutable
@NotNullByDefault
public class MessageEncrypter {

	private static final String KEY_TYPE = "SEC1_brainpoolp512r1";
	private static final ECDomainParameters PARAMETERS;
	private static final int MESSAGE_KEY_BITS = 512;
	private static final int MAC_KEY_BITS = 256;
	private static final int CIPHER_KEY_BITS = 256;
	private static final int LINE_LENGTH = 70;

	static {
		X9ECParameters x9 = TeleTrusTNamedCurves.getByName("brainpoolp512r1");
		PARAMETERS = new ECDomainParameters(x9.getCurve(), x9.getG(),
				x9.getN(), x9.getH());
	}

	private final ECKeyPairGenerator generator;
	private final KeyParser parser;
	private final EphemeralKeyPairGenerator ephemeralGenerator;
	private final PublicKeyParser ephemeralParser;

	MessageEncrypter(SecureRandom random) {
		generator = new ECKeyPairGenerator();
		generator.init(new ECKeyGenerationParameters(PARAMETERS, random));
		parser = new Sec1KeyParser(KEY_TYPE, PARAMETERS, MESSAGE_KEY_BITS);
		KeyEncoder encoder = new PublicKeyEncoder();
		ephemeralGenerator = new EphemeralKeyPairGenerator(generator, encoder);
		ephemeralParser = new PublicKeyParser(PARAMETERS);
	}

	KeyPair generateKeyPair() {
		AsymmetricCipherKeyPair keyPair = generator.generateKeyPair();
		// Return a wrapper that uses the SEC 1 encoding
		ECPublicKeyParameters ecPublicKey =
				(ECPublicKeyParameters) keyPair.getPublic();
		PublicKey publicKey = new Sec1PublicKey(KEY_TYPE, ecPublicKey);
		ECPrivateKeyParameters ecPrivateKey =
				(ECPrivateKeyParameters) keyPair.getPrivate();
		PrivateKey privateKey =
				new Sec1PrivateKey(KEY_TYPE, ecPrivateKey, MESSAGE_KEY_BITS);
		return new KeyPair(publicKey, privateKey);
	}

	KeyParser getKeyParser() {
		return parser;
	}

	byte[] encrypt(PublicKey pub, byte[] plaintext) throws CryptoException {
		if (!(pub instanceof Sec1PublicKey))
			throw new IllegalArgumentException();
		IESEngine engine = getEngine();
		engine.init(((Sec1PublicKey) pub).getKey(), getCipherParameters(),
				ephemeralGenerator);
		return engine.processBlock(plaintext, 0, plaintext.length);
	}

	byte[] decrypt(PrivateKey priv, byte[] ciphertext)
			throws CryptoException {
		if (!(priv instanceof Sec1PrivateKey))
			throw new IllegalArgumentException();
		IESEngine engine = getEngine();
		engine.init(((Sec1PrivateKey) priv).getKey(), getCipherParameters(),
				ephemeralParser);
		return engine.processBlock(ciphertext, 0, ciphertext.length);
	}

	private IESEngine getEngine() {
		BasicAgreement agreement = new ECDHCBasicAgreement();
		DerivationFunction kdf = new KDF2BytesGenerator(new SHA256Digest());
		Mac mac = new HMac(new SHA256Digest());
		BlockCipher cipher = new CBCBlockCipher(new AESLightEngine());
		PaddedBufferedBlockCipher pad = new PaddedBufferedBlockCipher(cipher);
		return new IESEngine(agreement, kdf, mac, pad);
	}

	private CipherParameters getCipherParameters() {
		return new IESWithCipherParameters(null, null, MAC_KEY_BITS,
				CIPHER_KEY_BITS);
	}

	private static class PublicKeyEncoder implements KeyEncoder {

		@Override
		public byte[] getEncoded(AsymmetricKeyParameter key) {
			if (!(key instanceof ECPublicKeyParameters))
				throw new IllegalArgumentException();
			return ((ECPublicKeyParameters) key).getQ().getEncoded(false);
		}
	}

	private static class PublicKeyParser extends ECIESPublicKeyParser {

		private PublicKeyParser(ECDomainParameters ecParams) {
			super(ecParams);
		}

		@Override
		public AsymmetricKeyParameter readKey(InputStream in)
				throws IOException {
			try {
				return super.readKey(in);
			} catch (IllegalArgumentException e) {
				throw new IOException(e);
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			printUsage();
			System.exit(1);
		}
		if (args[0].equals("generate")) {
			if (args.length != 3) {
				printUsage();
				System.exit(1);
			}
			try {
				generateKeyPair(args[1], args[2]);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		} else if (args[0].equals("encrypt")) {
			if (args.length != 2) {
				printUsage();
				System.exit(1);
			}
			try {
				encryptMessage(args[1]);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		} else if (args[0].equals("decrypt")) {
			if (args.length != 2) {
				printUsage();
				System.exit(1);
			}
			try {
				decryptMessage(args[1]);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(2);
			}
		} else {
			printUsage();
			System.exit(1);
		}
	}

	private static void printUsage() {
		System.err.println("Usage:");
		System.err.println(
				"MessageEncrypter generate <public_key_file> <private_key_file>");
		System.err.println("MessageEncrypter encrypt <public_key_file>");
		System.err.println("MessageEncrypter decrypt <private_key_file>");
	}

	private static void generateKeyPair(String publicKeyFile,
			String privateKeyFile) throws Exception {
		SecureRandom random = new SecureRandom();
		MessageEncrypter encrypter = new MessageEncrypter(random);
		KeyPair keyPair = encrypter.generateKeyPair();
		PrintStream out = new PrintStream(new FileOutputStream(publicKeyFile));
		out.print(StringUtils.toHexString(keyPair.getPublic().getEncoded()));
		out.flush();
		out.close();
		out = new PrintStream(new FileOutputStream(privateKeyFile));
		out.print(StringUtils.toHexString(keyPair.getPrivate().getEncoded()));
		out.flush();
		out.close();
	}

	private static void encryptMessage(String publicKeyFile) throws Exception {
		SecureRandom random = new SecureRandom();
		MessageEncrypter encrypter = new MessageEncrypter(random);
		InputStream in = new FileInputStream(publicKeyFile);
		byte[] keyBytes = StringUtils.fromHexString(readFully(in).trim());
		PublicKey publicKey =
				encrypter.getKeyParser().parsePublicKey(keyBytes);
		String message = readFully(System.in);
		byte[] plaintext = message.getBytes(UTF_8);
		byte[] ciphertext = encrypter.encrypt(publicKey, plaintext);
		System.out.println(AsciiArmour.wrap(ciphertext, LINE_LENGTH));
	}

	private static void decryptMessage(String privateKeyFile) throws Exception {
		SecureRandom random = new SecureRandom();
		MessageEncrypter encrypter = new MessageEncrypter(random);
		InputStream in = new FileInputStream(privateKeyFile);
		byte[] keyBytes = StringUtils.fromHexString(readFully(in).trim());
		PrivateKey privateKey =
				encrypter.getKeyParser().parsePrivateKey(keyBytes);
		byte[] ciphertext = AsciiArmour.unwrap(readFully(System.in));
		byte[] plaintext = encrypter.decrypt(privateKey, ciphertext);
		System.out.println(new String(plaintext, UTF_8));
	}

	private static String readFully(InputStream in) throws IOException {
		String newline = System.getProperty("line.separator");
		StringBuilder stringBuilder = new StringBuilder();
		Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			stringBuilder.append(scanner.nextLine());
			stringBuilder.append(newline);
		}
		scanner.close();
		in.close();
		return stringBuilder.toString();
	}
}
