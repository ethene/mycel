package com.quantumresearch.mycel.app.headless

import dagger.Component
import com.quantumresearch.mycel.spore.BrambleCoreEagerSingletons
import com.quantumresearch.mycel.spore.BrambleCoreModule
import com.quantumresearch.mycel.spore.BrambleJavaEagerSingletons
import com.quantumresearch.mycel.spore.BrambleJavaModule
import com.quantumresearch.mycel.spore.api.crypto.CryptoComponent
import com.quantumresearch.mycel.app.BriarCoreEagerSingletons
import com.quantumresearch.mycel.app.BriarCoreModule
import com.quantumresearch.mycel.app.api.test.TestDataCreator
import javax.inject.Singleton

@Component(
    modules = [
        BrambleCoreModule::class,
        BrambleJavaModule::class,
        BriarCoreModule::class,
        HeadlessTestModule::class
    ]
)
@Singleton
internal interface BriarHeadlessTestApp : BrambleCoreEagerSingletons, BriarCoreEagerSingletons,
    BrambleJavaEagerSingletons, HeadlessEagerSingletons {

    fun getRouter(): Router

    fun getCryptoComponent(): CryptoComponent

    fun getTestDataCreator(): TestDataCreator
}
