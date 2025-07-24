package com.quantumresearch.mycel.spore.plugin;

import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.plugin.BackoffFactory;
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig;
import com.quantumresearch.mycel.spore.api.plugin.PluginManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PluginModule {

	public static class EagerSingletons {
		@Inject
		PluginManager pluginManager;
		@Inject
		Poller poller;
	}

	@Provides
	BackoffFactory provideBackoffFactory() {
		return new BackoffFactoryImpl();
	}

	@Provides
	@Singleton
	PluginManager providePluginManager(LifecycleManager lifecycleManager,
			PluginManagerImpl pluginManager) {
		lifecycleManager.registerService(pluginManager);
		return pluginManager;
	}

	@Provides
	@Singleton
	Poller providePoller(PluginConfig config, EventBus eventBus,
			PollerImpl poller) {
		if (config.shouldPoll()) eventBus.addListener(poller);
		return poller;
	}
}
