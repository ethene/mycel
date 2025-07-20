package com.quantumresearch.mycel.infrastructure.rendezvous;

import com.quantumresearch.mycel.infrastructure.api.crypto.CryptoComponent;
import com.quantumresearch.mycel.infrastructure.api.crypto.SecretKey;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.rendezvous.KeyMaterialSource;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.infrastructure.rendezvous.RendezvousConstants.KEY_MATERIAL_LABEL;
import static com.quantumresearch.mycel.infrastructure.rendezvous.RendezvousConstants.PROTOCOL_VERSION;
import static com.quantumresearch.mycel.infrastructure.rendezvous.RendezvousConstants.RENDEZVOUS_KEY_LABEL;
import static com.quantumresearch.mycel.infrastructure.util.StringUtils.toUtf8;

@Immutable
@NotNullByDefault
class RendezvousCryptoImpl implements RendezvousCrypto {

	private final CryptoComponent crypto;

	@Inject
	RendezvousCryptoImpl(CryptoComponent crypto) {
		this.crypto = crypto;
	}

	@Override
	public SecretKey deriveRendezvousKey(SecretKey staticMasterKey) {
		return crypto.deriveKey(RENDEZVOUS_KEY_LABEL, staticMasterKey,
				new byte[] {PROTOCOL_VERSION});
	}

	@Override
	public KeyMaterialSource createKeyMaterialSource(SecretKey rendezvousKey,
			TransportId t) {
		SecretKey sourceKey = crypto.deriveKey(KEY_MATERIAL_LABEL,
				rendezvousKey, toUtf8(t.getString()));
		return new KeyMaterialSourceImpl(sourceKey);
	}
}
