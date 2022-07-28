package com.noticemc.noticetransport.velocity

import cloud.commandframework.annotations.*
import cloud.commandframework.execution.CommandExecutionCoordinator
import cloud.commandframework.meta.SimpleCommandMeta
import cloud.commandframework.velocity.CloudInjectionModule
import cloud.commandframework.velocity.VelocityCommandManager
import com.google.inject.*
import com.noticemc.noticetransport.velocity.commands.TransportCommand
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent
import com.noticemc.noticetransport.velocity.files.Config
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import java.nio.file.Path
import java.util.logging.*

class NoticeTransport {

    @Inject
    lateinit var injector: Injector

    @Inject
    fun noticeTransport(server: ProxyServer, logger: Logger, @DataDirectory dataDirectory: Path) {
        Companion.logger = logger
        Companion.server = server
        Companion.dataDirectory = dataDirectory
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        server.eventManager.register(this, PlayerLeftEvent())
        setCommand()
        Config.load()
    }

    private fun setCommand() {
        val childInjector: Injector = this.injector.createChildInjector(CloudInjectionModule(CommandSource::class.java,
            CommandExecutionCoordinator.simpleCoordinator(),
            java.util.function.Function.identity(),
            java.util.function.Function.identity()))
        val commandManager: VelocityCommandManager<CommandSource> =
            childInjector.getInstance(Key.get(object : TypeLiteral<VelocityCommandManager<CommandSource>>() {}))

        val annotationParser: AnnotationParser<CommandSource> = AnnotationParser(commandManager, CommandSource::class.java) {
            SimpleCommandMeta.empty()
        }

        annotationParser.parse(TransportCommand)
    }

    companion object {
        lateinit var logger: Logger
        lateinit var server: ProxyServer
        lateinit var dataDirectory: Path
    }
}