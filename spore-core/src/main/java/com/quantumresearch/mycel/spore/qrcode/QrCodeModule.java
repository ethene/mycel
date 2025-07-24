package com.quantumresearch.mycel.spore.qrcode;

import com.quantumresearch.mycel.spore.api.qrcode.QrCodeClassifier;

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
