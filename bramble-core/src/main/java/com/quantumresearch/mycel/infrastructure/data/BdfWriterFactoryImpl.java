package com.quantumresearch.mycel.infrastructure.data;

import com.quantumresearch.mycel.infrastructure.api.data.BdfWriter;
import com.quantumresearch.mycel.infrastructure.api.data.BdfWriterFactory;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.OutputStream;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class BdfWriterFactoryImpl implements BdfWriterFactory {

	@Override
	public BdfWriter createWriter(OutputStream out) {
		return new BdfWriterImpl(out);
	}
}
