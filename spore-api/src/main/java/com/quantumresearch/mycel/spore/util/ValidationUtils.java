package com.quantumresearch.mycel.spore.util;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.data.BdfDictionary;
import com.quantumresearch.mycel.spore.api.data.BdfList;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ValidationUtils {

	public static void checkLength(@Nullable String s, int minLength,
			int maxLength) throws FormatException {
		if (s != null) {
			int length = StringUtils.toUtf8(s).length;
			if (length < minLength) throw new FormatException();
			if (length > maxLength) throw new FormatException();
		}
	}

	public static void checkLength(@Nullable String s, int length)
			throws FormatException {
		if (s != null && StringUtils.toUtf8(s).length != length)
			throw new FormatException();
	}

	public static void checkLength(@Nullable byte[] b, int minLength,
			int maxLength) throws FormatException {
		if (b != null) {
			if (b.length < minLength) throw new FormatException();
			if (b.length > maxLength) throw new FormatException();
		}
	}

	public static void checkLength(@Nullable byte[] b, int length)
			throws FormatException {
		if (b != null && b.length != length) throw new FormatException();
	}

	public static void checkSize(@Nullable BdfList list, int minSize,
			int maxSize) throws FormatException {
		if (list != null) {
			if (list.size() < minSize) throw new FormatException();
			if (list.size() > maxSize) throw new FormatException();
		}
	}

	public static void checkSize(@Nullable BdfList list, int size)
			throws FormatException {
		if (list != null && list.size() != size) throw new FormatException();
	}

	public static void checkSize(@Nullable BdfDictionary dictionary,
			int minSize, int maxSize) throws FormatException {
		if (dictionary != null) {
			if (dictionary.size() < minSize) throw new FormatException();
			if (dictionary.size() > maxSize) throw new FormatException();
		}
	}

	public static void checkSize(@Nullable BdfDictionary dictionary, int size)
			throws FormatException {
		if (dictionary != null && dictionary.size() != size)
			throw new FormatException();
	}

	public static void checkRange(@Nullable Long l, long min, long max)
			throws FormatException {
		if (l != null && (l < min || l > max)) throw new FormatException();
	}
}
