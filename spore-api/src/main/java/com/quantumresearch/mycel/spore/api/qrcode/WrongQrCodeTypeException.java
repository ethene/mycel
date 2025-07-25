package com.quantumresearch.mycel.spore.api.qrcode;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.qrcode.QrCodeClassifier.QrCodeType;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * Thrown when a QR code that has been scanned does not have the expected type.
 */
@Immutable
@NotNullByDefault
public class WrongQrCodeTypeException extends FormatException {

	private final QrCodeType qrCodeType;

	public WrongQrCodeTypeException(QrCodeType qrCodeType) {
		this.qrCodeType = qrCodeType;
	}

	public QrCodeType getQrCodeType() {
		return qrCodeType;
	}
}
