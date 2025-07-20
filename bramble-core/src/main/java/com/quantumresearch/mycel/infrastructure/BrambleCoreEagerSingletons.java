package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.cleanup.CleanupModule;
import com.quantumresearch.mycel.infrastructure.contact.ContactModule;
import com.quantumresearch.mycel.infrastructure.crypto.CryptoExecutorModule;
import com.quantumresearch.mycel.infrastructure.db.DatabaseExecutorModule;
import com.quantumresearch.mycel.infrastructure.identity.IdentityModule;
import com.quantumresearch.mycel.infrastructure.lifecycle.LifecycleModule;
import com.quantumresearch.mycel.infrastructure.mailbox.MailboxModule;
import com.quantumresearch.mycel.infrastructure.plugin.PluginModule;
import com.quantumresearch.mycel.infrastructure.properties.PropertiesModule;
import com.quantumresearch.mycel.infrastructure.rendezvous.RendezvousModule;
import com.quantumresearch.mycel.infrastructure.sync.validation.ValidationModule;
import com.quantumresearch.mycel.infrastructure.transport.TransportModule;
import com.quantumresearch.mycel.infrastructure.transport.agreement.TransportKeyAgreementModule;
import com.quantumresearch.mycel.infrastructure.versioning.VersioningModule;

public interface BrambleCoreEagerSingletons {

	void inject(CleanupModule.EagerSingletons init);

	void inject(ContactModule.EagerSingletons init);

	void inject(CryptoExecutorModule.EagerSingletons init);

	void inject(DatabaseExecutorModule.EagerSingletons init);

	void inject(IdentityModule.EagerSingletons init);

	void inject(LifecycleModule.EagerSingletons init);

	void inject(MailboxModule.EagerSingletons init);

	void inject(PluginModule.EagerSingletons init);

	void inject(PropertiesModule.EagerSingletons init);

	void inject(RendezvousModule.EagerSingletons init);

	void inject(TransportKeyAgreementModule.EagerSingletons init);

	void inject(TransportModule.EagerSingletons init);

	void inject(ValidationModule.EagerSingletons init);

	void inject(VersioningModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(BrambleCoreEagerSingletons c) {
			c.inject(new CleanupModule.EagerSingletons());
			c.inject(new ContactModule.EagerSingletons());
			c.inject(new CryptoExecutorModule.EagerSingletons());
			c.inject(new DatabaseExecutorModule.EagerSingletons());
			c.inject(new IdentityModule.EagerSingletons());
			c.inject(new LifecycleModule.EagerSingletons());
			c.inject(new MailboxModule.EagerSingletons());
			c.inject(new RendezvousModule.EagerSingletons());
			c.inject(new PluginModule.EagerSingletons());
			c.inject(new PropertiesModule.EagerSingletons());
			c.inject(new TransportKeyAgreementModule.EagerSingletons());
			c.inject(new TransportModule.EagerSingletons());
			c.inject(new ValidationModule.EagerSingletons());
			c.inject(new VersioningModule.EagerSingletons());
		}
	}
}
