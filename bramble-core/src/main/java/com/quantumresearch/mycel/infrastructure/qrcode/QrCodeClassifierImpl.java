package com.quantumresearch.mycel.infrastructure.qrcode;

import com.quantumresearch.mycel.infrastructure.api.Pair;
import com.quantumresearch.mycel.infrastructure.api.keyagreement.KeyAgreementConstants;
import com.quantumresearch.mycel.infrastructure.api.mailbox.MailboxConstants;
import com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType.BQP;
import static com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType.MAILBOX;
import static com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier.QrCodeType.UNKNOWN;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.ISO_8859_1;

@Immutable
@NotNullByDefault
class QrCodeClassifierImpl implements QrCodeClassifier {

	@Inject
	QrCodeClassifierImpl() {
	}

	@Override
	public Pair<QrCodeType, Integer> classifyQrCode(String payload) {
		byte[] bytes = payload.getBytes(ISO_8859_1);
		if (bytes.length == 0) return new Pair<>(UNKNOWN, 0);
		// If this is a Bramble QR code then the first byte encodes the
		// format ID (3 bits) and version (5 bits)
		int formatIdAndVersion = bytes[0] & 0xFF;
		int formatId = formatIdAndVersion >> 5;
		int formatVersion = formatIdAndVersion & 0x1F;
		if (formatId == KeyAgreementConstants.QR_FORMAT_ID) {
			return new Pair<>(BQP, formatVersion);
		}
		if (formatId == MailboxConstants.QR_FORMAT_ID) {
			return new Pair<>(MAILBOX, formatVersion);
		}
		return new Pair<>(UNKNOWN, 0);
	}
}
