package com.quantumresearch.mycel.app.api.forum;

import com.quantumresearch.mycel.infrastructure.api.sync.Group;
import com.quantumresearch.mycel.app.api.client.NamedGroup;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class Forum extends NamedGroup implements Shareable {

	public Forum(Group group, String name, byte[] salt) {
		super(group, name, salt);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Forum && super.equals(o);
	}

}
