package com.quantumresearch.mycel.app.messaging;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.BrambleCoreModule;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;
import com.quantumresearch.mycel.app.autodelete.AutoDeleteModule;
import com.quantumresearch.mycel.app.avatar.AvatarModule;
import com.quantumresearch.mycel.app.client.BriarClientModule;
import com.quantumresearch.mycel.app.conversation.ConversationModule;
import com.quantumresearch.mycel.app.forum.ForumModule;
import com.quantumresearch.mycel.app.identity.IdentityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		BriarClientModule.class,
		AutoDeleteModule.class,
		AvatarModule.class,
		ConversationModule.class,
		ForumModule.class,
		IdentityModule.class,
		MessagingModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface MessageSizeIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	void inject(MessageSizeIntegrationTest testCase);

	void inject(AvatarModule.EagerSingletons init);

	void inject(ForumModule.EagerSingletons init);

	void inject(MessagingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				MessageSizeIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new ForumModule.EagerSingletons());
			c.inject(new MessagingModule.EagerSingletons());
		}
	}
}
