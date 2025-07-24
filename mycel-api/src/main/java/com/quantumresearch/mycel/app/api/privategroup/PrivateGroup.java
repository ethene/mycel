package com.quantumresearch.mycel.app.api.privategroup;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Group;
import com.quantumresearch.mycel.app.api.client.NamedGroup;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class PrivateGroup extends NamedGroup implements Shareable {

	private final Author creator;

	public PrivateGroup(Group group, String name, Author creator, byte[] salt) {
		super(group, name, salt);
		this.creator = creator;
	}

	public Author getCreator() {
		return creator;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof PrivateGroup && super.equals(o);
	}

}
