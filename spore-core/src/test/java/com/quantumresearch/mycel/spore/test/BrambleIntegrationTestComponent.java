package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.api.client.ClientHelper;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;

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
public interface BrambleIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	IdentityManager getIdentityManager();

	EventBus getEventBus();

	ConnectionManager getConnectionManager();

	ClientHelper getClientHelper();

}
