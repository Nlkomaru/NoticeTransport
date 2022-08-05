package com.noticemc.noticetransport.velocity.commands

import cloud.commandframework.annotations.*
import cloud.commandframework.annotations.suggestions.Suggestions
import cloud.commandframework.context.CommandContext
import com.noticemc.noticetransport.common.*
import com.noticemc.noticetransport.common.ChannelKey.namespace
import com.noticemc.noticetransport.common.ChannelKey.value
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
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.key.Key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import java.nio.file.Files

@CommandMethod("nt|noticetransport")
class TransportCommand {
    companion object {

        val locationFiles = NoticeTransport.dataDirectory.toFile().resolve("location")
        val mm = MiniMessage.miniMessage()
    }

    @CommandMethod("tp -d|-default <playerName> <serverName> <world> <x> <y> <z>")
    @CommandPermission("noticetransport.commands.transport")
    @CommandDescription("player transport command")
    suspend fun playerTransport(sender: CommandSource,
        @Argument(value = "playerName", suggestions = "playerName") playerName: String,
        @Argument(value = "serverName", suggestions = "serverName") serverName: String,
        @Argument(value = "world", suggestions = "world") world: String,
        @Argument(value = "x") x: Double,
        @Argument(value = "y") y: Double,
        @Argument(value = "z") z: Double) {

        val player = server.allPlayers.find { it.username == playerName } ?: return sender.sendMessage(mm.deserialize("Player not found"))
        val server = NoticeTransport.server.getServer(serverName).orElse(null) ?: return sender.sendMessage(mm.deserialize("Server not found"))
        val playerLocation = PlayerLocation(player.uniqueId, Location(world, x, y, z))
        val json = Json.encodeToString(playerLocation)



        if (server.serverInfo.name != player.currentServer.get().server.serverInfo.name) {
            player.createConnectionRequest(server).fireAndForget()
            delay(2500)
        }
        server.sendPluginMessage(MinecraftChannelIdentifier.create(namespace, value), json.toByteArray())
    }

    @CommandMethod("tp -t|-template <playerName> <file>")
    @CommandPermission("noticetransport.commands.transport")
    @CommandDescription("player transport command")
    suspend fun playerTransportFile(sender: CommandSource,
        @Argument(value = "playerName", suggestions = "playerName") playerName: String,
        @Argument(value = "file", suggestions = "file") file: String) {

        val templateFile = locationFiles.resolve("$file.json")

        val playerLocation = if (templateFile.exists()) {
            Json.decodeFromString<TemplateLocation>(withContext(Dispatchers.IO) {
                Files.readString(templateFile.toPath())
            })
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
        var notWaiting = false
        Config.config.templateFileName.keys.forEach { serverName ->
            if (waiting[serverName]!!.isEmpty()) {
                notWaiting = true
            }
        }
        if (list.isEmpty() && notWaiting) {
            list.add(sender)
            Config.config.templateFileName.keys.forEach { serverName ->
                if (nowPlaying[serverName]!!.isEmpty() && waiting[serverName]!!.isEmpty()) {
                    println("$serverName is empty")
                    nextPlayer(serverName)
                    return
                }
            }
        } else {
            sender.sendMessage(mm.deserialize("Wait..."))
            list.add(sender)
        }
    }

    @CommandMethod("invite <playerName>")
    @CommandPermission("noticetransport.commands.invite")
    @CommandDescription("invite command")
    suspend fun invite(sender: CommandSource, @Argument(value = "playerName", suggestions = "playerName") playerName: String) {
        if (sender !is Player) {
            sender.sendMessage(mm.deserialize("You must be a player to use this command"))
            return
        }
        val player = server.allPlayers.find { it.username == playerName } ?: return sender.sendMessage(mm.deserialize("Player not found"))
        val server = sender.currentServer.orElse(null) ?: return sender.sendMessage(mm.deserialize("You must be on a server to use this command"))
        val serverName = server.serverInfo.name
        if (nowPlaying[serverName]?.contains(sender) != true) {
            sender.sendMessage(mm.deserialize("There are no players playing on this server"))
            return
        }
        waiting[serverName]?.add(player)
        player.sendMessage(mm.deserialize("<click:run_command:'/nt tp wait accept $serverName'><color:green><hover:show_text:'クリックで承認'>${sender.username}から${serverName}に招待されました</hover></click>"))

        player.playSound(Sound.sound(Key.key("block.note_block.iron_xylophone"), Sound.Source.VOICE, 1.0F, 1.0F))

        delay(Config.config.timeOut.toLong() * 1000)

        if (waiting[serverName]?.contains(player) == true) {
            waiting[serverName]?.remove(player)
            player.sendMessage(mm.deserialize("<color:red>一定時間操作がなかったため、キャンセルされました"))
        }
    }

    @CommandMethod("clear -w|-wait")
    @CommandPermission("noticetransport.commands.clear.wait")
    @CommandDescription("clear command")
    fun clear() {
        list.clear()
    }

    @CommandMethod("show -w")
    @CommandPermission("noticetransport.commands.show.wait")
    @CommandDescription("show command")
    fun show(sender: CommandSource) {
        val names = arrayListOf<String>()
        list.forEach {
            names.add(it.username)
        }
        sender.sendMessage(mm.deserialize("waiting: ${names.joinToString(", ")}"))
    }

    @CommandMethod("show -p <serverName>")
    @CommandPermission("noticetransport.commands.show.playing")
    @CommandDescription("show command")
    fun showPlaying(sender: CommandSource, @Argument(value = "serverName", suggestions = "serverName") serverName: String) {
        val names = arrayListOf<String>()
        nowPlaying[serverName]?.forEach {
            names.add(it.username)
        }
        sender.sendMessage(mm.deserialize("playing: ${names.joinToString(", ")}"))
    }

    @CommandMethod("template <fileName> <serverName> <world> <x> <y> <z>")
    @CommandPermission("noticetransport.commands.create.template")
    @CommandDescription("template file create  command")
    fun createTemplate(sender: CommandSource,
        @Argument(value = "serverName", suggestions = "serverName") serverName: String,
        @Argument(value = "fileName") fileName: String,
        @Argument(value = "world", suggestions = "world") world: String,
        @Argument(value = "x") x: Double,
        @Argument(value = "y") y: Double,
        @Argument(value = "z") z: Double) {
        val templateLocation = TemplateLocation(serverName, Location(world, x, y, z))
        val json = Json.encodeToString(templateLocation)
        val file = locationFiles.resolve("$fileName.json")
        file.parentFile.mkdirs()
        file.createNewFile()
        file.writeText(json)
    }

    @CommandMethod("clear -p|-playing <serverName>")
    @CommandPermission("noticetransport.commands.clear.playing")
    @CommandDescription("clear command")
    fun clearPlaying(@Argument(value = "serverName", suggestions = "serverName") serverName: String) {
        nowPlaying[serverName]?.clear()
    }

    @CommandMethod("tp wait accept <serverName>")
    @CommandPermission("noticetransport.commands.tp.accept")
    @CommandDescription("tp invite accept command")
    @Hidden
    suspend fun tpAccept(sender: CommandSource, @Argument(value = "serverName", suggestions = "serverName") serverName: String) {
        if (sender !is Player) {
            return
        }
        if (!waiting[serverName]!!.contains(sender)) {
            return
        }
        waiting[serverName]!!.remove(sender)

        val template = Config.config.templateFileName[serverName] ?: return

        playerTransportFile(sender, sender.username, template)
        delay(3000)
        if (sender.currentServer.orElse(null).serverInfo.name == serverName) {
            nowPlaying[serverName]!!.add(sender)
        }
    }

    @CommandMethod("reload")
    @CommandPermission("noticetransport.commands.reload")
    @CommandDescription("reload command")
    fun reload(sender: CommandSource) {
        Config.load()
        sender.sendMessage(mm.deserialize("reloaded"))
    }

    @Suggestions("playerName")
    fun playerNameSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return server.allPlayers.map { it.username }
    }

    @Suggestions("serverName")
    fun serverSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return server.allServers.map { it.serverInfo.name }
    }

    @Suggestions("world")
    fun worldSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        return listOf("world", "world_nether", "world_the_end").toList()
    }

    @Suggestions("file")
    fun fileSuggestions(sender: CommandContext<CommandSource>, input: String?): List<String> {
        locationFiles.mkdirs()
        return locationFiles.listFiles()?.map { it.nameWithoutExtension } ?: listOf()
    }

}
