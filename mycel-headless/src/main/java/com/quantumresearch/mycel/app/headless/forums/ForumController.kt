package com.quantumresearch.mycel.app.headless.forums

import io.javalin.http.Context

interface ForumController {

    fun list(ctx: Context): Context

    fun create(ctx: Context): Context

}
