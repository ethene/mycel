package com.quantumresearch.mycel.app.headless.blogs

import io.javalin.http.BadRequestResponse
import io.javalin.plugin.json.JavalinJson.toJson
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import com.quantumresearch.mycel.spore.api.db.DbCallable
import com.quantumresearch.mycel.spore.api.db.DbException
import com.quantumresearch.mycel.spore.api.db.Transaction
import com.quantumresearch.mycel.spore.api.sync.MessageId
import com.quantumresearch.mycel.spore.identity.output
import com.quantumresearch.mycel.spore.util.StringUtils.getRandomString
import com.quantumresearch.mycel.app.api.blog.Blog
import com.quantumresearch.mycel.app.api.blog.BlogConstants.MAX_BLOG_POST_TEXT_LENGTH
import com.quantumresearch.mycel.app.api.blog.BlogManager
import com.quantumresearch.mycel.app.api.blog.BlogPost
import com.quantumresearch.mycel.app.api.blog.BlogPostFactory
import com.quantumresearch.mycel.app.api.blog.BlogPostHeader
import com.quantumresearch.mycel.app.api.blog.MessageType.POST
import com.quantumresearch.mycel.app.api.identity.AuthorInfo
import com.quantumresearch.mycel.app.api.identity.AuthorInfo.Status.OURSELVES
import com.quantumresearch.mycel.app.headless.ControllerTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class BlogControllerTest : ControllerTest() {

    private val blogManager = mockk<BlogManager>()
    private val blogPostFactory = mockk<BlogPostFactory>()

    private val controller =
        BlogControllerImpl(blogManager, blogPostFactory, db, identityManager, objectMapper, clock)

    private val blog = Blog(group, author, false)
    private val parentId: MessageId? = null
    private val rssFeed = false
    private val read = true
    private val header = BlogPostHeader(
        POST,
        group.id,
        message.id,
        parentId,
        message.timestamp,
        timestamp,
        author,
        AuthorInfo(OURSELVES),
        rssFeed,
        read
    )

    @Test
    fun testCreate() {
        val post = BlogPost(message, null, localAuthor)
        val dbSlot = slot<DbCallable<BlogPostHeader, DbException>>()
        val txn = Transaction(Object(), true)

        every { ctx.body() } returns """{"text": "$text"}"""
        every { identityManager.localAuthor } returns localAuthor
        every { blogManager.getPersonalBlog(localAuthor) } returns blog
        every { clock.currentTimeMillis() } returns message.timestamp
        every {
            blogPostFactory.createBlogPost(
                message.groupId,
                message.timestamp,
                parentId,
                localAuthor,
                text
            )
        } returns post
        every { db.transactionWithResult(true, capture(dbSlot)) } answers {
            dbSlot.captured.call(txn)
        }
        every { blogManager.addLocalPost(txn, post) } just Runs
        every {
            blogManager.getPostHeader(txn, post.message.groupId, post.message.id)
        } returns header
        every { ctx.json(header.output(text)) } returns ctx

        controller.createPost(ctx)
    }

    @Test
    fun testCreateNoText() {
        every { ctx.body() } returns """{"foo": "bar"}"""

        assertThrows(BadRequestResponse::class.java) { controller.createPost(ctx) }
    }

    @Test
    fun testCreateEmptyText() {
        every { ctx.body() } returns """{"text": ""}"""

        assertThrows(BadRequestResponse::class.java) { controller.createPost(ctx) }
    }

    @Test
    fun testCreateTooLongText() {
        every { ctx.body() } returns """{"text": "${getRandomString(MAX_BLOG_POST_TEXT_LENGTH + 1)}"}"""

        assertThrows(BadRequestResponse::class.java) { controller.createPost(ctx) }
    }

    @Test
    fun testList() {
        every { blogManager.blogs } returns listOf(blog)
        every { blogManager.getPostHeaders(group.id) } returns listOf(header)
        every { blogManager.getPostText(message.id) } returns text
        every { ctx.json(listOf(header.output(text))) } returns ctx

        controller.listPosts(ctx)
    }

    @Test
    fun testEmptyList() {
        every { blogManager.blogs } returns listOf(blog)
        every { blogManager.getPostHeaders(group.id) } returns emptyList()
        every { ctx.json(emptyList<Any>()) } returns ctx

        controller.listPosts(ctx)
    }

    @Test
    fun testOutputBlogPost() {
        val json = """
            {
                "text": "$text",
                "author": ${toJson(author.output())},
                "authorStatus": "ourselves",
                "type": "post",
                "id": ${toJson(header.id.bytes)},
                "parentId": $parentId,
                "read": $read,
                "rssFeed": $rssFeed,
                "timestamp": ${message.timestamp},
                "timestampReceived": $timestamp
            }
        """
        assertJsonEquals(json, header.output(text))
    }

}
