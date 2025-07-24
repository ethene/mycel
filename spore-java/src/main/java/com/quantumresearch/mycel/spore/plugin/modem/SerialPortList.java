package com.quantumresearch.mycel.spore.plugin.modem;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface SerialPortList {

	String[] getPortNames();
}
