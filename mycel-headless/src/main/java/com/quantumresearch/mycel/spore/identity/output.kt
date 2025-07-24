package com.quantumresearch.mycel.spore.identity

import com.quantumresearch.mycel.spore.api.identity.Author
import com.quantumresearch.mycel.app.api.identity.AuthorInfo
import com.quantumresearch.mycel.app.headless.json.JsonDict
import java.util.Locale

/**
 * Extension function to convert Author to JSON output format
 */
internal fun Author.output() = JsonDict(
    "formatVersion" to formatVersion,
    "id" to id.bytes,
    "name" to name,
    "publicKey" to publicKey.encoded
)

/**
 * Extension function to convert AuthorInfo.Status to string output format
 */
internal fun AuthorInfo.Status.output() = name.lowercase(Locale.US)