package com.quantumresearch.mycel.infrastructure.data;

import com.quantumresearch.mycel.infrastructure.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.data.BdfWriterFactory;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataEncoder;
import com.quantumresearch.mycel.infrastructure.api.data.MetadataParser;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {

	@Provides
	BdfReaderFactory provideBdfReaderFactory() {
		return new BdfReaderFactoryImpl();
	}

	@Provides
	BdfWriterFactory provideBdfWriterFactory() {
		return new BdfWriterFactoryImpl();
	}

	@Provides
	MetadataParser provideMetaDataParser(BdfReaderFactory bdfReaderFactory) {
		return new MetadataParserImpl(bdfReaderFactory);
	}

	@Provides
	MetadataEncoder provideMetaDataEncoder(BdfWriterFactory bdfWriterFactory) {
		return new MetadataEncoderImpl(bdfWriterFactory);
	}

}
