package com.quantumresearch.mycel.spore.api.plugin.duplex;

import com.quantumresearch.mycel.spore.api.data.BdfList;
import com.quantumresearch.mycel.spore.api.keyagreement.KeyAgreementListener;
import com.quantumresearch.mycel.spore.api.plugin.ConnectionHandler;
import com.quantumresearch.mycel.spore.api.plugin.Plugin;
import com.quantumresearch.mycel.spore.api.properties.TransportProperties;
import com.quantumresearch.mycel.spore.api.rendezvous.KeyMaterialSource;
import com.quantumresearch.mycel.spore.api.rendezvous.RendezvousEndpoint;
import com.quantumresearch.mycel.spore.api.system.Wakeful;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;

/**
 * An interface for transport plugins that support duplex communication.
 */
@NotNullByDefault
public interface DuplexPlugin extends Plugin {

	/**
	 * Attempts to create and return a connection using the given transport
	 * properties. Returns null if a connection cannot be created.
	 */
	@Wakeful
	@Nullable
	DuplexTransportConnection createConnection(TransportProperties p);

	/**
	 * Returns true if the plugin supports short-range key agreement.
	 */
	boolean supportsKeyAgreement();

	/**
	 * Attempts to create and return a listener that can be used to perform key
	 * agreement. Returns null if a listener cannot be created.
	 */
	@Nullable
	KeyAgreementListener createKeyAgreementListener(byte[] localCommitment);

	/**
	 * Attempts to connect to the remote peer specified in the given descriptor.
	 * Returns null if no connection can be established.
	 */
	@Nullable
	DuplexTransportConnection createKeyAgreementConnection(
			byte[] remoteCommitment, BdfList descriptor);

	/**
	 * Returns true if the plugin supports rendezvous connections.
	 */
	boolean supportsRendezvous();

	/**
	 * Creates and returns an endpoint that uses the given key material to
	 * rendezvous with a pending contact, and the given connection handler to
	 * handle incoming connections. Returns null if an endpoint cannot be
	 * created.
	 */
	@Nullable
	RendezvousEndpoint createRendezvousEndpoint(KeyMaterialSource k,
			boolean alice, ConnectionHandler incoming);
}
