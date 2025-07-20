package com.quantumresearch.mycel.app.api.privategroup.event;

import com.quantumresearch.mycel.infrastructure.api.event.Event;
import com.quantumresearch.mycel.infrastructure.api.sync.GroupId;
import com.quantumresearch.mycel.app.api.privategroup.GroupMessageHeader;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

/**
 * An event that is broadcast when a private group message was added
 * to the database.
 */
@Immutable
@NotNullByDefault
public class GroupMessageAddedEvent extends Event {

	private final GroupId groupId;
	private final GroupMessageHeader header;
	private final String text;
	private final boolean local;

	public GroupMessageAddedEvent(GroupId groupId, GroupMessageHeader header,
			String text, boolean local) {
		this.groupId = groupId;
		this.header = header;
		this.text = text;
		this.local = local;
	}

	public GroupId getGroupId() {
		return groupId;
	}

	public GroupMessageHeader getHeader() {
		return header;
	}

	public String getText() {
		return text;
	}

	public boolean isLocal() {
		return local;
	}

}
