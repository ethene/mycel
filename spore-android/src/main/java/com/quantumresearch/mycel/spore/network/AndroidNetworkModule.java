package com.quantumresearch.mycel.spore.network;

import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.network.NetworkManager;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidNetworkModule {

	public static class EagerSingletons {
		@Inject
		NetworkManager networkManager;
	}

	@Provides
	@Singleton
	NetworkManager provideNetworkManager(LifecycleManager lifecycleManager,
			AndroidNetworkManager networkManager) {
		lifecycleManager.registerService(networkManager);
		return networkManager;
	}
}
