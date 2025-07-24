package com.quantumresearch.mycel.app.headless.contact

import com.quantumresearch.mycel.spore.api.contact.PendingContact
import com.quantumresearch.mycel.spore.api.contact.PendingContactState
import com.quantumresearch.mycel.spore.api.contact.PendingContactState.ADDING_CONTACT
import com.quantumresearch.mycel.spore.api.contact.PendingContactState.CONNECTING
import com.quantumresearch.mycel.spore.api.contact.PendingContactState.FAILED
import com.quantumresearch.mycel.spore.api.contact.PendingContactState.OFFLINE
import com.quantumresearch.mycel.spore.api.contact.PendingContactState.WAITING_FOR_CONNECTION
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactAddedEvent
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactRemovedEvent
import com.quantumresearch.mycel.spore.api.contact.event.PendingContactStateChangedEvent
import com.quantumresearch.mycel.app.headless.json.JsonDict

internal fun PendingContact.output() = JsonDict(
    "pendingContactId" to id.bytes,
    "alias" to alias,
    "timestamp" to timestamp
)

internal fun PendingContactState.output() = when (this) {
    WAITING_FOR_CONNECTION -> "waiting_for_connection"
    OFFLINE -> "offline"
    CONNECTING -> "connecting"
    ADDING_CONTACT -> "adding_contact"
    FAILED -> "failed"
    else -> throw AssertionError()
}

internal fun PendingContactAddedEvent.output() = JsonDict(
    "pendingContact" to pendingContact.output()
)

internal fun PendingContactStateChangedEvent.output() = JsonDict(
    "pendingContactId" to id.bytes,
    "state" to pendingContactState.output()
)

internal fun PendingContactRemovedEvent.output() = JsonDict(
    "pendingContactId" to id.bytes
)
