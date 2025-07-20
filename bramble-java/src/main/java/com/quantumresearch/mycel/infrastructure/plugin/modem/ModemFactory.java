package com.quantumresearch.mycel.infrastructure.plugin.modem;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
interface ModemFactory {

	Modem createModem(Modem.Callback callback, String portName);
}
