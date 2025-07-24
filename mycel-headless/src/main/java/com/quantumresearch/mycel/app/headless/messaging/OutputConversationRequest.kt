package com.quantumresearch.mycel.app.headless.messaging

import com.quantumresearch.mycel.spore.api.contact.ContactId
import com.quantumresearch.mycel.app.api.blog.BlogInvitationRequest
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader
import com.quantumresearch.mycel.app.api.conversation.ConversationRequest
import com.quantumresearch.mycel.app.api.forum.ForumInvitationRequest
import com.quantumresearch.mycel.app.api.introduction.IntroductionRequest
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationRequest
import com.quantumresearch.mycel.app.api.sharing.InvitationRequest
import com.quantumresearch.mycel.app.headless.json.JsonDict

internal fun ConversationRequest<*>.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationMessageHeader).output(contactId, text)
    dict.putAll(
        "sessionId" to sessionId.bytes,
        "name" to name,
        "answered" to wasAnswered()
    )
    return dict
}

internal fun IntroductionRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationRequest<*>).output(contactId)
    dict.putAll(
        "type" to "IntroductionRequest",
        "alreadyContact" to isContact
    )
    return dict
}

internal fun InvitationRequest<*>.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationRequest<*>).output(contactId)
    dict["canBeOpened"] = canBeOpened()
    return dict
}

internal fun BlogInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "BlogInvitationRequest"
    return dict
}

internal fun ForumInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "ForumInvitationRequest"
    return dict
}

internal fun GroupInvitationRequest.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationRequest<*>).output(contactId)
    dict["type"] = "GroupInvitationRequest"
    return dict
}
