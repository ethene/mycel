package com.quantumresearch.mycel.spore.contact;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;

import java.util.concurrent.Executor;

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
interface ContactExchangeIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	ConnectionManager getConnectionManager();

	ContactExchangeManager getContactExchangeManager();

	ContactManager getContactManager();

	EventBus getEventBus();

	IdentityManager getIdentityManager();

	@IoExecutor
	Executor getIoExecutor();

	LifecycleManager getLifecycleManager();
}
