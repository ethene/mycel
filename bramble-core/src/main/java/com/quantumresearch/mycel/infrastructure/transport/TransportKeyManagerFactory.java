package com.quantumresearch.mycel.infrastructure.transport;

import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface TransportKeyManagerFactory {

	TransportKeyManager createTransportKeyManager(TransportId transportId,
			long maxLatency);

}
