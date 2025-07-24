package com.quantumresearch.mycel.spore.api.network;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface NetworkManager {

	NetworkStatus getNetworkStatus();
}
