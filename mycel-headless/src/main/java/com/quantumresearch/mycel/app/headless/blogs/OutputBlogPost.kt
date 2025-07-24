package com.quantumresearch.mycel.app.headless.blogs

import com.quantumresearch.mycel.spore.identity.output
import com.quantumresearch.mycel.app.api.blog.BlogPostHeader
import com.quantumresearch.mycel.app.api.blog.MessageType
import com.quantumresearch.mycel.app.headless.json.JsonDict
import java.util.Locale

internal fun BlogPostHeader.output(text: String) = JsonDict(
    "text" to text,
    "author" to author.output(),
    "authorStatus" to authorInfo.status.output(),
    "type" to type.output(),
    "id" to id.bytes,
    "parentId" to parentId?.bytes,
    "read" to isRead,
    "rssFeed" to isRssFeed,
    "timestamp" to timestamp,
    "timestampReceived" to timeReceived
)

internal fun MessageType.output() = name.lowercase(Locale.US)
