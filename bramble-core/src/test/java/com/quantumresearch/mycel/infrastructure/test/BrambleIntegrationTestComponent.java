package com.quantumresearch.mycel.infrastructure.test;

import com.quantumresearch.mycel.infrastructure.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.api.client.ClientHelper;
import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.identity.IdentityManager;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;

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
public interface BrambleIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	IdentityManager getIdentityManager();

	EventBus getEventBus();

	ConnectionManager getConnectionManager();

	ClientHelper getClientHelper();

}
