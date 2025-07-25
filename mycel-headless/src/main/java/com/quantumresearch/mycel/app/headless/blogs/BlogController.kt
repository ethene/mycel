package com.quantumresearch.mycel.app.headless.blogs

import io.javalin.http.Context

interface BlogController {

    fun listPosts(ctx: Context): Context

    fun createPost(ctx: Context): Context

}
