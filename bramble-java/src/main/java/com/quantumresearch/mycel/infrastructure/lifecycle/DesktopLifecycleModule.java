package com.quantumresearch.mycel.infrastructure.lifecycle;

import com.quantumresearch.mycel.infrastructure.api.lifecycle.ShutdownManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.quantumresearch.mycel.infrastructure.util.OsUtils.isWindows;

@Module
public class DesktopLifecycleModule extends LifecycleModule {

	@Provides
	@Singleton
	ShutdownManager provideDesktopShutdownManager() {
		if (isWindows()) return new WindowsShutdownManagerImpl();
		else return new ShutdownManagerImpl();
	}
}
