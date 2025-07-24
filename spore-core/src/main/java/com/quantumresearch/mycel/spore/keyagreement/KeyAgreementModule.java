package com.quantumresearch.mycel.spore.keyagreement;

import com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementTask;
import com.quantumresearch.mycel.spore.api.keyagreement.PayloadEncoder;
import com.quantumresearch.mycel.spore.api.keyagreement.PayloadParser;

import dagger.Module;
import dagger.Provides;

@Module
public class KeyAgreementModule {

	@Provides
	KeyAgreementTask provideKeyAgreementTask(
			KeyAgreementTaskImpl keyAgreementTask) {
		return keyAgreementTask;
	}

	@Provides
	PayloadEncoder providePayloadEncoder(PayloadEncoderImpl payloadEncoder) {
		return payloadEncoder;
	}

	@Provides
	PayloadParser providePayloadParser(PayloadParserImpl payloadParser) {
		return payloadParser;
	}

	@Provides
	ConnectionChooser provideConnectionChooser(
			ConnectionChooserImpl connectionChooser) {
		return connectionChooser;
	}
}
