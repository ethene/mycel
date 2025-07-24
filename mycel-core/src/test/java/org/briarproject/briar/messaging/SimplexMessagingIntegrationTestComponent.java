package com.quantumresearch.mycel.app.messaging;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.BrambleCoreModule;
import com.quantumresearch.mycel.spore.api.connection.ConnectionManager;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;
import com.quantumresearch.mycel.app.api.messaging.MessagingManager;
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageFactory;
import com.quantumresearch.mycel.app.autodelete.AutoDeleteModule;
import com.quantumresearch.mycel.app.client.BriarClientModule;
import com.quantumresearch.mycel.app.conversation.ConversationModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		AutoDeleteModule.class,
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		BriarClientModule.class,
		ConversationModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface SimplexMessagingIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	void inject(MessagingModule.EagerSingletons init);

	LifecycleManager getLifecycleManager();

	IdentityManager getIdentityManager();

	ContactManager getContactManager();

	MessagingManager getMessagingManager();

	PrivateMessageFactory getPrivateMessageFactory();

	EventBus getEventBus();

	ConnectionManager getConnectionManager();

	class Helper {

		public static void injectEagerSingletons(
				SimplexMessagingIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}
