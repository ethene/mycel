package com.quantumresearch.mycel.spore.reliability;

import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.reliability.ReliabilityLayerFactory;

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
