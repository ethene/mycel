package com.quantumresearch.mycel.infrastructure.plugin.modem;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SerialPortList {

	String[] getPortNames();
}
