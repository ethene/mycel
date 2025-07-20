package com.quantumresearch.mycel.infrastructure.settings;

import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.settings.SettingsManager;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsModule {

	@Provides
	SettingsManager provideSettingsManager(DatabaseComponent db) {
		return new SettingsManagerImpl(db);
	}

}
