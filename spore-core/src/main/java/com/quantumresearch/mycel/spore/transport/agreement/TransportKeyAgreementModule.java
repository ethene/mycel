package com.quantumresearch.mycel.spore.transport.agreement;

import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.sync.validation.ValidationManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.transport.agreement.TransportKeyAgreementManager;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.spore.api.transport.agreement.TransportKeyAgreementManager.CLIENT_ID;
import static com.quantumresearch.mycel.spore.api.transport.agreement.TransportKeyAgreementManager.MAJOR_VERSION;
import static com.quantumresearch.mycel.spore.api.transport.agreement.TransportKeyAgreementManager.MINOR_VERSION;

@Module
public class TransportKeyAgreementModule {

	public static class EagerSingletons {
		@Inject
		TransportKeyAgreementManager transportKeyAgreementManager;
		@Inject
		TransportKeyAgreementValidator transportKeyAgreementValidator;
	}

	@Provides
	@Singleton
	TransportKeyAgreementManager provideTransportKeyAgreementManager(
			LifecycleManager lifecycleManager,
			ValidationManager validationManager,
			ContactManager contactManager,
			ClientVersioningManager clientVersioningManager,
			TransportKeyAgreementManagerImpl transportKeyAgreementManager) {
		lifecycleManager.registerOpenDatabaseHook(transportKeyAgreementManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID,
				MAJOR_VERSION, transportKeyAgreementManager);
		contactManager.registerContactHook(transportKeyAgreementManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, transportKeyAgreementManager);
		return transportKeyAgreementManager;
	}

	@Provides
	@Singleton
	TransportKeyAgreementValidator provideTransportKeyAgreementValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, MessageEncoder messageEncoder,
			ValidationManager validationManager) {
		TransportKeyAgreementValidator validator =
				new TransportKeyAgreementValidator(clientHelper,
						metadataEncoder, clock, messageEncoder);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	MessageEncoder provideMessageEncoder(MessageEncoderImpl messageEncoder) {
		return messageEncoder;
	}

	@Provides
	SessionEncoder provideSessionEncoder(SessionEncoderImpl sessionEncoder) {
		return sessionEncoder;
	}

	@Provides
	SessionParser provideSessionParser(SessionParserImpl sessionParser) {
		return sessionParser;
	}

	@Provides
	TransportKeyAgreementCrypto provideTransportKeyAgreementCrypto(
			TransportKeyAgreementCryptoImpl transportKeyAgreementCrypto) {
		return transportKeyAgreementCrypto;
	}
}
