package com.quantumresearch.mycel.spore.record;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.record.Record;
import com.quantumresearch.mycel.spore.api.record.RecordReader;
import com.quantumresearch.mycel.spore.util.ByteUtils;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import static com.quantumresearch.mycel.spore.api.record.Record.MAX_RECORD_PAYLOAD_BYTES;
import static com.quantumresearch.mycel.spore.api.record.Record.RECORD_HEADER_BYTES;

@NotThreadSafe
@NotNullByDefault
class RecordReaderImpl implements RecordReader {

	private final DataInputStream in;
	private final byte[] header = new byte[RECORD_HEADER_BYTES];

	RecordReaderImpl(InputStream in) {
		if (!in.markSupported()) in = new BufferedInputStream(in, 1);
		this.in = new DataInputStream(in);
	}

	@Override
	public Record readRecord() throws IOException {
		in.readFully(header);
		byte protocolVersion = header[0];
		byte recordType = header[1];
		int payloadLength = ByteUtils.readUint16(header, 2);
		if (payloadLength < 0 || payloadLength > MAX_RECORD_PAYLOAD_BYTES)
			throw new FormatException();
		byte[] payload = new byte[payloadLength];
		in.readFully(payload);
		return new Record(protocolVersion, recordType, payload);
	}

	@Nullable
	@Override
	public Record readRecord(RecordPredicate accept, RecordPredicate ignore)
			throws IOException {
		while (true) {
			if (eof()) return null;
			Record r = readRecord();
			if (accept.test(r)) return r;
			if (!ignore.test(r)) throw new FormatException();
		}
	}

	@Override
	public void close() throws IOException {
		in.close();
	}

	private boolean eof() throws IOException {
		in.mark(1);
		int next = in.read();
		in.reset();
		return next == -1;
	}
}
