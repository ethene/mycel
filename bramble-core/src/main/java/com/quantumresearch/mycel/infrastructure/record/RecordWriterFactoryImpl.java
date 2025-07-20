package com.quantumresearch.mycel.infrastructure.record;

import com.quantumresearch.mycel.infrastructure.api.record.RecordWriter;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriterFactory;

import java.io.OutputStream;

class RecordWriterFactoryImpl implements RecordWriterFactory {

	@Override
	public RecordWriter createRecordWriter(OutputStream out) {
		return new RecordWriterImpl(out);
	}
}
