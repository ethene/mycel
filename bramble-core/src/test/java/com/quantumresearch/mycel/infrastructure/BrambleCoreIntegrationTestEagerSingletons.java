package com.quantumresearch.mycel.infrastructure;

import com.quantumresearch.mycel.infrastructure.system.TimeTravelModule;

public interface BrambleCoreIntegrationTestEagerSingletons
		extends BrambleCoreEagerSingletons {

	void inject(TimeTravelModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(
				BrambleCoreIntegrationTestEagerSingletons c) {
			BrambleCoreEagerSingletons.Helper.injectEagerSingletons(c);
			c.inject(new TimeTravelModule.EagerSingletons());
		}
	}
}
