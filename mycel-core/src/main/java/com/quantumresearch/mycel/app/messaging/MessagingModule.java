package com.quantumresearch.mycel.app.messaging;

import com.quantumresearch.mycel.spore.api.FeatureFlags;
import com.quantumresearch.mycel.spore.api.cleanup.CleanupManager;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.sync.validation.ValidationManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.api.versioning.ClientVersioningManager;
import com.quantumresearch.mycel.app.api.conversation.ConversationManager;
import com.quantumresearch.mycel.app.api.messaging.MessagingManager;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.app.api.messaging.MessagingManager.CLIENT_ID;
import static com.quantumresearch.mycel.app.api.messaging.MessagingManager.MAJOR_VERSION;
import static com.quantumresearch.mycel.app.api.messaging.MessagingManager.MINOR_VERSION;

@Module
public class MessagingModule {

	public static class EagerSingletons {
		@Inject
		MessagingManager messagingManager;
		@Inject
		PrivateMessageValidator privateMessageValidator;
	}

	@Provides
	PrivateMessageFactory providePrivateMessageFactory(
			PrivateMessageFactoryImpl privateMessageFactory) {
		return privateMessageFactory;
	}

	@Provides
	@Singleton
	PrivateMessageValidator getValidator(ValidationManager validationManager,
			BdfReaderFactory bdfReaderFactory, MetadataEncoder metadataEncoder,
			Clock clock) {
		PrivateMessageValidator validator = new PrivateMessageValidator(
				bdfReaderFactory, metadataEncoder, clock);
		validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
				validator);
		return validator;
	}

	@Provides
	@Singleton
	MessagingManager getMessagingManager(LifecycleManager lifecycleManager,
			ContactManager contactManager, ValidationManager validationManager,
			ConversationManager conversationManager,
			ClientVersioningManager clientVersioningManager,
			CleanupManager cleanupManager, FeatureFlags featureFlags,
			MessagingManagerImpl messagingManager) {
		lifecycleManager.registerOpenDatabaseHook(messagingManager);
		contactManager.registerContactHook(messagingManager);
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		conversationManager.registerConversationClient(messagingManager);
		// Don't advertise support for image attachments or disappearing
		// messages unless the respective feature flags are enabled
		boolean images = featureFlags.shouldEnableImageAttachments();
		boolean disappear = featureFlags.shouldEnableDisappearingMessages();
		int minorVersion = images ? (disappear ? MINOR_VERSION : 2) : 0;
		clientVersioningManager.registerClient(CLIENT_ID, MAJOR_VERSION,
				minorVersion, messagingManager);
		cleanupManager.registerCleanupHook(CLIENT_ID, MAJOR_VERSION,
				messagingManager);
		return messagingManager;
	}
}
