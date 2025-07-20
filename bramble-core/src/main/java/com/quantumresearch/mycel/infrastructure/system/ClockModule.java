package com.quantumresearch.mycel.infrastructure.system;

import com.quantumresearch.mycel.infrastructure.api.system.Clock;

import dagger.Module;
import dagger.Provides;

@Module
public class ClockModule {

	@Provides
	Clock provideClock() {
		return new SystemClock();
	}
}
