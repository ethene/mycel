package com.quantumresearch.mycel.spore.test;

import dagger.Module;
import dagger.Provides;
import okhttp3.Dns;

@Module
public class TestDnsModule {

	@Provides
	Dns provideDns() {
		return Dns.SYSTEM;
	}

}
