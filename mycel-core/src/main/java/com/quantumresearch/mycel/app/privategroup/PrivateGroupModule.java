package com.quantumresearch.mycel.app.privategroup;

import com.quantumresearch.mycel.spore.api.FeatureFlags;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.sync.validation.ValidationManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupFactory;
import com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager;
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager.CLIENT_ID;
import static com.quantumresearch.mycel.app.api.privategroup.PrivateGroupManager.MAJOR_VERSION;

@Module
public class PrivateGroupModule {

	public static class EagerSingletons {
		@Inject
		GroupMessageValidator groupMessageValidator;
		@Inject
		PrivateGroupManager groupManager;
	}

	@Provides
	@Singleton
	PrivateGroupManager provideGroupManager(
			PrivateGroupManagerImpl groupManager,
			ValidationManager validationManager,
			FeatureFlags featureFlags) {
		if (!featureFlags.shouldEnablePrivateGroupsInCore()) {
			return groupManager;
		}
		validationManager.registerIncomingMessageHook(CLIENT_ID, MAJOR_VERSION,
				groupManager);
		return groupManager;
	}

	@Provides
	PrivateGroupFactory providePrivateGroupFactory(
			PrivateGroupFactoryImpl privateGroupFactory) {
		return privateGroupFactory;
	}

	@Provides
	GroupMessageFactory provideGroupMessageFactory(
			GroupMessageFactoryImpl groupMessageFactory) {
		return groupMessageFactory;
	}

	@Provides
	@Singleton
	GroupMessageValidator provideGroupMessageValidator(
			PrivateGroupFactory privateGroupFactory,
			ClientHelper clientHelper, MetadataEncoder metadataEncoder,
			Clock clock, GroupInvitationFactory groupInvitationFactory,
			ValidationManager validationManager, FeatureFlags featureFlags) {
		GroupMessageValidator validator = new GroupMessageValidator(
				privateGroupFactory, clientHelper, metadataEncoder, clock,
				groupInvitationFactory);
		if (featureFlags.shouldEnablePrivateGroupsInCore()) {
			validationManager.registerMessageValidator(CLIENT_ID, MAJOR_VERSION,
					validator);
		}
		return validator;
	}

}
