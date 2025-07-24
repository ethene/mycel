package com.quantumresearch.mycel.app.headless

import com.fasterxml.jackson.databind.ObjectMapper
import io.javalin.http.Context
import io.javalin.http.util.ContextUtil
import io.mockk.mockk
import com.quantumresearch.mycel.spore.api.connection.ConnectionRegistry
import com.quantumresearch.mycel.spore.api.contact.Contact
import com.quantumresearch.mycel.spore.api.contact.ContactManager
import com.quantumresearch.mycel.spore.api.db.TransactionManager
import com.quantumresearch.mycel.spore.api.identity.Author
import com.quantumresearch.mycel.spore.api.identity.IdentityManager
import com.quantumresearch.mycel.spore.api.identity.LocalAuthor
import com.quantumresearch.mycel.spore.api.sync.Group
import com.quantumresearch.mycel.spore.api.sync.Message
import com.quantumresearch.mycel.spore.api.system.Clock
import com.quantumresearch.mycel.spore.test.TestUtils.getAuthor
import com.quantumresearch.mycel.spore.test.TestUtils.getClientId
import com.quantumresearch.mycel.spore.test.TestUtils.getContact
import com.quantumresearch.mycel.spore.test.TestUtils.getGroup
import com.quantumresearch.mycel.spore.test.TestUtils.getLocalAuthor
import com.quantumresearch.mycel.spore.test.TestUtils.getMessage
import com.quantumresearch.mycel.spore.util.StringUtils.getRandomString
import com.quantumresearch.mycel.app.api.conversation.ConversationManager
import com.quantumresearch.mycel.app.headless.event.WebSocketController
import org.skyscreamer.jsonassert.JSONAssert.assertEquals
import org.skyscreamer.jsonassert.JSONCompareMode.STRICT
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

abstract class ControllerTest {

    protected val db = mockk<TransactionManager>()
    protected val contactManager = mockk<ContactManager>()
    protected val conversationManager = mockk<ConversationManager>()
    protected val identityManager = mockk<IdentityManager>()
    protected val connectionRegistry = mockk<ConnectionRegistry>()
    protected val clock = mockk<Clock>()
    protected val ctx = mockk<Context>()

    protected val webSocketController = mockk<WebSocketController>()

    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val outputCtx = ContextUtil.init(request, response)

    protected val objectMapper = ObjectMapper()

    protected val group: Group = getGroup(getClientId(), 0)
    protected val author: Author = getAuthor()
    protected val localAuthor: LocalAuthor = getLocalAuthor()
    protected val contact: Contact = getContact(author, localAuthor.id, true)
    protected val message: Message = getMessage(group.id)
    protected val text: String = getRandomString(5)
    protected val timestamp = 42L
    protected val unreadCount = 42

    protected fun assertJsonEquals(json: String, obj: Any) {
        assertEquals(json, outputCtx.json(obj).resultString(), STRICT)
    }

}
