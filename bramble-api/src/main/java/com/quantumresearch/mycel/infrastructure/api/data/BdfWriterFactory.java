package com.quantumresearch.mycel.infrastructure.api.data;

import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

@NotNullByDefault
public interface BdfWriterFactory {

	BdfWriter createWriter(OutputStream out);
}
