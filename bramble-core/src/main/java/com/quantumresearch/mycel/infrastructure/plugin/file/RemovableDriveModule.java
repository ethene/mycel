package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RemovableDriveModule {

	@Provides
	@Singleton
	RemovableDriveManager provideRemovableDriveManager(
			RemovableDriveManagerImpl removableDriveManager) {
		return removableDriveManager;
	}

	@Provides
	RemovableDriveTaskFactory provideTaskFactory(
			RemovableDriveTaskFactoryImpl taskFactory) {
		return taskFactory;
	}
}
