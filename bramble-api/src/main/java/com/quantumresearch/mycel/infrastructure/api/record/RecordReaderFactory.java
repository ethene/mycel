package com.quantumresearch.mycel.infrastructure.api.record;

import java.io.InputStream;

public interface RecordReaderFactory {

	RecordReader createRecordReader(InputStream in);
}
