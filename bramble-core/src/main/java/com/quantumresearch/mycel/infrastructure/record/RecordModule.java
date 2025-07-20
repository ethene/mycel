package com.quantumresearch.mycel.infrastructure.record;

import com.quantumresearch.mycel.infrastructure.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriterFactory;

import dagger.Module;
import dagger.Provides;

@Module
public class RecordModule {

	@Provides
	RecordReaderFactory provideRecordReaderFactory() {
		return new RecordReaderFactoryImpl();
	}

	@Provides
	RecordWriterFactory provideRecordWriterFactory() {
		return new RecordWriterFactoryImpl();
	}
}
