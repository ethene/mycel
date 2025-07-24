package com.quantumresearch.mycel.app.headless

import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import com.quantumresearch.mycel.spore.account.AccountModule
import com.quantumresearch.mycel.spore.api.FeatureFlags
import com.quantumresearch.mycel.spore.api.db.DatabaseConfig
import com.quantumresearch.mycel.spore.api.mailbox.MailboxDirectory
import com.quantumresearch.mycel.spore.api.plugin.PluginConfig
import com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_CONTROL_PORT
import com.quantumresearch.mycel.spore.api.plugin.TorConstants.DEFAULT_SOCKS_PORT
import com.quantumresearch.mycel.spore.api.plugin.TorControlPort
import com.quantumresearch.mycel.spore.api.plugin.TorDirectory
import com.quantumresearch.mycel.spore.api.plugin.TorSocksPort
import com.quantumresearch.mycel.spore.api.plugin.TransportId
import com.quantumresearch.mycel.spore.api.plugin.duplex.DuplexPluginFactory
import com.quantumresearch.mycel.spore.api.plugin.simplex.SimplexPluginFactory
import com.quantumresearch.mycel.spore.battery.DefaultBatteryManagerModule
import com.quantumresearch.mycel.spore.event.DefaultEventExecutorModule
import com.quantumresearch.mycel.spore.plugin.tor.MacTorPluginFactory
import com.quantumresearch.mycel.spore.plugin.tor.UnixTorPluginFactory
import com.quantumresearch.mycel.spore.plugin.tor.WindowsTorPluginFactory
import com.quantumresearch.mycel.spore.system.ClockModule
import com.quantumresearch.mycel.spore.system.DefaultTaskSchedulerModule
import com.quantumresearch.mycel.spore.system.DefaultThreadFactoryModule
import com.quantumresearch.mycel.spore.system.DefaultWakefulIoExecutorModule
import com.quantumresearch.mycel.spore.system.DesktopSecureRandomModule
import com.quantumresearch.mycel.spore.util.OsUtils.isLinux
import com.quantumresearch.mycel.spore.util.OsUtils.isMac
import com.quantumresearch.mycel.spore.util.OsUtils.isWindows
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
        DefaultBatteryManagerModule::class,
        DefaultEventExecutorModule::class,
        DefaultTaskSchedulerModule::class,
        DefaultWakefulIoExecutorModule::class,
        DefaultThreadFactoryModule::class,
        DesktopSecureRandomModule::class,
        HeadlessBlogModule::class,
        HeadlessContactModule::class,
        HeadlessEventModule::class,
        HeadlessForumModule::class,
        HeadlessMessagingModule::class
    ]
)
internal class HeadlessModule(private val appDir: File) {

    @Provides
    @Singleton
    internal fun provideBriarService(briarService: BriarServiceImpl): BriarService = briarService

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
    @TorDirectory
    internal fun provideTorDirectory(): File {
        return File(appDir, "tor")
    }

    @Provides
    @TorSocksPort
    internal fun provideTorSocksPort(): Int = DEFAULT_SOCKS_PORT

    @Provides
    @TorControlPort
    internal fun provideTorControlPort(): Int = DEFAULT_CONTROL_PORT

    @Provides
    @Singleton
    internal fun providePluginConfig(
        unixTor: UnixTorPluginFactory,
        macTor: MacTorPluginFactory,
        winTor: WindowsTorPluginFactory
    ): PluginConfig {
        val duplex: List<DuplexPluginFactory> = when {
            isLinux() -> listOf(unixTor)
            isMac() -> listOf(macTor)
            isWindows() -> listOf(winTor)
            else -> emptyList()
        }
        return object : PluginConfig {
            override fun getDuplexFactories(): Collection<DuplexPluginFactory> = duplex
            override fun getSimplexFactories(): Collection<SimplexPluginFactory> = emptyList()
            override fun shouldPoll(): Boolean = true
            override fun getTransportPreferences(): Map<TransportId, List<TransportId>> = emptyMap()
        }
    }

    @Provides
    @Singleton
    internal fun provideObjectMapper() = ObjectMapper()

    @Provides
    internal fun provideFeatureFlags() = object : FeatureFlags {
        override fun shouldEnableImageAttachments() = false
        override fun shouldEnableProfilePictures() = false
        override fun shouldEnableDisappearingMessages() = false
        override fun shouldEnablePrivateGroupsInCore() = false
        override fun shouldEnableForumsInCore() = true
        override fun shouldEnableBlogsInCore() = true
    }
}
