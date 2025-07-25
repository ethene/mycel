package com.quantumresearch.mycel.spore.api.keyagreement;

import com.quantumresearch.mycel.spore.api.plugin.TransportId;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class KeyAgreementConnection {

	private final DuplexTransportConnection conn;
	private final TransportId id;

	public KeyAgreementConnection(DuplexTransportConnection conn,
			TransportId id) {
		this.conn = conn;
		this.id = id;
	}

	public DuplexTransportConnection getConnection() {
		return conn;
	}

	public TransportId getTransportId() {
		return id;
	}
}
