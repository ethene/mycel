package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.infrastructure.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.infrastructure.test.TestDnsModule;
import com.quantumresearch.mycel.infrastructure.test.TestPluginConfigModule;
import com.quantumresearch.mycel.infrastructure.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface SyncIntegrationTestComponent extends
		BrambleCoreIntegrationTestEagerSingletons {

	void inject(SyncIntegrationTest testCase);
}
