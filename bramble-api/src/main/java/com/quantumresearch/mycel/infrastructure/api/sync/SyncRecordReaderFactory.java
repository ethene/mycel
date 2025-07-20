package com.quantumresearch.mycel.infrastructure.api.sync;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.InputStream;

@NotNullByDefault
public interface SyncRecordReaderFactory {

	SyncRecordReader createRecordReader(InputStream in);
}
