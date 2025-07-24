package com.quantumresearch.mycel.app.api.blog.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class BlogInvitationResponseReceivedEvent
		extends ConversationMessageReceivedEvent<BlogInvitationResponse> {

	public BlogInvitationResponseReceivedEvent(BlogInvitationResponse response,
			ContactId contactId) {
		super(response, contactId);
	}

}
