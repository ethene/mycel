package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.record.RecordReader;
import com.quantumresearch.mycel.infrastructure.api.record.RecordReaderFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordReader;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordReaderFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
@NotNullByDefault
class SyncRecordReaderFactoryImpl implements SyncRecordReaderFactory {

	private final MessageFactory messageFactory;
	private final RecordReaderFactory recordReaderFactory;

	@Inject
	SyncRecordReaderFactoryImpl(MessageFactory messageFactory,
			RecordReaderFactory recordReaderFactory) {
		this.messageFactory = messageFactory;
		this.recordReaderFactory = recordReaderFactory;
	}

	@Override
	public SyncRecordReader createRecordReader(InputStream in) {
		RecordReader reader = recordReaderFactory.createRecordReader(in);
		return new SyncRecordReaderImpl(messageFactory, reader);
	}
}
