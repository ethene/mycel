package com.quantumresearch.mycel.infrastructure.crypto;

import com.quantumresearch.mycel.infrastructure.api.FormatException;
import com.quantumresearch.mycel.infrastructure.util.StringUtils;
import org.briarproject.nullsafety.NotNullByDefault;

@NotNullByDefault
class AsciiArmour {

	static String wrap(byte[] b, int lineLength) {
		String wrapped = StringUtils.toHexString(b);
		StringBuilder s = new StringBuilder();
		int length = wrapped.length();
		for (int i = 0; i < length; i += lineLength) {
			int end = Math.min(i + lineLength, length);
			s.append(wrapped.substring(i, end));
			s.append("\r\n");
		}
		return s.toString();
	}

	static byte[] unwrap(String s) throws FormatException {
		try {
			return StringUtils.fromHexString(s.replaceAll("[^0-9a-fA-F]", ""));
		} catch (IllegalArgumentException e) {
			throw new FormatException();
		}
	}
}
