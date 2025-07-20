package com.quantumresearch.mycel.infrastructure.system;

import com.quantumresearch.mycel.infrastructure.api.system.SecureRandomProvider;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.infrastructure.util.OsUtils.isLinux;
import static com.quantumresearch.mycel.infrastructure.util.OsUtils.isMac;

@Module
public class DesktopSecureRandomModule {

	@Provides
	@Singleton
	SecureRandomProvider provideSecureRandomProvider() {
		if (isLinux()) return new UnixSecureRandomProvider();
		return () -> null; // Use system default
	}
}
