package com.quantumresearch.mycel.spore.api.keyagreement;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface PayloadEncoder {

	byte[] encode(Payload p);
}
