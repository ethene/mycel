package com.quantumresearch.mycel.infrastructure.contact;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactManager;
import com.quantumresearch.mycel.infrastructure.api.contact.HandshakeManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ContactModule {

	public static class EagerSingletons {
		@Inject
		ContactManager contactManager;
	}

	@Provides
	@Singleton
	ContactManager provideContactManager(EventBus eventBus,
			ContactManagerImpl contactManager) {
		eventBus.addListener(contactManager);
		return contactManager;
	}

	@Provides
	ContactExchangeManager provideContactExchangeManager(
			ContactExchangeManagerImpl contactExchangeManager) {
		return contactExchangeManager;
	}

	@Provides
	PendingContactFactory providePendingContactFactory(
			PendingContactFactoryImpl pendingContactFactory) {
		return pendingContactFactory;
	}

	@Provides
	ContactExchangeCrypto provideContactExchangeCrypto(
			ContactExchangeCryptoImpl contactExchangeCrypto) {
		return contactExchangeCrypto;
	}

	@Provides
	@Singleton
	HandshakeManager provideHandshakeManager(
			HandshakeManagerImpl handshakeManager) {
		return handshakeManager;
	}

	@Provides
	HandshakeCrypto provideHandshakeCrypto(
			HandshakeCryptoImpl handshakeCrypto) {
		return handshakeCrypto;
	}
}
