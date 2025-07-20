package org.briarproject.briar.android.logging;

import com.quantumresearch.mycel.infrastructure.BrambleCoreModule;
import com.quantumresearch.mycel.infrastructure.system.ClockModule;
import com.quantumresearch.mycel.infrastructure.test.TestSecureRandomModule;

import java.security.SecureRandom;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
		ClockModule.class,
		BrambleCoreModule.class,
		TestSecureRandomModule.class,
		LoggingModule.class,
		LoggingTestModule.class,
})
public interface LoggingComponent {

	SecureRandom random();

	CachingLogHandler cachingLogHandler();

	LogEncrypter logEncrypter();

	LogDecrypter logDecrypter();

}
