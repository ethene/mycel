package com.quantumresearch.mycel.spore.api.sync.event;

import com.quantumresearch.mycel.spore.api.event.Event;
import com.quantumresearch.mycel.spore.api.sync.Group;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a group is removed from the database.
 */
@Immutable
@NotNullByDefault
public class GroupRemovedEvent extends Event {

	private final Group group;

	public GroupRemovedEvent(Group group) {
		this.group = group;
	}

	public Group getGroup() {
		return group;
	}
}
