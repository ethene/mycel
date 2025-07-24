package com.quantumresearch.mycel.spore.record;

import com.quantumresearch.mycel.spore.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.spore.api.record.RecordWriterFactory;

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
