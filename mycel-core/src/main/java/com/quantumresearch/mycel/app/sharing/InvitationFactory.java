package com.quantumresearch.mycel.app.sharing;

import com.quantumresearch.mycel.spore.api.contact.ContactId;
import com.quantumresearch.mycel.spore.api.sync.GroupId;
import com.quantumresearch.mycel.spore.api.sync.MessageId;
import com.quantumresearch.mycel.app.api.conversation.ConversationRequest;
import com.quantumresearch.mycel.app.api.sharing.InvitationResponse;
import com.quantumresearch.mycel.app.api.sharing.Shareable;

public interface InvitationFactory<S extends Shareable, R extends InvitationResponse> {

	ConversationRequest<S> createInvitationRequest(boolean local, boolean sent,
			boolean seen, boolean read, InviteMessage<S> m, ContactId c,
			boolean available, boolean canBeOpened, long autoDeleteTimer);

	R createInvitationResponse(MessageId id, GroupId contactGroupId, long time,
			boolean local, boolean sent, boolean seen, boolean read,
			boolean accept, GroupId shareableId, long autoDeleteTimer,
			boolean isAutoDecline);

}
