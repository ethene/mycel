package com.quantumresearch.mycel.infrastructure.api.record;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.IOException;

@NotNullByDefault
public interface RecordWriter {

	void writeRecord(Record r) throws IOException;

	void flush() throws IOException;

	void close() throws IOException;

	long getBytesWritten();
}
