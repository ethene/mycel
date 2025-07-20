package com.quantumresearch.mycel.infrastructure.rendezvous;

import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.rendezvous.KeyMaterialSource;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RendezvousCrypto {

	SecretKey deriveRendezvousKey(SecretKey staticMasterKey);

	KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t);
}
