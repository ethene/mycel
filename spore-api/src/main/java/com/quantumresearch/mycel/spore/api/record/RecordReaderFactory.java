package com.quantumresearch.mycel.spore.api.record;

import java.io.InputStream;

public interface RecordReaderFactory {

	RecordReader createRecordReader(InputStream in);
}
