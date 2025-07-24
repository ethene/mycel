package com.quantumresearch.mycel.spore.api.data;

import org.briarproject.nullsafety.NotNullByDefault;

import java.util.Map.Entry;

import javax.annotation.concurrent.Immutable;

/**
 * A convenience class for building {@link BdfDictionary BdfDictionaries}
 * via the {@link BdfDictionary#of(Entry[]) factory method}. Entries in
 * BdfDictionaries do not have to be BdfEntries.
 */
@Immutable
@NotNullByDefault
public class BdfEntry implements Entry<String, Object>, Comparable<BdfEntry> {

	private final String key;
	private final Object value;

	public BdfEntry(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public Object setValue(Object value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int compareTo(BdfEntry e) {
		if (e == this) return 0;
		return key.compareTo(e.key);
	}
}
