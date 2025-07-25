package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.battery.AndroidBatteryModule;
import com.quantumresearch.mycel.spore.io.DnsModule;
import com.quantumresearch.mycel.spore.network.AndroidNetworkModule;
import com.quantumresearch.mycel.spore.plugin.tor.CircumventionModule;
import com.quantumresearch.mycel.spore.reporting.ReportingModule;
import com.quantumresearch.mycel.spore.socks.SocksModule;
import com.quantumresearch.mycel.spore.system.AndroidSystemModule;
import com.quantumresearch.mycel.spore.system.AndroidTaskSchedulerModule;
import com.quantumresearch.mycel.spore.system.AndroidWakeLockModule;
import com.quantumresearch.mycel.spore.system.AndroidWakefulIoExecutorModule;
import com.quantumresearch.mycel.spore.system.DefaultThreadFactoryModule;

import dagger.Module;

@Module(includes = {
		AndroidBatteryModule.class,
		AndroidNetworkModule.class,
		AndroidSystemModule.class,
		AndroidTaskSchedulerModule.class,
		AndroidWakefulIoExecutorModule.class,
		AndroidWakeLockModule.class,
		DefaultThreadFactoryModule.class,
		CircumventionModule.class,
		DnsModule.class,
		ReportingModule.class,
		SocksModule.class
})
public class SporeAndroidModule {
}
