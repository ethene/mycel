package com.quantumresearch.mycel.app.feed;

import com.quantumresearch.mycel.spore.BrambleCoreIntegrationTestEagerSingletons;
import com.quantumresearch.mycel.spore.BrambleCoreModule;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.test.BrambleCoreIntegrationTestModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestPluginConfigModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;
import com.quantumresearch.mycel.app.api.blog.BlogManager;
import com.quantumresearch.mycel.app.api.feed.FeedManager;
import com.quantumresearch.mycel.app.avatar.AvatarModule;
import com.quantumresearch.mycel.app.blog.BlogModule;
import com.quantumresearch.mycel.app.client.BriarClientModule;
import com.quantumresearch.mycel.app.identity.IdentityModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreIntegrationTestModule.class,
		BrambleCoreModule.class,
		AvatarModule.class,
		BlogModule.class,
		BriarClientModule.class,
		FeedModule.class,
		IdentityModule.class,
		ModularMailboxModule.class,
		TestDnsModule.class,
		TestSocksModule.class,
		TestPluginConfigModule.class,
})
interface FeedManagerIntegrationTestComponent
		extends BrambleCoreIntegrationTestEagerSingletons {

	void inject(FeedManagerIntegrationTest testCase);

	void inject(AvatarModule.EagerSingletons init);

	void inject(BlogModule.EagerSingletons init);

	void inject(FeedModule.EagerSingletons init);

	IdentityManager getIdentityManager();

	LifecycleManager getLifecycleManager();

	FeedManager getFeedManager();

	BlogManager getBlogManager();

	class Helper {

		public static void injectEagerSingletons(
				FeedManagerIntegrationTestComponent c) {
			BrambleCoreIntegrationTestEagerSingletons.Helper
					.injectEagerSingletons(c);
			c.inject(new AvatarModule.EagerSingletons());
			c.inject(new BlogModule.EagerSingletons());
			c.inject(new FeedModule.EagerSingletons());
		}
	}
}
