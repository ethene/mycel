package com.quantumresearch.mycel.infrastructure.api.network;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface NetworkManager {

	NetworkStatus getNetworkStatus();
}
