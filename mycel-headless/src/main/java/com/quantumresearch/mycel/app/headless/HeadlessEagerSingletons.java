package com.quantumresearch.mycel.app.headless;

import com.quantumresearch.mycel.spore.system.DefaultTaskSchedulerModule;

public interface HeadlessEagerSingletons {

	void inject(DefaultTaskSchedulerModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(HeadlessEagerSingletons c) {
			c.inject(new DefaultTaskSchedulerModule.EagerSingletons());
		}
	}
}
