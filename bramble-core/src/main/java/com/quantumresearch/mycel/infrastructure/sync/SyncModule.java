package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.sync.GroupFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriterFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncSessionFactory;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class SyncModule {

	@Provides
	GroupFactory provideGroupFactory(GroupFactoryImpl groupFactory) {
		return groupFactory;
	}

	@Provides
	MessageFactory provideMessageFactory(MessageFactoryImpl messageFactory) {
		return messageFactory;
	}

	@Provides
	SyncRecordReaderFactory provideRecordReaderFactory(
			SyncRecordReaderFactoryImpl recordReaderFactory) {
		return recordReaderFactory;
	}

	@Provides
	SyncRecordWriterFactory provideRecordWriterFactory(
			SyncRecordWriterFactoryImpl recordWriterFactory) {
		return recordWriterFactory;
	}

	@Provides
	@Singleton
	SyncSessionFactory provideSyncSessionFactory(
			SyncSessionFactoryImpl syncSessionFactory) {
		return syncSessionFactory;
	}
}
