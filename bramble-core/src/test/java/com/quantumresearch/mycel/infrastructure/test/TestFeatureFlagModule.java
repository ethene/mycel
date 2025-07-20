package com.quantumresearch.mycel.infrastructure.test;

import com.quantumresearch.mycel.infrastructure.api.FeatureFlags;

import dagger.Module;
import dagger.Provides;

@Module
public class TestFeatureFlagModule {
	@Provides
	FeatureFlags provideFeatureFlags() {
		return new FeatureFlags() {
			@Override
			public boolean shouldEnableImageAttachments() {
				return true;
			}

			@Override
			public boolean shouldEnableProfilePictures() {
				return true;
			}

			@Override
			public boolean shouldEnableDisappearingMessages() {
				return true;
			}

			@Override
			public boolean shouldEnablePrivateGroupsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableForumsInCore() {
				return true;
			}

			@Override
			public boolean shouldEnableBlogsInCore() {
				return true;
			}
		};
	}
}
