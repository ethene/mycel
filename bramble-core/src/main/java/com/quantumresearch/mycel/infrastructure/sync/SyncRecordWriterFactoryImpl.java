package com.quantumresearch.mycel.infrastructure.sync;

import com.quantumresearch.mycel.infrastructure.api.record.RecordWriter;
import com.quantumresearch.mycel.infrastructure.api.record.RecordWriterFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.MessageFactory;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriter;
import com.quantumresearch.mycel.infrastructure.api.sync.SyncRecordWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.inject.Inject;

@NotNullByDefault
class SyncRecordWriterFactoryImpl implements SyncRecordWriterFactory {

	private final MessageFactory messageFactory;
	private final RecordWriterFactory recordWriterFactory;

	@Inject
	SyncRecordWriterFactoryImpl(MessageFactory messageFactory,
			RecordWriterFactory recordWriterFactory) {
		this.messageFactory = messageFactory;
		this.recordWriterFactory = recordWriterFactory;
	}

	@Override
	public SyncRecordWriter createRecordWriter(OutputStream out) {
		RecordWriter writer = recordWriterFactory.createRecordWriter(out);
		return new SyncRecordWriterImpl(messageFactory, writer);
	}
}
