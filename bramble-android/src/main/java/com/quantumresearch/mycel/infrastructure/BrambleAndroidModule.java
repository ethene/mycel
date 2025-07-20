package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.battery.AndroidBatteryModule;
import com.quantumresearch.mycel.infrastructure.io.DnsModule;
import com.quantumresearch.mycel.infrastructure.network.AndroidNetworkModule;
import com.quantumresearch.mycel.infrastructure.plugin.tor.CircumventionModule;
import com.quantumresearch.mycel.infrastructure.reporting.ReportingModule;
import com.quantumresearch.mycel.infrastructure.socks.SocksModule;
import com.quantumresearch.mycel.infrastructure.system.AndroidSystemModule;
import com.quantumresearch.mycel.infrastructure.system.AndroidTaskSchedulerModule;
import com.quantumresearch.mycel.infrastructure.system.AndroidWakeLockModule;
import com.quantumresearch.mycel.infrastructure.system.AndroidWakefulIoExecutorModule;
import com.quantumresearch.mycel.infrastructure.system.DefaultThreadFactoryModule;

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
public class BrambleAndroidModule {
}
