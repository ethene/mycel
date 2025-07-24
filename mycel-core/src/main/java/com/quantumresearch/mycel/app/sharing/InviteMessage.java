package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.sharing.Shareable;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
class InviteMessage<S extends Shareable> extends DeletableSharingMessage {

	private final S shareable;
	@Nullable
	private final String text;

	InviteMessage(MessageId id, @Nullable MessageId previousMessageId,
			GroupId contactGroupId, S shareable, @Nullable String text,
			long timestamp, long autoDeleteTimer) {
		super(id, contactGroupId, shareable.getId(), timestamp,
				previousMessageId, autoDeleteTimer);
		if (text != null && text.isEmpty())
			throw new IllegalArgumentException();
		this.shareable = shareable;
		this.text = text;
	}

	public S getShareable() {
		return shareable;
	}

	@Nullable
	public String getText() {
		return text;
	}

}
