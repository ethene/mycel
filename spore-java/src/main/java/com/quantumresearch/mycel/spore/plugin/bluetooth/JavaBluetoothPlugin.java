package com.quantumresearch.mycel.spore.plugin.bluetooth;

import com.quantumresearch.mycel.spore.api.plugin.Backoff;
import com.quantumresearch.mycel.spore.api.plugin.PluginCallback;
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexTransportConnection;
import org.briarproject.nullsafety.MethodsNotNullByDefault;
import org.briarproject.nullsafety.ParametersNotNullByDefault;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import static java.util.logging.Level.WARNING;
import static java.util.logging.Logger.getLogger;
import static com.quantumresearch.mycel.spore.util.LogUtils.logException;
import static com.quantumresearch.mycel.spore.util.StringUtils.isValidMac;

@MethodsNotNullByDefault
@ParametersNotNullByDefault
class JavaBluetoothPlugin extends
		AbstractBluetoothPlugin<StreamConnection, StreamConnectionNotifier> {

	private static final Logger LOG =
			getLogger(JavaBluetoothPlugin.class.getName());

	// Non-null if the plugin started successfully
	private volatile LocalDevice localDevice = null;

	JavaBluetoothPlugin(BluetoothConnectionLimiter connectionManager,
			BluetoothConnectionFactory<StreamConnection> connectionFactory,
			Executor ioExecutor,
			Executor wakefulIoExecutor,
			SecureRandom secureRandom,
			Backoff backoff,
			PluginCallback callback,
			long maxLatency,
			int maxIdleTime) {
		super(connectionManager, connectionFactory, ioExecutor,
				wakefulIoExecutor, secureRandom, backoff, callback,
				maxLatency, maxIdleTime);
	}

	@Override
	void initialiseAdapter() throws IOException {
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (UnsatisfiedLinkError | BluetoothStateException e) {
			throw new IOException(e);
		}
	}

	@Override
	boolean isAdapterEnabled() {
		return localDevice != null && LocalDevice.isPowerOn();
	}

	@Nullable
	@Override
	String getBluetoothAddress() {
		return localDevice.getBluetoothAddress();
	}

	@Override
	StreamConnectionNotifier openServerSocket(String uuid) throws IOException {
		String url = makeUrl("localhost", uuid);
		return (StreamConnectionNotifier) Connector.open(url);
	}

	@Override
	void tryToClose(@Nullable StreamConnectionNotifier ss) {
		try {
			if (ss != null) ss.close();
		} catch (IOException e) {
			logException(LOG, WARNING, e);
		}
	}

	@Override
	DuplexTransportConnection acceptConnection(StreamConnectionNotifier ss)
			throws IOException {
		return connectionFactory.wrapSocket(this, ss.acceptAndOpen());
	}

	@Override
	boolean isValidAddress(String address) {
		return isValidMac(address);
	}

	@Override
	DuplexTransportConnection connectTo(String address, String uuid)
			throws IOException {
		String url = makeUrl(address, uuid);
		StreamConnection s = (StreamConnection) Connector.open(url);
		return connectionFactory.wrapSocket(this, s);
	}

	@Override
	@Nullable
	DuplexTransportConnection discoverAndConnect(String uuid) {
		return null; // TODO
	}

	@Override
	public void stopDiscoverAndConnect() {
		// TODO
	}

	private String makeUrl(String address, String uuid) {
		return "btspp://" + address + ":" + uuid + ";name=RFCOMM";
	}
}
