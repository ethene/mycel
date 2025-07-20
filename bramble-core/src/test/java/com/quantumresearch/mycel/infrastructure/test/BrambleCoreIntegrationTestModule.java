package com.quantumresearch.mycel.infrastructure.test;

import com.quantumresearch.mycel.infrastructure.battery.DefaultBatteryManagerModule;
import com.quantumresearch.mycel.infrastructure.event.DefaultEventExecutorModule;
import com.quantumresearch.mycel.infrastructure.system.DefaultWakefulIoExecutorModule;
import com.quantumresearch.mycel.infrastructure.system.TimeTravelModule;

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
