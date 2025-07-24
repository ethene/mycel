package com.quantumresearch.mycel.app.client;

import com.quantumresearch.mycel.app.api.client.MessageTracker;

import dagger.Module;
import dagger.Provides;

@Module
public class BriarClientModule {

	@Provides
	MessageTracker provideMessageTracker(MessageTrackerImpl messageTracker) {
		return messageTracker;
	}
}
