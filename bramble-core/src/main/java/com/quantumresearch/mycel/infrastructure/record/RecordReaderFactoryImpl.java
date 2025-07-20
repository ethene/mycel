package com.quantumresearch.mycel.infrastructure.record;

import com.quantumresearch.mycel.infrastructure.api.record.RecordReader;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReaderFactory;

import java.io.InputStream;

class RecordReaderFactoryImpl implements RecordReaderFactory {

	@Override
	public RecordReader createRecordReader(InputStream in) {
		return new RecordReaderImpl(in);
	}
}
