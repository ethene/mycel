package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.record.Record;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.Ack;
import com.quantumresearch.mycel.infrastructure.api.sync.Message;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageId;
import com.quantumresearch.mycel.infrastructure.api.sync.Offer;
import com.quantumresearch.mycel.infrastructure.api.sync.Priority;
import com.quantumresearch.mycel.infrastructure.api.sync.Request;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.Versions;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.ACK;
import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.MESSAGE;
import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.OFFER;
import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.PRIORITY;
import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.REQUEST;
import static com.quantumresearch.mycel.infrastructure.api.sync.RecordTypes.VERSIONS;
import static com.quantumresearch.mycel.infrastructure.api.sync.SyncConstants.PROTOCOL_VERSION;

@NotThreadSafe
@NotNullByDefault
class SyncRecordWriterImpl implements SyncRecordWriter {

	private final MessageFactory messageFactory;
	private final RecordWriter writer;
	private final ByteArrayOutputStream payload = new ByteArrayOutputStream();

	SyncRecordWriterImpl(MessageFactory messageFactory, RecordWriter writer) {
		this.messageFactory = messageFactory;
		this.writer = writer;
	}

	private void writeRecord(byte recordType) throws IOException {
		writer.writeRecord(new Record(PROTOCOL_VERSION, recordType,
				payload.toByteArray()));
		payload.reset();
	}

	@Override
	public void writeAck(Ack a) throws IOException {
		for (MessageId m : a.getMessageIds()) payload.write(m.getBytes());
		writeRecord(ACK);
	}

	@Override
	public void writeMessage(Message m) throws IOException {
		byte[] raw = messageFactory.getRawMessage(m);
		writer.writeRecord(new Record(PROTOCOL_VERSION, MESSAGE, raw));
	}

	@Override
	public void writeOffer(Offer o) throws IOException {
		for (MessageId m : o.getMessageIds()) payload.write(m.getBytes());
		writeRecord(OFFER);
	}

	@Override
	public void writeRequest(Request r) throws IOException {
		for (MessageId m : r.getMessageIds()) payload.write(m.getBytes());
		writeRecord(REQUEST);
	}

	@Override
	public void writeVersions(Versions v) throws IOException {
		for (byte b : v.getSupportedVersions()) payload.write(b);
		writeRecord(VERSIONS);
	}

	@Override
	public void writePriority(Priority p) throws IOException {
		writer.writeRecord(
				new Record(PROTOCOL_VERSION, PRIORITY, p.getNonce()));
	}

	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	@Override
	public long getBytesWritten() {
		return writer.getBytesWritten();
	}
}
