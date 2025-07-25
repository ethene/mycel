package com.quantumresearch.mycel.spore.util;

import org.briarproject.nullsafety.NotNullByDefault;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;

@NotNullByDefault
public class NetworkUtils {

	private static final Logger LOG = getLogger(NetworkUtils.class.getName());

	public static List<NetworkInterface> getNetworkInterfaces() {
		try {
			Enumeration<NetworkInterface> ifaces =
					NetworkInterface.getNetworkInterfaces();
			// Despite what the docs say, the return value can be null
			//noinspection ConstantConditions
			return ifaces == null ? emptyList() : list(ifaces);
		} catch (SocketException | NullPointerException e) {
			logException(LOG, WARNING, e);
			return emptyList();
		}
	}
}
