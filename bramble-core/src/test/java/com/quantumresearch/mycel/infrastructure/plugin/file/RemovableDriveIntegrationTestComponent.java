package com.quantumresearch.mycel.infrastructure.plugin.file;

import com.quantumresearch.mycel.infrastructure.BrambleCoreEagerSingletons;
import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.api.contact.ContactManager;
import com.quantumresearch.mycel.infrastructure.api.event.EventBus;
import com.quantumresearch.mycel.infrastructure.api.identity.IdentityManager;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.infrastructure.api.plugin.file.RemovableDriveManager;
import com.quantumresearch.mycel.infrastructure.battery.DefaultBatteryManagerModule;
import com.quantumresearch.mycel.infrastructure.event.DefaultEventExecutorModule;
import com.quantumresearch.mycel.infrastructure.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.infrastructure.system.DefaultThreadFactoryModule;
import com.quantumresearch.mycel.infrastructure.system.DefaultWakefulIoExecutorModule;
import com.quantumresearch.mycel.infrastructure.system.TimeTravelModule;
import com.quantumresearch.mycel.infrastructure.test.TestDatabaseConfigModule;
import com.quantumresearch.mycel.infrastructure.test.TestDnsModule;
import com.quantumresearch.mycel.infrastructure.test.TestFeatureFlagModule;
import com.quantumresearch.mycel.infrastructure.test.TestMailboxDirectoryModule;
import com.quantumresearch.mycel.infrastructure.test.TestSecureRandomModule;
import com.quantumresearch.mycel.infrastructure.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		BrambleCoreModule.class,
		DefaultBatteryManagerModule.class,
		DefaultEventExecutorModule.class,
		DefaultWakefulIoExecutorModule.class,
		DefaultThreadFactoryModule.class,
		TestDatabaseConfigModule.class,
		TestDnsModule.class,
		TestFeatureFlagModule.class,
		TestMailboxDirectoryModule.class,
		RemovableDriveIntegrationTestModule.class,
		RemovableDriveModule.class,
		ModularMailboxModule.class,
		TestSecureRandomModule.class,
		TimeTravelModule.class,
		TestSocksModule.class,
})
interface RemovableDriveIntegrationTestComponent
		extends BrambleCoreEagerSingletons {

	ContactManager getContactManager();

	EventBus getEventBus();

	IdentityManager getIdentityManager();

	LifecycleManager getLifecycleManager();

	RemovableDriveManager getRemovableDriveManager();

	class Helper {

		public static void injectEagerSingletons(
				RemovableDriveIntegrationTestComponent c) {
			BrambleCoreEagerSingletons.Helper.injectEagerSingletons(c);
		}
	}
}
