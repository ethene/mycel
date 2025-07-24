package com.quantumresearch.mycel.spore.properties;

import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.sync.validation.ValidationManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager.CLIENT_ID;
import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager.MAJOR_VERSION;
import static com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager.MINOR_VERSION;

@Module
public class PropertiesModule {

	public static class EagerSingletons {
		@Inject
		TransportPropertyValidator transportPropertyValidator;
		@Inject
		TransportPropertyManager transportPropertyManager;
	}

	@Provides
	@Singleton
	TransportPropertyValidator getValidator(ValidationManager validationManager,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock) {
		TransportPropertyValidator validator = new TransportPropertyValidator(
				clientHelper, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	@Singleton
	TransportPropertyManager getTransportPropertyManager(
			LifecycleManager lifecycleManager,
			ValidationManager validationManager, ContactManager contactManager,
			ClientVersioningManager clientVersioningManager,
			TransportPropertyManagerImpl transportPropertyManager) {
		lifecycleManager.registerOpenDatabaseHook(transportPropertyManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				transportPropertyManager);
		contactManager.registerContactHook(transportPropertyManager);
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				MINOR_VERSION, transportPropertyManager);
		return transportPropertyManager;
	}
}
