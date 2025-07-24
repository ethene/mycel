package com.quantumresearch.mycel.spore.crypto;

import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.spore.api.crypto.KeyAgreementCrypto;
import com.quantumresearch.mycel.spore.api.crypto.PasswordStrengthEstimator;
import com.quantumresearch.mycel.spore.api.crypto.StreamDecrypterFactory;
import com.quantumresearch.mycel.spore.api.crypto.StreamEncrypterFactory;
import com.quantumresearch.mycel.spore.api.crypto.TransportCrypto;
import com.quantumresearch.mycel.spore.api.system.SecureRandomProvider;

import java.security.SecureRandom;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CryptoModule {

	@Provides
	AuthenticatedCipher provideAuthenticatedCipher() {
		return new XSalsa20Poly1305AuthenticatedCipher();
	}

	@Provides
	@Singleton
	CryptoComponent provideCryptoComponent(
			SecureRandomProvider secureRandomProvider,
			ScryptKdf passwordBasedKdf) {
		return new CryptoComponentImpl(secureRandomProvider, passwordBasedKdf);
	}

	@Provides
	PasswordStrengthEstimator providePasswordStrengthEstimator() {
		return new PasswordStrengthEstimatorImpl();
	}

	@Provides
	TransportCrypto provideTransportCrypto(
			TransportCryptoImpl transportCrypto) {
		return transportCrypto;
	}

	@Provides
	StreamDecrypterFactory provideStreamDecrypterFactory(
			Provider<AuthenticatedCipher> cipherProvider) {
		return new StreamDecrypterFactoryImpl(cipherProvider);
	}

	@Provides
	StreamEncrypterFactory provideStreamEncrypterFactory(
			CryptoComponent crypto, TransportCrypto transportCrypto,
			Provider<AuthenticatedCipher> cipherProvider) {
		return new StreamEncrypterFactoryImpl(crypto, transportCrypto,
				cipherProvider);
	}

	@Provides
	KeyAgreementCrypto provideKeyAgreementCrypto(
			KeyAgreementCryptoImpl keyAgreementCrypto) {
		return keyAgreementCrypto;
	}

	@Provides
	SecureRandom getSecureRandom(CryptoComponent crypto) {
		return crypto.getSecureRandom();
	}

}
