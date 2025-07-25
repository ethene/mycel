package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.api.plugin.PluginConfig;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory;
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

@Module
class RemovableDriveIntegrationTestModule {

	@Provides
	@Singleton
	PluginConfig providePluginConfig(RemovableDrivePluginFactory drive) {
		@NotNullByDefault
		PluginConfig pluginConfig = new PluginConfig() {

			@Override
			public Collection<DuplexPluginFactory> getDuplexFactories() {
				return emptyList();
			}

			@Override
			public Collection<SimplexPluginFactory> getSimplexFactories() {
				return singletonList(drive);
			}

			@Override
			public boolean shouldPoll() {
				return false;
			}

			@Override
			public Map<TransportId, List<TransportId>> getTransportPreferences() {
				return emptyMap();
			}

		};
		return pluginConfig;
	}
}
