package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.FormatException;
import com.quantumresearch.mycel.spore.api.sync.Group.Visibility;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.INVISIBLE;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.SHARED;
import static com.quantumresearch.mycel.spore.api.sync.Group.Visibility.VISIBLE;

@Immutable
@NotNullByDefault
enum State {

	START(0, INVISIBLE),
	/**
	 * The local user has been invited to the shareable, but not yet responded.
	 */
	LOCAL_INVITED(1, INVISIBLE),
	/**
	 * The remote user has been invited to the shareable, but not yet responded.
	 */
	REMOTE_INVITED(2, VISIBLE),
	SHARING(3, SHARED),
	LOCAL_LEFT(4, INVISIBLE),
	/**
	 * The local user has left the shareable, but the remote user hasn't.
	 */
	REMOTE_HANGING(5, INVISIBLE);

	private final int value;
	private final Visibility visibility;

	State(int value, Visibility visibility) {
		this.value = value;
		this.visibility = visibility;
	}

	public int getValue() {
		return value;
	}

	public Visibility getVisibility() {
		return visibility;
	}

	public boolean isAwaitingResponse() {
		return this == LOCAL_INVITED || this == REMOTE_INVITED;
	}

	static State fromValue(int value) throws FormatException {
		for (State s : values()) if (s.value == value) return s;
		throw new FormatException();
	}

}
