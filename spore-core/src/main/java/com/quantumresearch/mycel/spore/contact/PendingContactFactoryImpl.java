package com.quantumresearch.mycel.spore.contact;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.UnsupportedVersionException;
import com.quantumresearch.mycel.spore.api.contact.PendingContact;
import com.quantumresearch.mycel.spore.api.contact.PendingContactId;
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyParser;
import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.util.Base32;

import java.security.GeneralSecurityException;
import java.util.Locale;
import java.util.regex.Matcher;

import javax.inject.Inject;

import static java.lang.System.arraycopy;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.FORMAT_VERSION;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.ID_LABEL;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.LINK_REGEX;
import static com.quantumresearch.mycel.spore.api.contact.HandshakeLinkConstants.RAW_LINK_BYTES;
import static com.quantumresearch.mycel.spore.api.crypto.CryptoConstants.KEY_TYPE_AGREEMENT;

class PendingContactFactoryImpl implements PendingContactFactory {

	private final CryptoComponent crypto;
	private final Clock clock;

	@Inject
	PendingContactFactoryImpl(CryptoComponent crypto, Clock clock) {
		this.crypto = crypto;
		this.clock = clock;
	}

	@Override
	public PendingContact createPendingContact(String link, String alias)
			throws FormatException {
		PublicKey publicKey = parseHandshakeLink(link);
		PendingContactId id = getPendingContactId(publicKey);
		long timestamp = clock.currentTimeMillis();
		return new PendingContact(id, publicKey, alias, timestamp);
	}

	@Override
	public String createHandshakeLink(PublicKey k) {
		if (!k.getKeyType().equals(KEY_TYPE_AGREEMENT))
			throw new IllegalArgumentException();
		byte[] encoded = k.getEncoded();
		if (encoded.length != RAW_LINK_BYTES - 1)
			throw new IllegalArgumentException();
		byte[] raw = new byte[RAW_LINK_BYTES];
		raw[0] = FORMAT_VERSION;
		arraycopy(encoded, 0, raw, 1, encoded.length);
		return "mycel://" + Base32.encode(raw).toLowerCase(Locale.US);
	}

	private PublicKey parseHandshakeLink(String link) throws FormatException {
		Matcher matcher = LINK_REGEX.matcher(link);
		if (!matcher.find()) throw new FormatException();
		// Discard 'mycel://' and anything before or after the link
		link = matcher.group(2);
		byte[] raw = Base32.decode(link, false);
		if (raw.length != RAW_LINK_BYTES) throw new AssertionError();
		byte version = raw[0];
		if (version != FORMAT_VERSION)
			throw new UnsupportedVersionException(version < FORMAT_VERSION);
		byte[] publicKeyBytes = new byte[raw.length - 1];
		arraycopy(raw, 1, publicKeyBytes, 0, publicKeyBytes.length);
		try {
			KeyParser parser = crypto.getAgreementKeyParser();
			return parser.parsePublicKey(publicKeyBytes);
		} catch (GeneralSecurityException e) {
			throw new FormatException();
		}
	}

	private PendingContactId getPendingContactId(PublicKey publicKey) {
		byte[] hash = crypto.hash(ID_LABEL, publicKey.getEncoded());
		return new PendingContactId(hash);
	}
}
