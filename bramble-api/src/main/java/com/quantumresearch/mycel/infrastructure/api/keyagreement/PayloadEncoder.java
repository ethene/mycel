package com.quantumresearch.mycel.infrastructure.api.keyagreement;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PayloadEncoder {

	byte[] encode(Payload p);
}
