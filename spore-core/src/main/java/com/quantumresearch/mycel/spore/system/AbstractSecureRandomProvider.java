package com.quantumresearch.mycel.spore.system;

import com.quantumresearch.mycel.spore.api.system.SecureRandomProvider;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.concurrent.Immutable;

import static java.util.Collections.list;
import static com.quantumresearch.mycel.spore.util.NetworkUtils.getNetworkInterfaces;

@Immutable
@NotNullByDefault
abstract class AbstractSecureRandomProvider implements SecureRandomProvider {

	// Contribute whatever slightly unpredictable info we have to the pool
	protected void writeToEntropyPool(DataOutputStream out) throws IOException {
		out.writeLong(System.currentTimeMillis());
		out.writeLong(System.nanoTime());
		out.writeLong(Runtime.getRuntime().freeMemory());
		for (NetworkInterface i : getNetworkInterfaces()) {
			for (InetAddress a : list(i.getInetAddresses()))
				out.write(a.getAddress());
			byte[] hardware = i.getHardwareAddress();
			if (hardware != null) out.write(hardware);
		}
		for (Entry<String, String> e : System.getenv().entrySet()) {
			out.writeUTF(e.getKey());
			out.writeUTF(e.getValue());
		}
		Properties properties = System.getProperties();
		for (String key : properties.stringPropertyNames())
			out.writeUTF(properties.getProperty(key));
	}
}
