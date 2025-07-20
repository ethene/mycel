package com.quantumresearch.mycel.infrastructure.versioning;

import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactManager;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataEncoder;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.sync.validation.ValidationManager;
import com.quantumresearch.mycel.infrastructure.api.system.Clock;
import com.quantumresearch.mycel.infrastructure.api.versioning.ClientVersioningManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.infrastructure.api.versioning.ClientVersioningManager.CLIENT_ID;
import static com.quantumresearch.mycel.infrastructure.api.versioning.ClientVersioningManager.MAJOR_VERSION;

@Module
public class VersioningModule {

	public static class EagerSingletons {
		@Inject
		ClientVersioningManager clientVersioningManager;
		@Inject
		ClientVersioningValidator clientVersioningValidator;
	}


	@Provides
	@Singleton
	ClientVersioningManager provideClientVersioningManager(
			ClientVersioningManagerImpl clientVersioningManager,
			LifecycleManager lifecycleManager, ContactManager contactManager,
			ValidationManager validationManager) {
		lifecycleManager.registerOpenDatabaseHook(clientVersioningManager);
		lifecycleManager.registerService(clientVersioningManager);
		contactManager.registerContactHook(clientVersioningManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				clientVersioningManager);
		return clientVersioningManager;
	}

	@Provides
	@Singleton
	ClientVersioningValidator provideClientVersioningValidator(
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, ValidationManager validationManager) {
		ClientVersioningValidator validator = new ClientVersioningValidator(
				clientHelper, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}
}
