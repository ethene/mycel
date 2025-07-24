package com.quantumresearch.mycel.spore.mailbox;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.db.DatabaseComponent;
import com.quantumresearch.mycel.spore.api.identity.AuthorFactory;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxSettingsManager;
import com.quantumresearch.mycel.spore.api.mailbox.MailboxUpdateManager;
import com.quantumresearch.mycel.spore.api.properties.TransportPropertyManager;
import com.quantumresearch.mycel.spore.api.system.Clock;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.BrambleIntegrationTestComponent;
import com.quantumresearch.mycel.spore.test.MailboxTestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		SporeCoreModule.class,
		TestModularMailboxModule.class,
		MailboxTestPluginConfigModule.class,
		TestSocksModule.class,
		TestDnsModule.class,
})
interface MailboxIntegrationTestComponent extends
		BrambleIntegrationTestComponent {

	LifecycleManager getLifecycleManager();

	DatabaseComponent getDatabaseComponent();

	ContactManager getContactManager();

	AuthorFactory getAuthorFactory();

	Clock getClock();

	MailboxManager getMailboxManager();

	MailboxSettingsManager getMailboxSettingsManager();

	MailboxUpdateManager getMailboxUpdateManager();

	TransportPropertyManager getTransportPropertyManager();

	class Helper {
		static void injectEagerSingletons(
				MailboxIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
		}
	}
}
