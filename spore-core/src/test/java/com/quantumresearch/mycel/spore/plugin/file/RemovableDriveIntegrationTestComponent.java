package com.quantumresearch.mycel.spore.plugin.file;

import com.quantumresearch.mycel.spore.SporeCoreEagerSingletons;
import com.quantumresearch.mycel.spore.SporeCoreModule;
import com.quantumresearch.mycel.spore.api.contact.ContactManager;
import com.quantumresearch.mycel.spore.api.event.EventBus;
import com.quantumresearch.mycel.spore.api.identity.IdentityManager;
import com.quantumresearch.mycel.spore.api.lifecycle.LifecycleManager;
import com.quantumresearch.mycel.spore.api.plugin.file.RemovableDriveManager;
import com.quantumresearch.mycel.spore.battery.DefaultBatteryManagerModule;
import com.quantumresearch.mycel.spore.event.DefaultEventExecutorModule;
import com.quantumresearch.mycel.spore.mailbox.ModularMailboxModule;
import com.quantumresearch.mycel.spore.system.DefaultThreadFactoryModule;
import com.quantumresearch.mycel.spore.system.DefaultWakefulIoExecutorModule;
import com.quantumresearch.mycel.spore.system.TimeTravelModule;
import com.quantumresearch.mycel.spore.test.TestDatabaseConfigModule;
import com.quantumresearch.mycel.spore.test.TestDnsModule;
import com.quantumresearch.mycel.spore.test.TestFeatureFlagModule;
import com.quantumresearch.mycel.spore.test.TestMailboxDirectoryModule;
import com.quantumresearch.mycel.spore.test.TestSecureRandomModule;
import com.quantumresearch.mycel.spore.test.TestSocksModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		SporeCoreModule.class,
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
		extends SporeCoreEagerSingletons {

	ContactManager getContactManager();

	EventBus getEventBus();

	IdentityManager getIdentityManager();

	LifecycleManager getLifecycleManager();

	RemovableDriveManager getRemovableDriveManager();

	class Helper {

		public static void injectEagerSingletons(
				RemovableDriveIntegrationTestComponent c) {
			SporeCoreEagerSingletons.Helper.injectEagerSingletons(c);
		}
	}
}
