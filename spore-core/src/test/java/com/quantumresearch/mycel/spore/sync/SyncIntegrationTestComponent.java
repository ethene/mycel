package com.quantumresearch.mycel.spore.sync;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		SporeCoreModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface SyncIntegrationTestComponent extends
		BrambleCoreIntegrationTestEagerSingletons {

	void inject(SyncIntegrationTest testCase);
}
