package com.quantumresearch.mycel.infrastructure.api.record;

import java.io.OutputStream;

public interface RecordWriterFactory {

	RecordWriter createRecordWriter(OutputStream out);
}
