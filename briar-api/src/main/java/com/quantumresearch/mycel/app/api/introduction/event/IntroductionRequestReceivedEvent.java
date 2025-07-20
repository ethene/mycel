package com.quantumresearch.mycel.app.api.introduction.event;

import com.quantumresearch.mycel.infrastructure.api.contact.ContactId;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.introduction.IntroductionRequest;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionRequestReceivedEvent
		extends ConversationMessageReceivedEvent<IntroductionRequest> {

	public IntroductionRequestReceivedEvent(
			IntroductionRequest introductionRequest, ContactId contactId) {
		super(introductionRequest, contactId);
	}

}
