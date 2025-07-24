package com.quantumresearch.mycel.spore.record;

import com.quantumresearch.mycel.spore.api.record.RecordReader;
import com.quantumresearch.mycel.spore.api.record.RecordReaderFactory;

import java.io.InputStream;

class RecordReaderFactoryImpl implements RecordReaderFactory {

	@Override
	public RecordReader createRecordReader(InputStream in) {
		return new RecordReaderImpl(in);
	}
}
