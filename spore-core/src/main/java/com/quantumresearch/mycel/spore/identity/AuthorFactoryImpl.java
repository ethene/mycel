package com.quantumresearch.mycel.spore.identity;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyPair;
import com.quantumresearch.mycel.spore.api.crypto.PrivateKey;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.identity.AuthorId;
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.identity.Author.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.identity.AuthorId.LABEL;
import static com.quantumresearch.mycel.spore.util.ByteUtils.INT_32_BYTES;
import static com.quantumresearch.mycel.spore.util.ByteUtils.writeUint32;
import static com.quantumresearch.mycel.spore.util.StringUtils.toUtf8;

@Immutable
@NotNullByDefault
class AuthorFactoryImpl implements AuthorFactory {

	private final CryptoComponent crypto;

	@Inject
	AuthorFactoryImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public Author createAuthor(String name, PublicKey publicKey) {
		return createAuthor(FORMAT_VERSION, name, publicKey);
	}

	@Override
	public Author createAuthor(int formatVersion, String name,
			PublicKey publicKey) {
		AuthorId id = getId(formatVersion, name, publicKey);
		return new Author(id, formatVersion, name, publicKey);
	}

	@Override
	public LocalAuthor createLocalAuthor(String name) {
		KeyPair signatureKeyPair = crypto.generateSignatureKeyPair();
		PublicKey publicKey = signatureKeyPair.getPublic();
		PrivateKey privateKey = signatureKeyPair.getPrivate();
		AuthorId id = getId(FORMAT_VERSION, name, publicKey);
		return new LocalAuthor(id, FORMAT_VERSION, name, publicKey, privateKey);
	}

	private AuthorId getId(int formatVersion, String name,
			PublicKey publicKey) {
		byte[] formatVersionBytes = new byte[INT_32_BYTES];
		writeUint32(formatVersion, formatVersionBytes, 0);
		return new AuthorId(crypto.hash(LABEL, formatVersionBytes,
				toUtf8(name), publicKey.getEncoded()));
	}
}
