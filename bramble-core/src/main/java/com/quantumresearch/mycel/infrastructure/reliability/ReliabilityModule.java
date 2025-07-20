package com.quantumresearch.mycel.infrastructure.reliability;

import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.reliability.ReliabilityLayerFactory;

import java.util.concurrent.Executor;

import dagger.Module;
import dagger.Provides;

@Module
public class ReliabilityModule {

	@Provides
	ReliabilityLayerFactory provideReliabilityFactoryByExector(
			@IoExecutor Executor ioExecutor) {
		return new ReliabilityLayerFactoryImpl(ioExecutor);
	}

	@Provides
	ReliabilityLayerFactory provideReliabilityFactory(
			ReliabilityLayerFactoryImpl reliabilityLayerFactory) {
		return reliabilityLayerFactory;
	}

}
