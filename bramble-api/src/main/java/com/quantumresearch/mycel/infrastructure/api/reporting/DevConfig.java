package com.quantumresearch.mycel.infrastructure.api.reporting;

import com.quantumresearch.mycel.infrastructure.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

@NotNullByDefault
public interface DevConfig {

	PublicKey getDevPublicKey();

	String getDevOnionAddress();

	File getReportDir();

	File getLogcatFile();
}
