package com.quantumresearch.mycel.spore;

import com.quantumresearch.mycel.spore.network.JavaNetworkModule;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface SporeJavaEagerSingletons {

	void inject(JavaNetworkModule.EagerSingletons init);

	class Helper {

		public static void injectEagerSingletons(SporeJavaEagerSingletons c) {
			c.inject(new JavaNetworkModule.EagerSingletons());
		}
	}
}
