package com.quantumresearch.mycel.app.api.forum.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.app.api.conversation.ConversationRequest;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.forum.Forum;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class ForumInvitationRequestReceivedEvent extends
		ConversationMessageReceivedEvent<ConversationRequest<Forum>> {

	public ForumInvitationRequestReceivedEvent(ConversationRequest<Forum> request,
			ContactId contactId) {
		super(request, contactId);
	}

}
