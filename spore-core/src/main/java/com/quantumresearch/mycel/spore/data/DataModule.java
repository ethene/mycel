package com.quantumresearch.mycel.spore.data;

import com.quantumresearch.mycel.spore.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.spore.api.data.BdfWriterFactory;
import com.quantumresearch.mycel.spore.api.data.MetadataEncoder;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;

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
