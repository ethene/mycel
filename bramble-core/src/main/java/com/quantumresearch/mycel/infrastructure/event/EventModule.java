package com.quantumresearch.mycel.infrastructure.event;

import com.quantumresearch.mycel.infrastructure.api.event.EventBus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class EventModule {

	@Provides
	@Singleton
	EventBus provideEventBus(EventBusImpl eventBus) {
		return eventBus;
	}
}
