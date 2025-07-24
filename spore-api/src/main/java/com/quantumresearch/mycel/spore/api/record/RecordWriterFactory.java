package com.quantumresearch.mycel.spore.api.record;

import java.io.OutputStream;

public interface RecordWriterFactory {

	RecordWriter createRecordWriter(OutputStream out);
}
