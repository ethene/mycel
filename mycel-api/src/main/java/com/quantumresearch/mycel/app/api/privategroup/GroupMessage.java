package com.quantumresearch.mycel.app.api.privategroup;

import com.quantumresearch.mycel.spore.api.identity.Author;
import com.quantumresearch.mycel.spore.api.sync.Message;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.client.ThreadedMessage;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class GroupMessage extends ThreadedMessage {

	public GroupMessage(Message message, @Nullable MessageId parent,
			Author member) {
		super(message, parent, member);
	}

	public Author getMember() {
		return super.getAuthor();
	}

}
