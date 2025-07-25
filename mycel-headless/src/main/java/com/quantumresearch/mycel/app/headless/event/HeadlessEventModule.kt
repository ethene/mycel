package com.quantumresearch.mycel.app.headless.event

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HeadlessEventModule {

    @Provides
    @Singleton
    internal fun provideWebSocketController(webSocketController: WebSocketControllerImpl): WebSocketController {
        return webSocketController
    }

}
