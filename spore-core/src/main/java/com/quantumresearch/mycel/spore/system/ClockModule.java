package com.quantumresearch.mycel.spore.system;

import com.quantumresearch.mycel.spore.api.system.Clock;

import dagger.Module;
import dagger.Provides;

@Module
public class ClockModule {

	@Provides
	Clock provideClock() {
		return new SystemClock();
	}
}
