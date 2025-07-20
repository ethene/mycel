package com.quantumresearch.mycel.infrastructure.transport.agreement;

import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.api.client.ContactGroupFactory;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactManager;
import com.quantumresearch.mycel.infrastructure.api.db.DatabaseComponent;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.infrastructure.api.transport.KeyManager;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.infrastructure.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.infrastructure.test.BrambleIntegrationTestComponent;
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
interface TransportKeyAgreementTestComponent
		extends BrambleIntegrationTestComponent {

	KeyManager getKeyManager();

	TransportKeyAgreementManagerImpl getTransportKeyAgreementManager();

	ContactManager getContactManager();

	LifecycleManager getLifecycleManager();

	ContactGroupFactory getContactGroupFactory();

	SessionParser getSessionParser();

	TransportPropertyManager getTransportPropertyManager();

	DatabaseComponent getDatabaseComponent();
}
