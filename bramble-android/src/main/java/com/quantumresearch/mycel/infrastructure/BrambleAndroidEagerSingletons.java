package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.battery.AndroidBatteryModule;
import com.quantumresearch.mycel.infrastructure.network.AndroidNetworkModule;
import com.quantumresearch.mycel.infrastructure.reporting.ReportingModule;

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
