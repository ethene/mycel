package com.quantumresearch.mycel.spore.test;

import javax.net.SocketFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class TestSocksModule {

	@Provides
	SocketFactory provideSocketFactory() {
		return SocketFactory.getDefault();
	}

}
