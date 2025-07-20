package org.briarproject.briar.android.logging;

import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import com.quantumresearch.mycel.infrastructure.api.reporting.DevConfig;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
class LoggingTestModule {

	private final File logFile;

	LoggingTestModule(File logFile) {
		this.logFile = logFile;
	}

	@Provides
	@Singleton
	DevConfig provideDevConfig() {
		@NotNullByDefault
		DevConfig devConfig = new DevConfig() {
			@Override
			public PublicKey getDevPublicKey() {
				throw new UnsupportedOperationException();

			}

			@Override
			public String getDevOnionAddress() {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getReportDir() {
				throw new UnsupportedOperationException();
			}

			@Override
			public File getLogcatFile() {
				return logFile;
			}
		};
		return devConfig;
	}

}
