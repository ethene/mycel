package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.system.TimeTravelModule;

public interface BrambleCoreIntegrationTestEagerSingletons
		extends SporeCoreEagerSingletons {

	void inject(TimeTravelModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				BrambleCoreIntegrationTestEagerSingletons c) {
			SporeCoreEagerSingletons.Helper.injectEagerSingletons(c);
			c.inject(new TimeTravelModule.EagerSingletons());
		}
	}
}
