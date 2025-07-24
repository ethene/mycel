package com.quantumresearch.mycel.spore.transport;

import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface TransportKeyManagerFactory {

	TransportKeyManager createTransportKeyManager(TransportId transportId,
			long maxLatency);

}
