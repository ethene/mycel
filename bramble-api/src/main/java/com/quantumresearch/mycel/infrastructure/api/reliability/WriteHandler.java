package com.quantumresearch.mycel.infrastructure.api.reliability;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface WriteHandler {

	void handleWrite(byte[] b) throws IOException;
}
