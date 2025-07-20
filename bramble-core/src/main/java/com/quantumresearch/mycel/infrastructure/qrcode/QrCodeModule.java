package com.quantumresearch.mycel.infrastructure.qrcode;

import com.quantumresearch.mycel.infrastructure.api.qrcode.QrCodeClassifier;

import dagger.Module;
import dagger.Provides;

@Module
public class QrCodeModule {

	@Provides
	QrCodeClassifier provideQrCodeClassifier(
			QrCodeClassifierImpl qrCodeClassifier) {
		return qrCodeClassifier;
	}
}
