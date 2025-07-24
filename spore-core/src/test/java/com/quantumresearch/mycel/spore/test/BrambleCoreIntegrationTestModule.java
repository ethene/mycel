package com.quantumresearch.mycel.spore.test;

import com.quantumresearch.mycel.spore.battery.DefaultBatteryManagerModule;
import com.quantumresearch.mycel.spore.event.DefaultEventExecutorModule;
import com.quantumresearch.mycel.spore.system.DefaultWakefulIoExecutorModule;
import com.quantumresearch.mycel.spore.system.TimeTravelModule;

import dagger.Module;

@Module(includes = {
		DefaultBatteryManagerModule.class,
		DefaultEventExecutorModule.class,
		DefaultWakefulIoExecutorModule.class,
		TestThreadFactoryModule.class,
		TestDatabaseConfigModule.class,
		TestFeatureFlagModule.class,
		TestMailboxDirectoryModule.class,
		TestSecureRandomModule.class,
		TimeTravelModule.class
})
public class BrambleCoreIntegrationTestModule {

}
