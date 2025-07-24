package com.quantumresearch.mycel.app.headless.event

import io.javalin.websocket.WsContext
import com.quantumresearch.mycel.spore.api.lifecycle.IoExecutor
import com.quantumresearch.mycel.app.headless.json.JsonDict
import javax.annotation.concurrent.ThreadSafe

@ThreadSafe
interface WebSocketController {

    val sessions: MutableSet<WsContext>

    /**
     * Sends an event to all open sessions using the [IoExecutor].
     */
    fun sendEvent(name: String, obj: JsonDict)

}
