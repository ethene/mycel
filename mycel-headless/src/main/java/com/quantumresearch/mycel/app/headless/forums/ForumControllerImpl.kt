package com.quantumresearch.mycel.app.headless.forums

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.BadRequestResponse
import io.javalin.http.Context
import com.quantumresearch.mycel.spore.util.StringUtils.utf8IsTooLong
import com.quantumresearch.mycel.app.api.forum.ForumConstants.MAX_FORUM_NAME_LENGTH
import com.quantumresearch.mycel.app.api.forum.ForumManager
import com.quantumresearch.mycel.app.headless.getFromJson
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
internal class ForumControllerImpl
@Inject
constructor(private val forumManager: ForumManager, private val objectMapper: ObjectMapper) :
    ForumController {

    override fun list(ctx: Context): Context {
        return ctx.json(forumManager.forums.output())
    }

    override fun create(ctx: Context): Context {
        val name = ctx.getFromJson(objectMapper, "name")
        if (utf8IsTooLong(name, MAX_FORUM_NAME_LENGTH))
            throw BadRequestResponse("Forum name is too long")
        return ctx.json(forumManager.addForum(name).output())
    }

}
