package com.quantumresearch.mycel.infrastructure.api.reliability;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ReliabilityLayerFactory {

	/**
	 * Returns a reliability layer that writes to the given lower layer.
	 */
	ReliabilityLayer createReliabilityLayer(WriteHandler writeHandler);
}
