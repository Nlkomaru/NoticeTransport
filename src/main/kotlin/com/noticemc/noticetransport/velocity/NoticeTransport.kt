package com.noticemc.noticetransport.velocity

import cloud.commandframework.annotations.*
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator
import cloud.commandframework.kotlin.coroutines.annotations.installCoroutineSupport
import cloud.commandframework.meta.SimpleCommandMeta
import cloud.commandframework.velocity.CloudInjectionModule
import cloud.commandframework.velocity.VelocityCommandManager
import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.github.shynixn.mccoroutine.velocity.registerSuspend
import com.google.inject.*
import com.noticemc.noticetransport.velocity.commands.TransportCommand
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent
import com.noticemc.noticetransport.velocity.files.Config
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.logging.*

class NoticeTransport {

    @Inject
    lateinit var injector: Injector

    @Inject
    lateinit var pluginContainer: PluginContainer

    @Inject
    fun noticeTransport(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path ,suspendingPluginContainer: SuspendingPluginContainer) {
        suspendingPluginContainer.initialize(this)
        Companion.logger = logger
        Companion.server = server
        Companion.dataDirectory = dataDirectory
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        Config.load()
        server.eventManager.registerSuspend(this, PlayerLeftEvent())
        setCommand()

    }

    private fun setCommand() {
        val childInjector: Injector = this.injector.createChildInjector(CloudInjectionModule(CommandSource::class.java,
            AsynchronousCommandExecutionCoordinator.newBuilder<CommandSource>().build(),
            java.util.function.Function.identity(),
            java.util.function.Function.identity()))
        val commandManager: VelocityCommandManager<CommandSource> =
            childInjector.getInstance(Key.get(object : TypeLiteral<VelocityCommandManager<CommandSource>>() {}))

        val annotationParser: AnnotationParser<CommandSource> = AnnotationParser(commandManager, CommandSource::class.java) {
            SimpleCommandMeta.empty()
        }.installCoroutineSupport()

        annotationParser.parse(TransportCommand())
    }

    companion object {
        lateinit var logger: Logger
        lateinit var server: ProxyServer
        lateinit var dataDirectory: Path
    }
}