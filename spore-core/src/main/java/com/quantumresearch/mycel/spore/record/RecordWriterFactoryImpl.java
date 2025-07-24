package com.quantumresearch.mycel.spore.record;

import com.quantumresearch.mycel.spore.api.record.RecordWriter;
import com.quantumresearch.mycel.spore.api.record.RecordWriterFactory;

import java.io.OutputStream;

class RecordWriterFactoryImpl implements RecordWriterFactory {

	@Override
	public RecordWriter createRecordWriter(OutputStream out) {
		return new RecordWriterImpl(out);
	}
}
