package com.quantumresearch.mycel.spore.sync;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.UniqueId;
import com.quantumresearch.mycel.spore.api.record.Record;
import com.quantumresearch.mycel.spore.api.record.RecordReader;
import com.quantumresearch.mycel.spore.api.record.RecordReader.RecordPredicate;
import com.quantumresearch.mycel.spore.api.sync.Ack;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageFactory;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.spore.api.sync.Offer;
import com.quantumresearch.mycel.spore.api.sync.Priority;
import com.quantumresearch.mycel.spore.api.sync.Request;
import com.quantumresearch.mycel.spore.api.sync.SyncRecordReader;
import com.quantumresearch.mycel.spore.api.sync.Versions;
import com.quantumresearch.mycel.spore.util.ByteUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.ACK;
import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.MESSAGE;
import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.OFFER;
import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.PRIORITY;
import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.REQUEST;
import static com.quantumresearch.mycel.spore.api.sync.RecordTypes.VERSIONS;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_MESSAGE_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MAX_SUPPORTED_VERSIONS;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.MESSAGE_HEADER_LENGTH;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.PRIORITY_NONCE_BYTES;
import static com.quantumresearch.mycel.spore.api.sync.SyncConstants.PROTOCOL_VERSION;

@NotThreadSafe
@NotNullByDefault
class SyncRecordReaderImpl implements SyncRecordReader {

	// Accept records with current protocol version, known record type
	private static final RecordPredicate ACCEPT = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					isKnownRecordType(r.getRecordType());

	// Ignore records with current protocol version, unknown record type
	private static final RecordPredicate IGNORE = r ->
			r.getProtocolVersion() == PROTOCOL_VERSION &&
					!isKnownRecordType(r.getRecordType());

	private static boolean isKnownRecordType(byte type) {
		return type == ACK || type == MESSAGE || type == OFFER ||
				type == REQUEST || type == VERSIONS || type == PRIORITY;
	}

	private final MessageFactory messageFactory;
	private final RecordReader reader;

	@Nullable
	private Record nextRecord = null;
	private boolean eof = false;

	SyncRecordReaderImpl(MessageFactory messageFactory, RecordReader reader) {
		this.messageFactory = messageFactory;
		this.reader = reader;
	}

	private byte getNextRecordType() {
		if (nextRecord == null) throw new AssertionError();
		return nextRecord.getRecordType();
	}

	/**
	 * Returns true if there's another record available or false if we've
	 * reached the end of the input stream.
	 * <p>
	 * If a record is available, it's been read into the buffer by the time
	 * eof() returns, so the method that called eof() can access the record
	 * from the buffer, for example to check its type or extract its payload.
	 */
	@Override
	public boolean eof() throws IOException {
		if (nextRecord != null) return false;
		if (eof) return true;
		nextRecord = reader.readRecord(ACCEPT, IGNORE);
		if (nextRecord == null) eof = true;
		return eof;
	}

	@Override
	public boolean hasAck() throws IOException {
		return !eof() && getNextRecordType() == ACK;
	}

	@Override
	public Ack readAck() throws IOException {
		if (!hasAck()) throw new FormatException();
		return new Ack(readMessageIds());
	}

	private List<MessageId> readMessageIds() throws IOException {
		if (nextRecord == null) throw new AssertionError();
		byte[] payload = nextRecord.getPayload();
		if (payload.length == 0) throw new FormatException();
		if (payload.length % UniqueId.LENGTH != 0) throw new FormatException();
		List<MessageId> ids = new ArrayList<>(payload.length / UniqueId.LENGTH);
		for (int off = 0; off < payload.length; off += UniqueId.LENGTH) {
			byte[] id = new byte[UniqueId.LENGTH];
			System.arraycopy(payload, off, id, 0, UniqueId.LENGTH);
			ids.add(new MessageId(id));
		}
		nextRecord = null;
		return ids;
	}

	@Override
	public boolean hasMessage() throws IOException {
		return !eof() && getNextRecordType() == MESSAGE;
	}

	@Override
	public Message readMessage() throws IOException {
		if (!hasMessage()) throw new FormatException();
		if (nextRecord == null) throw new AssertionError();
		byte[] payload = nextRecord.getPayload();
		if (payload.length <= MESSAGE_HEADER_LENGTH)
			throw new FormatException();
		if (payload.length > MAX_MESSAGE_LENGTH)
			throw new FormatException();
		// Validate timestamp
		long timestamp = ByteUtils.readUint64(payload, UniqueId.LENGTH);
		if (timestamp < 0) throw new FormatException();
		nextRecord = null;
		return messageFactory.createMessage(payload);
	}

	@Override
	public boolean hasOffer() throws IOException {
		return !eof() && getNextRecordType() == OFFER;
	}

	@Override
	public Offer readOffer() throws IOException {
		if (!hasOffer()) throw new FormatException();
		return new Offer(readMessageIds());
	}

	@Override
	public boolean hasRequest() throws IOException {
		return !eof() && getNextRecordType() == REQUEST;
	}

	@Override
	public Request readRequest() throws IOException {
		if (!hasRequest()) throw new FormatException();
		return new Request(readMessageIds());
	}

	@Override
	public boolean hasVersions() throws IOException {
		return !eof() && getNextRecordType() == VERSIONS;
	}

	@Override
	public Versions readVersions() throws IOException {
		if (!hasVersions()) throw new FormatException();
		return new Versions(readSupportedVersions());
	}

	private List<Byte> readSupportedVersions() throws IOException {
		if (nextRecord == null) throw new AssertionError();
		byte[] payload = nextRecord.getPayload();
		if (payload.length == 0) throw new FormatException();
		if (payload.length > MAX_SUPPORTED_VERSIONS)
			throw new FormatException();
		List<Byte> supported = new ArrayList<>(payload.length);
		for (byte b : payload) supported.add(b);
		nextRecord = null;
		return supported;
	}

	@Override
	public boolean hasPriority() throws IOException {
		return !eof() && getNextRecordType() == PRIORITY;
	}

	@Override
	public Priority readPriority() throws IOException {
		if (!hasPriority()) throw new FormatException();
		return new Priority(readNonce());
	}

	private byte[] readNonce() throws IOException {
		if (nextRecord == null) throw new AssertionError();
		byte[] payload = nextRecord.getPayload();
		if (payload.length != PRIORITY_NONCE_BYTES) throw new FormatException();
		nextRecord = null;
		return payload;
	}
}
