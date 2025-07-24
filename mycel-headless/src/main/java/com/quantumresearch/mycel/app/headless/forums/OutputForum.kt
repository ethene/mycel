package com.quantumresearch.mycel.app.headless.forums

import com.quantumresearch.mycel.app.api.forum.Forum
import com.quantumresearch.mycel.app.headless.json.JsonDict

internal fun Forum.output() = JsonDict(
    "name" to name,
    "id" to id.bytes
)

internal fun Collection<Forum>.output() = map { it.output() }
