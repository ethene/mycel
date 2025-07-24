package com.quantumresearch.mycel.spore.rendezvous;

import com.quantumresearch.mycel.spore.api.crypto.SecretKey;
import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.rendezvous.KeyMaterialSource;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface RendezvousCrypto {

	SecretKey deriveRendezvousKey(SecretKey staticMasterKey);

	KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t);
}
