package com.quantumresearch.mycel.spore.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

@NotNullByDefault
public interface SyncRecordWriterFactory {

	SyncRecordWriter createRecordWriter(OutputStream out);
}
