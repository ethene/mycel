package com.quantumresearch.mycel.spore.api;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Consumer<T> {

	void accept(T t);
}
