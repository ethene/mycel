package com.quantumresearch.mycel.spore.api;

import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
public interface Predicate<T> {

	boolean test(T t);
}
