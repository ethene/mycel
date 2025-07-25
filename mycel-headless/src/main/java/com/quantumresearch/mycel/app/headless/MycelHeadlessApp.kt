package com.quantumresearch.mycel.app.headless

import dagger.Component
import com.quantumresearch.mycel.spore.SporeCoreEagerSingletons
import com.quantumresearch.mycel.spore.SporeCoreModule
import com.quantumresearch.mycel.spore.SporeJavaEagerSingletons
import com.quantumresearch.mycel.spore.SporeJavaModule
import com.quantumresearch.mycel.app.MycelCoreEagerSingletons
import com.quantumresearch.mycel.app.MycelCoreModule
import java.security.SecureRandom
import javax.inject.Singleton

@Component(
    modules = [
        SporeCoreModule::class,
        SporeJavaModule::class,
        MycelCoreModule::class,
        HeadlessModule::class
    ]
)
@Singleton
internal interface MycelHeadlessApp : SporeCoreEagerSingletons, MycelCoreEagerSingletons,
    SporeJavaEagerSingletons, HeadlessEagerSingletons {

    fun getRouter(): Router

    fun getSecureRandom(): SecureRandom
}
