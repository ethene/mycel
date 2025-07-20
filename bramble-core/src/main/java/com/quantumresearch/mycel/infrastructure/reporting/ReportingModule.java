package com.quantumresearch.mycel.infrastructure.reporting;

import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.reporting.DevReporter;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ReportingModule {

	public static class EagerSingletons {
		@Inject
		DevReporter devReporter;
	}

	@Provides
	@Singleton
	DevReporter provideDevReporter(DevReporterImpl devReporter,
			EventBus eventBus) {
		eventBus.addListener(devReporter);
		return devReporter;
	}
}
