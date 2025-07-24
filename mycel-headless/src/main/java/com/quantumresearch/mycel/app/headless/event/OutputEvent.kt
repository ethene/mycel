package com.quantumresearch.mycel.app.headless.event

import com.quantumresearch.mycel.app.api.blog.BlogInvitationRequest
import com.quantumresearch.mycel.app.api.blog.BlogInvitationResponse
import com.quantumresearch.mycel.app.api.conversation.event.ConversationMessageReceivedEvent
import com.quantumresearch.mycel.app.api.forum.ForumInvitationRequest
import com.quantumresearch.mycel.app.api.forum.ForumInvitationResponse
import com.quantumresearch.mycel.app.api.introduction.IntroductionRequest
import com.quantumresearch.mycel.app.api.introduction.IntroductionResponse
import com.quantumresearch.mycel.app.api.messaging.PrivateMessageHeader
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationRequest
import com.quantumresearch.mycel.app.api.privategroup.invitation.GroupInvitationResponse
import com.quantumresearch.mycel.app.headless.json.JsonDict
import com.quantumresearch.mycel.app.headless.messaging.output
import javax.annotation.concurrent.Immutable

@Immutable
@Suppress("unused")
internal class OutputEvent(val name: String, val data: JsonDict) {
    val type = "event"
}

internal fun ConversationMessageReceivedEvent<*>.output(text: String?): JsonDict {
    check(messageHeader is PrivateMessageHeader)
    return (messageHeader as PrivateMessageHeader).output(contactId, text)
}

internal fun ConversationMessageReceivedEvent<*>.output() = when (messageHeader) {
    // requests
    is ForumInvitationRequest -> (messageHeader as ForumInvitationRequest).output(contactId)
    is BlogInvitationRequest -> (messageHeader as BlogInvitationRequest).output(contactId)
    is GroupInvitationRequest -> (messageHeader as GroupInvitationRequest).output(contactId)
    is IntroductionRequest -> (messageHeader as IntroductionRequest).output(contactId)
    // responses
    is ForumInvitationResponse -> (messageHeader as ForumInvitationResponse).output(contactId)
    is BlogInvitationResponse -> (messageHeader as BlogInvitationResponse).output(contactId)
    is GroupInvitationResponse -> (messageHeader as GroupInvitationResponse).output(contactId)
    is IntroductionResponse -> (messageHeader as IntroductionResponse).output(contactId)
    // unknown
    else -> throw IllegalStateException()
}
