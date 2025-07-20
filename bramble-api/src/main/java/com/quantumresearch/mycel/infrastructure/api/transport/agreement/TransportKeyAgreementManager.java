package com.quantumresearch.mycel.infrastructure.api.transport.agreement;

import com.quantumresearch.mycel.infrastructure.api.sync.ClientId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface TransportKeyAgreementManager {

	/**
	 * The unique ID of the transport key agreement client.
	 */
	ClientId CLIENT_ID =
			new ClientId("org.briarproject.bramble.transport.agreement");

	/**
	 * The current major version of the transport key agreement client.
	 */
	int MAJOR_VERSION = 0;

	/**
	 * The current minor version of the transport key agreement client.
	 */
	int MINOR_VERSION = 0;
}
