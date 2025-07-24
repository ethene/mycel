package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.battery.AndroidBatteryModule;
import com.quantumresearch.mycel.spore.network.AndroidNetworkModule;
import com.quantumresearch.mycel.spore.reporting.ReportingModule;

public interface BrambleAndroidEagerSingletons {

	void inject(AndroidBatteryModule.EagerSingletons init);

	void inject(AndroidNetworkModule.EagerSingletons init);

	void inject(ReportingModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				BrambleAndroidEagerSingletons c) {
			c.inject(new AndroidBatteryModule.EagerSingletons());
			c.inject(new AndroidNetworkModule.EagerSingletons());
			c.inject(new ReportingModule.EagerSingletons());
		}
	}
}
