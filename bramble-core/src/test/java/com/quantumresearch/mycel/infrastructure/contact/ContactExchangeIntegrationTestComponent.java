package com.quantumresearch.mycel.infrastructure.contact;

import com.quantumresearch.mycel.infrastructure.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.api.connection.ConnectionManager;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactExchangeManager;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.identity.IdentityManager;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.IoExecutor;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.infrastructure.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.infrastructure.test.TestDnsModule;
import com.quantumresearch.mycel.infrastructure.test.TestPluginConfigModule;
import com.quantumresearch.mycel.infrastructure.test.TestSocksModule;

import java.util.concurrent.Executor;

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
