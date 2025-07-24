package com.quantumresearch.mycel.spore.api.reporting;

import com.quantumresearch.mycel.spore.api.crypto.PublicKey;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.File;

@NotNullByDefault
public interface DevConfig {

	PublicKey getDevPublicKey();

	String getDevOnionAddress();

	File getReportDir();

	File getLogcatFile();
}
