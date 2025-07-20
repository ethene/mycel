package com.quantumresearch.mycel.infrastructure.api.lifecycle.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.lifecycle.LifecycleManager.LifecycleState;

/**
 * An event that is broadcast when the app enters a new lifecycle state.
 */
public class LifecycleEvent extends Event {

	private final LifecycleState state;

	public LifecycleEvent(LifecycleState state) {
		this.state = state;
	}

	public LifecycleState getLifecycleState() {
		return state;
	}
}
