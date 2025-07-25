package com.quantumresearch.mycel.spore.data;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfReader;
import com.quantumresearch.mycel.spore.api.data.BdfReaderFactory;
import com.quantumresearch.mycel.spore.api.data.MetadataParser;
import com.quantumresearch.mycel.spore.api.db.Metadata;
import org.briarproject.nullsafety.NotNullByDefault;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

import static com.quantumresearch.mycel.spore.api.data.BdfDictionary.NULL_VALUE;
import static com.quantumresearch.mycel.spore.api.db.Metadata.REMOVE;

@Immutable
@NotNullByDefault
class MetadataParserImpl implements MetadataParser {

	private final BdfReaderFactory bdfReaderFactory;

	@Inject
	MetadataParserImpl(BdfReaderFactory bdfReaderFactory) {
		this.bdfReaderFactory = bdfReaderFactory;
	}

	@Override
	public BdfDictionary parse(Metadata m) throws FormatException {
		BdfDictionary d = new BdfDictionary();
		try {
			for (Entry<String, byte[]> e : m.entrySet()) {
				// Special case: if key is being removed, value is null
				if (e.getValue() == REMOVE) d.put(e.getKey(), NULL_VALUE);
				else d.put(e.getKey(), parseValue(e.getValue()));
			}
		} catch (FormatException e) {
			throw e;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return d;
	}

	private Object parseValue(byte[] b) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(b);
		BdfReader reader = bdfReaderFactory.createReader(in);
		Object o = parseObject(reader);
		if (!reader.eof()) throw new FormatException();
		return o;
	}

	private Object parseObject(BdfReader reader) throws IOException {
		if (reader.hasNull()) return NULL_VALUE;
		if (reader.hasBoolean()) return reader.readBoolean();
		if (reader.hasLong()) return reader.readLong();
		if (reader.hasDouble()) return reader.readDouble();
		if (reader.hasString()) return reader.readString();
		if (reader.hasRaw()) return reader.readRaw();
		if (reader.hasList()) return reader.readList();
		if (reader.hasDictionary()) return reader.readDictionary();
		throw new FormatException();
	}
}
