package com.quantumresearch.mycel.spore.api.qrcode;

import com.quantumresearch.mycel.spore.api.Pair;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface QrCodeClassifier {

	enum QrCodeType {
		BQP,
		MAILBOX,
		UNKNOWN
	}

	Pair<QrCodeType, Integer> classifyQrCode(String payload);
}
