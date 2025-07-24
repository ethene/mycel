package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.api.system.SecureRandomProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class TestSecureRandomModule {

	@Provides
	SecureRandomProvider provideSecureRandomProvider() {
		return new TestSecureRandomProvider();
	}
}
