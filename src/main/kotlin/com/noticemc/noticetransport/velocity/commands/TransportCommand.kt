package com.noticemc.noticetransport.velocity.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.suggestions.Suggestions
import com.noticemc.noticetransport.common.ChannelKey.namespace
import com.noticemc.noticetransport.common.ChannelKey.value
import com.noticemc.noticetransport.common.PlayerLocation
import com.noticemc.noticetransport.common.TemplateLocation
import com.noticemc.noticetransport.velocity.NoticeTransport
import com.noticemc.noticetransport.velocity.NoticeTransport.Companion.server
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent.Companion.list
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent.Companion.nextPlayer
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent.Companion.nowPlaying
import com.noticemc.noticetransport.velocity.event.PlayerLeftEvent.Companion.waiting
import com.noticemc.noticetransport.velocity.files.Config
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage
import java.nio.file.Files

@CommandMethod("nt|noticetransport")
class TransportCommand {

    val locationFiles = NoticeTransport.dataDirectory.toFile().resolve("location")
    val mm = MiniMessage.miniMessage()

    @CommandMethod("tp -d|-default <playerName> <serverName> <world> <x> <y> <z>")
    @CommandPermission("noticetransport.commands.transport")
    @CommandDescription("player transport command")
    fun playerTransport(sender: CommandSource,
        @Argument(value = "playerName", suggestions = "playerName") playerName: String,
        @Argument(value = "serverName", suggestions = "serverName") serverName: String,
        @Argument(value = "world", suggestions = "world") world: String,
        @Argument(value = "x") x: Double,
        @Argument(value = "y") y: Double,
        @Argument(value = "z") z: Double) {

        val player = server.allPlayers.find { it.username == playerName } ?: return sender.sendMessage(mm.deserialize("Player not found"))
        val server = NoticeTransport.server.getServer(serverName).orElse(null) ?: return sender.sendMessage(mm.deserialize("Server not found"))
        val playerLocation = PlayerLocation(player.uniqueId, world, x, y, z)
        val json = Json.encodeToString(playerLocation)

        server.sendPluginMessage(MinecraftChannelIdentifier.create(namespace, value), json.toByteArray())

        if (server.serverInfo.name != player.currentServer.get().server.serverInfo.name) {
            player.createConnectionRequest(server).fireAndForget()
        }
    }

    @CommandMethod("tp -t|-template <playerName> <file>")
    @CommandPermission("noticetransport.commands.transport")
    @CommandDescription("player transport command")
    fun playerTransportFile(sender: CommandSource,
        @Argument(value = "playerName", suggestions = "playerName") playerName: String,
        @Argument(value = "file", suggestions = "file") file: String) {

        val templateFile = locationFiles.resolve("$file.json")

        val playerLocation = if (templateFile.exists()) {
            Json.decodeFromString<TemplateLocation>(Files.readString(templateFile.toPath()))
        } else {
            return sender.sendMessage(mm.deserialize("File not found"))
        }

        playerTransport(sender,
            playerName,
            playerLocation.server,
            playerLocation.location.world,
            playerLocation.location.x,
            playerLocation.location.y,
            playerLocation.location.z)
    }

    @CommandMethod("wait")
    @CommandPermission("noticetransport.commands.wait")
    @CommandDescription("wait command")
    suspend fun wait(sender: CommandSource) {
        if (sender !is Player) {
            sender.sendMessage(mm.deserialize("You must be a player to use this command"))
            return
        }
        if (list.isEmpty()) {
            Config.config.templateFileName.keys.forEach { serverName ->
                if (nowPlaying[serverName]?.isEmpty() == true) {
                    nextPlayer(serverName)
                    return
                }
            }
        } else {
            list.add(sender)
        }
    }

    @CommandMethod("clear -w|-wait")
    @CommandPermission("noticetransport.commands.clear.wait")
    @CommandDescription("clear command")
    fun clear(sender: CommandSource) {
        list.clear()
    }

    @CommandMethod("clear -p|-playing <serverName>")
    @CommandPermission("noticetransport.commands.clear.playing")
    @CommandDescription("clear command")
    fun clearPlaying(sender: CommandSource, @Argument(value = "serverName", suggestions = "serverName") serverName: String) {
        nowPlaying[serverName]?.clear()
    }

    @CommandMethod("tp wait accept <serverName>")
    @CommandPermission("noticetransport.commands.tp.accept")
    @CommandDescription("tp invite accept command")
    @Hidden
    fun tpAccept(sender: CommandSource, @Argument(value = "serverName", suggestions = "serverName") serverName: String) {
        if (sender !is Player) {
            sender.sendMessage(mm.deserialize("You must be a player to use this command"))
            return
        }
        if (waiting[serverName]?.contains(sender) != true) {
            return
        }
        waiting[serverName]?.remove(sender)

        sender.sendMessage(mm.deserialize("You have accepted the invite"))

        if (nowPlaying[serverName] == null) {
            nowPlaying[serverName] = arrayListOf()
        }
        nowPlaying[serverName]?.add(sender)

        val template = Config.config.templateFileName[serverName] ?: return

        playerTransportFile(sender, sender.username, template)
    }

    @Suggestions("playerName")
    fun playerNameSuggestions(): List<String> {
        return server.allPlayers.map { it.username }
    }

    @Suggestions("serverName")
    fun serverSuggestions(): List<String> {
        return server.allServers.map { it.serverInfo.name }
    }

    @Suggestions("world")
    fun worldSuggestions(): List<String> {
        return listOf("world", "world_nether", "world_the_end")
    }

    @Suggestions("file")
    fun fileSuggestions(): List<String> {
        locationFiles.mkdirs()
        return locationFiles.listFiles()?.map { it.nameWithoutExtension } ?: listOf()
    }

}