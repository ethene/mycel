package com.quantumresearch.mycel.app.headless.contact

import com.quantumresearch.mycel.spore.api.contact.Contact
import com.quantumresearch.mycel.spore.api.contact.event.ContactAddedEvent
import com.quantumresearch.mycel.spore.api.plugin.event.ContactConnectedEvent
import com.quantumresearch.mycel.spore.api.plugin.event.ContactDisconnectedEvent
import com.quantumresearch.mycel.spore.identity.output
import com.quantumresearch.mycel.app.headless.json.JsonDict

internal fun Contact.output(latestMsgTime: Long, connected: Boolean, unreadCount: Int) = JsonDict(
    "contactId" to id.int,
    "author" to author.output(),
    "verified" to isVerified,
    "lastChatActivity" to latestMsgTime,
    "connected" to connected,
    "unreadCount" to unreadCount
).apply {
    alias?.let { put("alias", it) }
    handshakePublicKey?.let { put("handshakePublicKey", it.encoded) }
}

internal fun ContactAddedEvent.output() = JsonDict(
    "contactId" to contactId.int,
    "verified" to isVerified
)

internal fun ContactConnectedEvent.output() = JsonDict(
    "contactId" to contactId.int
)

internal fun ContactDisconnectedEvent.output() = JsonDict(
    "contactId" to contactId.int
)
