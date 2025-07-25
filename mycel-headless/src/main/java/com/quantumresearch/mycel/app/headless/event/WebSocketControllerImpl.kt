package com.quantumresearch.mycel.app.headless.event

import io.javalin.plugin.json.JavalinJson.toJson
import io.javalin.websocket.WsContext
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor
import com.quantumresearch.mycel.spore.util.LogUtils.logException
import com.quantumresearch.mycel.app.headless.json.JsonDict
import org.eclipse.jetty.websocket.api.WebSocketException
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.logging.Level.WARNING
import java.util.logging.Logger.getLogger
import javax.annotation.concurrent.Immutable
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
internal class WebSocketControllerImpl
@Inject
constructor(@IoExecutor private val ioExecutor: Executor) : WebSocketController {

    private val logger = getLogger(WebSocketControllerImpl::javaClass.name)

    override val sessions: MutableSet<WsContext> = ConcurrentHashMap.newKeySet<WsContext>()

    override fun sendEvent(name: String, obj: JsonDict) {
        val event = toJson(OutputEvent(name, obj))
        sessions.forEach { session ->
            ioExecutor.execute {
                try {
                    session.send(event)
                } catch (e: WebSocketException) {
                    logException(logger, WARNING, e)
                } catch (e: IOException) {
                    logException(logger, WARNING, e)
                }
            }
        }
    }

}
