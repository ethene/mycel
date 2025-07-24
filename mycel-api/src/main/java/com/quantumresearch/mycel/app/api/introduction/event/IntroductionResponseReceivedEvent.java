package com.quantumresearch.mycel.app.api.introduction.event;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent;
import com.quantumresearch.mycel.app.api.introduction.IntroductionResponse;
import org.briarproject.nullsafety.NotNullByDefault;

import javax.annotation.concurrent.Immutable;

@Immutable
@NotNullByDefault
public class IntroductionResponseReceivedEvent extends
		ConversationMessageReceivedEvent<IntroductionResponse> {

	public IntroductionResponseReceivedEvent(
			IntroductionResponse introductionResponse, ContactId contactId) {
		super(introductionResponse, contactId);
	}

}
