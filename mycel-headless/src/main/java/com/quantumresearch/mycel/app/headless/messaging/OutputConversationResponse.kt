package com.quantumresearch.mycel.app.headless.messaging

import com.quantumresearch.mycel.spore.api.contact.ContactId
import com.quantumresearch.mycel.spore.identity.output
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse
import com.quantumresearch.mycel.app.api.conversation.ConversationMessageHeader
import com.quantumresearch.mycel.app.api.conversation.ConversationResponse
import com.quantumresearch.mycel.app.api.forum.ForumInvitationResponse
import com.quantumresearch.mycel.app.api.introduction.IntroductionResponse
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationResponse
import com.quantumresearch.mycel.app.api.sharing.InvitationResponse
import com.quantumresearch.mycel.app.headless.json.JsonDict

internal fun ConversationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationMessageHeader).output(contactId)
    dict.putAll(
        "sessionId" to sessionId.bytes,
        "accepted" to wasAccepted()
    )
    return dict
}

internal fun IntroductionResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationResponse).output(contactId)
    dict.putAll(
        "type" to "IntroductionResponse",
        "introducedAuthor" to introducedAuthor.output(),
        "introducer" to isIntroducer
    )
    return dict
}

internal fun InvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as ConversationResponse).output(contactId)
    dict["shareableId"] = shareableId.bytes
    return dict
}

internal fun BlogInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "BlogInvitationResponse"
    return dict
}

internal fun ForumInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "ForumInvitationResponse"
    return dict
}

internal fun GroupInvitationResponse.output(contactId: ContactId): JsonDict {
    val dict = (this as InvitationResponse).output(contactId)
    dict["type"] = "GroupInvitationResponse"
    return dict
}
