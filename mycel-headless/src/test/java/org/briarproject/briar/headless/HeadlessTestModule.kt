package com.quantumresearch.mycel.app.headless

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import com.quantumresearch.mycel.spore.account.AccountModule
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig
import com.quantumresearch.mycel.spore.api.mailbox.MailboxDirectory
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig
import com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_CONTROL_PORT
import com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_SOCKS_PORT
import com.quantumresearch.mycel.spore.api.plugin.TorControlPort
import com.quantumresearch.mycel.spore.api.plugin.TorSocksPort
import com.quantumresearch.mycel.spore.api.plugin.TransportId
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory
import com.quantumresearch.mycel.spore.event.DefaultEventExecutorModule
import com.quantumresearch.mycel.spore.system.ClockModule
import com.quantumresearch.mycel.spore.system.DefaultTaskSchedulerModule
import com.quantumresearch.mycel.spore.system.DefaultThreadFactoryModule
import com.quantumresearch.mycel.spore.system.DefaultWakefulIoExecutorModule
import com.quantumresearch.mycel.spore.test.TestFeatureFlagModule
import com.quantumresearch.mycel.spore.test.TestSecureRandomModule
import com.quantumresearch.mycel.app.api.test.TestAvatarCreator
import com.quantumresearch.mycel.app.headless.blogs.HeadlessBlogModule
import com.quantumresearch.mycel.app.headless.contact.HeadlessContactModule
import com.quantumresearch.mycel.app.headless.event.HeadlessEventModule
import com.quantumresearch.mycel.app.headless.forums.HeadlessForumModule
import com.quantumresearch.mycel.app.headless.messaging.HeadlessMessagingModule
import java.io.File
import java.util.Collections.emptyList
import javax.inject.Singleton

@Module(
    includes = [
        AccountModule::class,
        ClockModule::class,
        DefaultEventExecutorModule::class,
        DefaultTaskSchedulerModule::class,
        DefaultWakefulIoExecutorModule::class,
        DefaultThreadFactoryModule::class,
        TestFeatureFlagModule::class,
        TestSecureRandomModule::class,
        HeadlessBlogModule::class,
        HeadlessContactModule::class,
        HeadlessEventModule::class,
        HeadlessForumModule::class,
        HeadlessMessagingModule::class
    ]
)
internal class HeadlessTestModule(private val appDir: File) {

    @Provides
    @Singleton
    internal fun provideBriarService(briarService: BriarTestServiceImpl): BriarService =
        briarService

    @Provides
    @Singleton
    internal fun provideDatabaseConfig(): DatabaseConfig {
        val dbDir = File(appDir, "db")
        val keyDir = File(appDir, "key")
        return HeadlessDatabaseConfig(dbDir, keyDir)
    }

    @Provides
    @MailboxDirectory
    internal fun provideMailboxDirectory(): File {
        return File(appDir, "mailbox")
    }

    @Provides
    @TorSocksPort
    internal fun provideTorSocksPort(): Int = DEFAULT_SOCKS_PORT

    @Provides
    @TorControlPort
    internal fun provideTorControlPort(): Int = DEFAULT_CONTROL_PORT

    @Provides
    @Singleton
    internal fun providePluginConfig(): PluginConfig {
        return object : PluginConfig {
            override fun getDuplexFactories(): Collection<DuplexPluginFactory> = emptyList()
            override fun getSimplexFactories(): Collection<SimplexPluginFactory> = emptyList()
            override fun shouldPoll(): Boolean = false
            override fun getTransportPreferences(): Map<TransportId, List<TransportId>> = emptyMap()
        }
    }

    @Provides
    @Singleton
    internal fun provideObjectMapper() = ObjectMapper()

    @Provides
    internal fun provideTestAvatarCreator() = TestAvatarCreator { null }
}
