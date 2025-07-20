package com.quantumresearch.mycel.infrastructure.api.connection;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.infrastructure.api.contact.PendingContactId;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionReader;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportConnectionWriter;
import com.quantumresearch.mycel.infrastructure.api.plugin.TransportId;
import com.quantumresearch.mycel.infrastructure.api.plugin.duplex.DuplexTransportConnection;
import com.quantumresearch.mycel.infrastructure.api.sync.OutgoingSessionRecord;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface ConnectionManager {

	/**
	 * Manages an incoming connection from a contact over a simplex transport.
	 */
	void manageIncomingConnection(TransportId t, TransportConnectionReader r);

	/**
	 * Manages an incoming connection from a contact via a mailbox.
	 * <p>
	 * This method does not mark the tag as recognised until after the data
	 * has been read from the {@link TransportConnectionReader}, at which
	 * point the {@link TagController} is called to decide whether the tag
	 * should be marked as recognised.
	 */
	void manageIncomingConnection(TransportId t, TransportConnectionReader r,
			TagController c);

	/**
	 * Manages an incoming connection from a contact over a duplex transport.
	 */
	void manageIncomingConnection(TransportId t, DuplexTransportConnection d);

	/**
	 * Manages an incoming handshake connection from a pending contact over a
	 * duplex transport.
	 */
	void manageIncomingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d);

	/**
	 * Manages an outgoing connection to a contact over a simplex transport.
	 */
	void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w);

	/**
	 * Manages an outgoing connection to a contact via a mailbox. The IDs of
	 * any messages sent or acked are added to the given
	 * {@link OutgoingSessionRecord}.
	 */
	void manageOutgoingConnection(ContactId c, TransportId t,
			TransportConnectionWriter w, OutgoingSessionRecord sessionRecord);

	/**
	 * Manages an outgoing connection to a contact over a duplex transport.
	 */
	void manageOutgoingConnection(ContactId c, TransportId t,
			DuplexTransportConnection d);

	/**
	 * Manages an outgoing handshake connection to a pending contact over a
	 * duplex transport.
	 */
	void manageOutgoingConnection(PendingContactId p, TransportId t,
			DuplexTransportConnection d);

	/**
	 * An interface for controlling whether a tag should be marked as
	 * recognised.
	 */
	interface TagController {
		/**
		 * This method is only called if a tag was read from the corresponding
		 * {@link TransportConnectionReader} and recognised.
		 *
		 * @param exception True if an exception was thrown while reading from
		 * the {@link TransportConnectionReader}, after successfully reading
		 * and recognising the tag.
		 * @return True if the tag should be marked as recognised.
		 */
		boolean shouldMarkTagAsRecognised(boolean exception);
	}
}
