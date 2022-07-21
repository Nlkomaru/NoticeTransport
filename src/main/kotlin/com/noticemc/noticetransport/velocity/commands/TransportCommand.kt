package com.noticemc.noticetransport.velocity.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import com.noticemc.noticetransport.common.ChannelKey.namespace
import com.noticemc.noticetransport.common.ChannelKey.value
import com.noticemc.noticetransport.common.PlayerLocation
import com.noticemc.noticetransport.velocity.NoticeTransport
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage

class TransportCommand {

    @CommandMethod("nt|noticetransport transport <playerName> <serverName> <world> <x> <y> <z>")
    @CommandPermission("noticetransport.commands.transport")
    @CommandDescription("player transport command")
    fun playerTransport(sender: CommandSource,
        @Argument(value = "playerName", suggestions = "playerName") playerName: String,
        @Argument(value = "serverName", suggestions = "serverName") serverName: String,
        @Argument(value = "world", suggestions = "world") world: String,
        @Argument(value = "x") x: Int,
        @Argument(value = "y") y: Int,
        @Argument(value = "z") z: Int) {

        val mm = MiniMessage.miniMessage()

        val player =
            NoticeTransport.server.allPlayers.find { it.username == playerName } ?: return sender.sendMessage(mm.deserialize("Player not found"))
        val server = NoticeTransport.server.getServer(serverName).orElse(null) ?: return sender.sendMessage(mm.deserialize("Server not found"))
        val playerLocation = PlayerLocation(player.uniqueId, world, x.toDouble(), y.toDouble(), z.toDouble())
        val json = Json.encodeToString(playerLocation)

        server.sendPluginMessage(MinecraftChannelIdentifier.create(namespace, value), json.toByteArray())

        if (server.serverInfo.name != player.currentServer.get().server.serverInfo.name) {
            player.createConnectionRequest(server).fireAndForget()
        }


    }

    @Suggestions("playerName")
    fun playerNameSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return NoticeTransport.server.allPlayers.map { it.username }.toList()
    }

    @Suggestions("serverName")
    fun serverSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return NoticeTransport.server.allServers.map { it.serverInfo.name }.toList()
    }

    @Suggestions("world")
    fun worldSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return listOf("world", "world_nether", "world_the_end")
    }
}