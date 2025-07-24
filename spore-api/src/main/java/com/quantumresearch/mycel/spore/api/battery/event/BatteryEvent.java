package com.quantumresearch.mycel.spore.api.battery.event;

import com.quantumresearch.mycel.spore.api.event.Event;

/**
 * An event that is broadcast when the device starts or stops charging.
 */
public class BatteryEvent extends Event {

	private final boolean charging;

	public BatteryEvent(boolean charging) {
		this.charging = charging;
	}

	public boolean isCharging() {
		return charging;
	}
}
