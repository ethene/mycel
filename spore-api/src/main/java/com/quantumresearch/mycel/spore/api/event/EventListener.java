package com.quantumresearch.mycel.spore.api.event;

import org.briarproject.nullsafety.NotNullByDefault;

/**
 * An interface for receiving notifications when events occur.
 */
@NotNullByDefault
public interface EventListener {

	/**
	 * Called when an event is broadcast. Implementations of this method must
	 * not block.
	 */
	@EventExecutor
	void eventOccurred(Event e);
}
