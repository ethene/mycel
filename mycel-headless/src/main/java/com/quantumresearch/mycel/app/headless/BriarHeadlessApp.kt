package com.quantumresearch.mycel.app.headless

import dagger.Component
import com.quantumresearch.mycel.spore.BrambleCoreEagerSingletons
import com.quantumresearch.mycel.spore.BrambleCoreModule
import com.quantumresearch.mycel.spore.BrambleJavaEagerSingletons
import com.quantumresearch.mycel.spore.BrambleJavaModule
import com.quantumresearch.mycel.app.BriarCoreEagerSingletons
import com.quantumresearch.mycel.app.BriarCoreModule
import java.security.SecureRandom
import javax.inject.Singleton

@Component(
    modules = [
        BrambleCoreModule::class,
        BrambleJavaModule::class,
        BriarCoreModule::class,
        HeadlessModule::class
    ]
)
@Singleton
internal interface BriarHeadlessApp : BrambleCoreEagerSingletons, BriarCoreEagerSingletons,
    BrambleJavaEagerSingletons, HeadlessEagerSingletons {

    fun getRouter(): Router

    fun getSecureRandom(): SecureRandom
}
