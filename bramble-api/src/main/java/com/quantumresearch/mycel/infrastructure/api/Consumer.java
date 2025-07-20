package com.quantumresearch.mycel.infrastructure.api;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Consumer<T> {

	void accept(T t);
}
