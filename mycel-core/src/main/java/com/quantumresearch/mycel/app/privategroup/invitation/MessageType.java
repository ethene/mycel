package com.quantumresearch.mycel.app.privategroup.invitation;

import com.quantumresearch.mycel.spore.api.FormatException;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
enum MessageType {

	INVITE(0), JOIN(1), LEAVE(2), ABORT(3);

	private final int value;

	MessageType(int value) {
		this.value = value;
	}

	int getValue() {
		return value;
	}

	static MessageType fromValue(int value) throws FormatException {
		for (MessageType m : values()) if (m.value == value) return m;
		throw new FormatException();
	}
}
