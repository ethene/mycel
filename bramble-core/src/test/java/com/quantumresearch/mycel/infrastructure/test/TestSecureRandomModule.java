package com.quantumresearch.mycel.infrastructure.test;

import com.quantumresearch.mycel.infrastructure.api.system.SecureRandomProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class TestSecureRandomModule {

	@Provides
	SecureRandomProvider provideSecureRandomProvider() {
		return new TestSecureRandomProvider();
	}
}
