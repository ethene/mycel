package com.quantumresearch.mycel.spore.rendezvous;

import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousPoller;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class RendezvousModule {

	public static class EagerSingletons {
		@Inject
		RendezvousPoller rendezvousPoller;
	}

	@Provides
	RendezvousCrypto provideRendezvousCrypto(
			RendezvousCryptoImpl rendezvousCrypto) {
		return rendezvousCrypto;
	}

	@Provides
	@Singleton
	RendezvousPoller provideRendezvousPoller(LifecycleManager lifecycleManager,
			EventBus eventBus, RendezvousPollerImpl rendezvousPoller) {
		lifecycleManager.registerService(rendezvousPoller);
		eventBus.addListener(rendezvousPoller);
		return rendezvousPoller;
	}
}
